package superstore.server;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.index.support.KeyStatisticsAttributeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import com.googlecode.cqengine.query.parser.cqn.CQNParser;
import com.googlecode.cqengine.resultset.ResultSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.gwtproject.rpc.api.Callback;
import org.gwtproject.rpc.servlet.websocket.AbstractServerImpl;
import superstore.common.shared.StoreClient;
import superstore.common.shared.StoreClient_Impl;
import superstore.common.shared.StoreServer;
import superstore.common.shared.attribute.AbstractMapAttribute;
import superstore.common.shared.attribute.MapIntegerAttribute;
import superstore.common.shared.attribute.MapStringAttribute;

import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@ServerEndpoint("/socket")
public class Server extends AbstractServerImpl<StoreServer, StoreClient> implements StoreServer {
    private static final ScheduledExecutorService execService = Executors.newScheduledThreadPool(2);
    /**
     * About six million records, from Jan 1 2002 to Dec 31 1006. five years of hourly records, about 365 days in a
     * year, 24 hrs in a day makes for about 43,800 records per station. If you play 4 hours per second, that is
     * still 182 minutes to replay, so we'll start the clock with one hour to go, or about a 3 years and 8 months
     * into the stream, i.e. Aug 1 2005.
     *
     * Worth noting that the 6m file requires a minimum of 5gb to load, more to query.
     * The 3m file requires a minimum of 2.5gb to load, more to query.
     */
    public static final String TRAFFIC_CSV = System.getProperty("traffic_csv", "/home/colin/workspace/worker-db/mn_dot_traffic_3m.csv");
    private Date startNow = new Date(2005 - 1900, 8 - 1, 1);
    /** How many millis to wait before ticking the "current hour" forward */
    private static final int millisPerHour = 250;

    private static final IndexedCollection<Map<String, String>> trafficData = new ConcurrentIndexedCollection<>();

    private static final MapStringAttribute dateAttribute = new MapStringAttribute("Date");
    private static final MapIntegerAttribute hourAttribute = new MapIntegerAttribute("Hour");

    private static final Attribute<Map<String, String>, String> timestampAttribute = new SimpleAttribute<Map<String, String>, String>() {
        @Override
        public String getValue(Map<String, String> object, QueryOptions queryOptions) {
            return dateAttribute.getValue(object, queryOptions) + "T" + hourAttribute.getValue(object, queryOptions);
        }
    };

    private static final int batchSize = 50_000;

