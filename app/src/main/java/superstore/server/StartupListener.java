package superstore.server;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import com.googlecode.cqengine.query.QueryFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import superstore.common.shared.DataStore;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebListener
public class StartupListener implements ServletContextListener {
    public static final DataStore store = new DataStore();

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

    private static final int batchSize = 50_000;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        new Thread(() -> {
            // load data from disk
            File csv = new File(TRAFFIC_CSV);
            long totalLength = csv.length();

            try (CountingFileReader reader = new CountingFileReader(csv);
                 CSVParser parser = new CSVParser(new BufferedReader(reader), CSVFormat.RFC4180.withHeader())){
                Iterator<CSVRecord> recordIterator = Iterators.peekingIterator(parser.iterator());
                while (recordIterator.hasNext()) {
                    List<Map<String, String>> page = Streams.stream(Iterators.limit(recordIterator, batchSize))
                            .map(StartupListener::wrap)
                            .collect(Collectors.toList());
                    store.addAll(page);

                    double percent = (double) reader.getPosition() / totalLength;
                    System.out.println("data loaded from disk: " + (int) (percent * 100) + "%, " + store.size() + " items");
//                client.dataLoadedFromDisk("traffic", percent, trafficData.size());
//                if (trafficData.size() >= 5_000_000) {
//                    // stop so we can stream the rest of the results
//                    break;
//                }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static Map<String, String> wrap(CSVRecord record) {
        return QueryFactory.mapEntity(record.toMap());
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