    static {
        trafficData.addIndex(NavigableIndex.onAttribute(dateAttribute));
        //TODO partial index on each station for its date and hour?
        trafficData.addIndex(HashIndex.onAttribute(new MapStringAttribute("STA")));
        trafficData.addIndex(HashIndex.onAttribute(new MapStringAttribute("Direction")));

        //one more attribute, to get us the ability to pretend-replay history
        trafficData.addIndex(NavigableIndex.onAttribute(timestampAttribute));

        //load data from disk
        File csv = new File(TRAFFIC_CSV);
        long totalLength = csv.length();

        try {
            CountingFileReader reader = new CountingFileReader(csv);
            CSVParser parser = new CSVParser(new BufferedReader(reader), CSVFormat.RFC4180.withHeader());
            Iterator<CSVRecord> recordIterator = Iterators.peekingIterator(parser.iterator());
            while (recordIterator.hasNext()) {
                List<Map<String, String>> page = Streams.stream(Iterators.limit(recordIterator, batchSize))
                        .map(Server::wrap)
                        .collect(Collectors.toList());
                trafficData.addAll(page);


                double percent = (double) reader.getPosition() / totalLength;
                System.out.println("data loaded from disk: " + (int) (percent * 100) + "%, " + trafficData.size() + " items");
//                client.dataLoadedFromDisk("traffic", percent, trafficData.size());
//                if (trafficData.size() >= 5_000_000) {
//                    // stop so we can stream the rest of the results
//                    break;
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** currently executing and updated query for this instance's active connection */
    private Query<Map<String, String>> activeQuery;
    private QueryOptions activeQueryOptions;
    /** Items left to read from the file */


    private final Object resultsStreamLock = new Object();
    private Date now = startNow;
    private Date lastResultsSent;
    private ScheduledFuture<?> tick;
    private ScheduledFuture<?> streamingResults;

    public Server() {
        super(StoreClient_Impl::new);
    }

    @Override
    public void onOpen(Connection connection, StoreClient client) {
        super.onOpen(connection, client);

        //inform client of schemas
        client.schemaLoaded("traffic", getSchema("traffic"));

        // start a thread telling the client that data is ready, streaming an hour per second, if it matches
        // the active query

        tick = execService.scheduleAtFixedRate(() -> {
            //scheduleAtFixedRate specifies:
            //  If any execution of this task
            //  takes longer than its period, then subsequent executions
            //  may start late, but will not concurrently execute.
            now = addNHours(now, 1);
        }, millisPerHour, millisPerHour, TimeUnit.MILLISECONDS);

        // The original demo assumed a single client at a time, so just read more data from disk, but to support
        // multiple clients that would... be expensive.
//        execService.scheduleAtFixedRate(() -> {
//            //scheduleAtFixedRate specifies:
//            //  If any execution of this task
//            //  takes longer than its period, then subsequent executions
//            //  may start late, but will not concurrently execute.
//
//            if (!recordIterator.hasNext()) {
//                return;
//            }
//            //read an item, read as many more as can be read that match the same day and hour
//            List<Map<String, String>> items = new ArrayList<>();
//            Map<String, String> first = wrap(recordIterator.next());
//            items.add(first);
//            String date = first.get("Date");
//            String hour = first.get("Hour");
//            while (recordIterator.hasNext()) {
//                Map<String, String> next = wrap(recordIterator.peek());
//                if (!date.equals(next.get("Date")) || !hour.equals(next.get("Hour"))) {
//                    break;
//                }
//                items.add(next);
//                recordIterator.next();
//            }
//
//            // I think this technically could race with itself, and send out-of-ordered results to the browser
//            execService.submit(() -> addItems(items));
//        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onClose(Connection connection, StoreClient client) {
        super.onClose(connection, client);
        if (streamingResults != null) {
            streamingResults.cancel(true);
        }
        if (tick != null) {
            tick.cancel(true);
        }
    }

    private String format(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH").format(date);
    }

    private Date addNHours(Date now, int hours) {
        return new Date(now.getTime() + 1000 * 60 * 60 * hours);
    }

    private static Map<String, String> wrap(CSVRecord record) {
        return QueryFactory.mapEntity(record.toMap());
    }

    @Override
    public synchronized void runQuery(Query<Map<String, String>> query, QueryOptions options) {
        if (streamingResults != null) {
            streamingResults.cancel(false);
        }
        activeQuery = query;
        activeQueryOptions = options;
        //to prevent a race, block on updates to "now"
        synchronized (resultsStreamLock) {
            //eliminate results that "haven't arrived yet"
            query = QueryFactory.and(query, QueryFactory.lessThanOrEqualTo(timestampAttribute, format(now)));
            lastResultsSent = now;
            ResultSet<Map<String, String>> results = trafficData.retrieve(query, options);
//            if (!query.equals(activeQuery)) {
//                return;
//            }
            int size = results.size();
            getClient().queryFinished(activeQuery, size);
            if (size == 0) {
                return;
            }
            Iterator<Map<String, String>> iterator = results.iterator();
            int offset = 0;
            while (/*query.equals(activeQuery) && */iterator.hasNext()) {
                getClient().queryResults(activeQuery, Lists.newArrayList(Iterators.limit(iterator, 1000)), offset);
                offset += 1000;
            }
        } //TODO possible this block can end much sooner?

        //schedule updates...
        // start a thread telling the client that data is ready, streaming an hour per second, if it matches
        // the active query
        //TODO move this and cancel this on close
        streamingResults = execService.scheduleAtFixedRate(() -> {
            //scheduleAtFixedRate specifies:
            //  If any execution of this task
            //  takes longer than its period, then subsequent executions
            //  may start late, but will not concurrently execute.

            try {
                if (activeQuery == null) {
                    //something is wrong, TODO
                    return;
                }

                synchronized (resultsStreamLock) {
                    //can't use QueryFactory.between without a compound index thingie of date+hour
                    Query<Map<String, String>> q = QueryFactory.and(activeQuery, QueryFactory.between(timestampAttribute, format(lastResultsSent), true, format(now), false));
                    List<Map<String, String>> items = Lists.newArrayList(trafficData.retrieve((Query<Map<String, String>>) q, activeQueryOptions).iterator());
                    if (!items.isEmpty()) {
                        getClient().additionalQueryResults(activeQuery, items);
                    }
                    lastResultsSent = now;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void parseQuery(String schema, String query, Callback<ParseResult<Map<String, String>>, IllegalStateException> callback) {
        try {
            callback.onSuccess(CQNParser.forPojoWithAttributes((Class<Map<String, String>>) (Class) Map.class, getSchema(schema)).parse(query));
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            callback.onFailure(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            callback.onFailure(new IllegalStateException(ex));
        }
    }

    @Override
    public synchronized void loadUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute) {
        assert schema.equals("traffic");

        Optional<KeyStatisticsAttributeIndex<Object, Map<String, String>>> hasIndex = Streams.stream(trafficData.getIndexes())
                .filter(index -> index instanceof KeyStatisticsAttributeIndex)
                .map(index -> (KeyStatisticsAttributeIndex<Object, Map<String, String>>) index)
                .filter(index -> index.getAttribute().equals(attribute))
                .findFirst();
        if (hasIndex.isPresent()) {
            int count = hasIndex.get().getCountOfDistinctKeys(QueryFactory.noQueryOptions());
            getClient().uniqueKeysLoaded(attribute, count);

            CloseableIterator<Object> iterator = hasIndex.get().getDistinctKeys(QueryFactory.noQueryOptions()).iterator();
            int offset = 0;
            while (iterator.hasNext()) {
                getClient().uniqueKeysResults(attribute, Lists.newArrayList(Iterators.limit(iterator, 200)), offset);
                offset += 200;
            }
        } else {
            getClient().uniqueKeysLoaded(attribute, 0);
        }
    }

    private Map<String, AbstractMapAttribute<?>> getSchema(String schema) {
        assert schema.equals("traffic");
        Map<String, AbstractMapAttribute<?>> traffic = new HashMap<>();
        traffic.put("Date", new MapStringAttribute("Date"));
        traffic.put("STA", new MapStringAttribute("STA"));
        traffic.put("Hour", new MapIntegerAttribute("Hour"));
        traffic.put("Value", new MapIntegerAttribute("Value"));
        traffic.put("Direction", new MapStringAttribute("Direction"));
        return traffic;
    }

    @Override
    public void onError(Throwable thr) {
        thr.printStackTrace();
        super.onError(thr);
    }
}
