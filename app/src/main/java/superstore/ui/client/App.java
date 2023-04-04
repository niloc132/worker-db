package superstore.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import org.gwtproject.rpc.api.Callback;
import org.gwtproject.rpc.worker.client.WorkerFactory;
import superstore.common.client.StoreApp;
import superstore.common.client.StoreWorker;
import superstore.common.client.StoreWorker_Impl;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.List;
import java.util.Map;

import static elemental2.dom.DomGlobal.console;

/**
 * Created by colin on 3/25/17.
 */
public class App implements EntryPoint {
    private FlowPanel remote;
    private FlowPanel local;

//    interface Factory extends ServerBuilder<StoreServer> {}

    @Override
    public void onModuleLoad() {
        RootPanel.get().add(new Label("" +
                "This demo app is best run with the developer tools open, to see network data stream in" +
                "(Network tab, click on 'socket'), and with the Console open to see results logged from the worker" +
                "and from the app as various events take place and to show unique index values as they are " +
                "available."
        ));
//        Factory factory = GWT.create(Factory.class);
//        factory.setPath("/socket");
//
//        StoreServer server = factory.start();

        WorkerFactory<StoreWorker, StoreApp> factory = WorkerFactory.of(StoreWorker_Impl::new);
        StoreWorker worker = factory.createDedicatedWorker("superstore.worker.Worker/worker.js", new StoreApp() {
            private StoreWorker remote;
            @Override
            public void setRemote(StoreWorker storeWorker) {
                this.remote = storeWorker;
            }

            @Override
            public StoreWorker getRemote() {
                return remote;
            }

            @Override
            public void onError(Throwable throwable) {
                //TODO
            }

            @Override
            public void dataLoadedFromDisk(String schema, int rows) {

            }

            @Override
            public void schemaLoaded(String name, Map<String, AbstractMapAttribute<?>> columns) {
                console.log("Schema loaded, " + columns.size() + " columns");

                for (AbstractMapAttribute<?> attribute : columns.values()) {
                    RootPanel.get().add(new Button(attribute.getAttributeName() + " values", (ClickHandler) e -> {
                        getRemote().loadUniqueKeysForColumn(name, attribute);
                    }));
                }
            }

//            @Override
//            public void dataLoadedFromDisk(String schema, double percent, int rows) {
//                console.log("Data loading for " + schema + " from disk, " + (percent * 100) + ", " + rows + " loaded");
//            }

            @Override
            public void queryFinished(Query<Map<String, String>> query, int totalCount) {
                console.log("Query had " + totalCount + " results");
            }

            @Override
            public void queryResults(Query<Map<String, String>> query, List<Map<String, String>> results, int offset) {
                console.log("Results loading " + offset);
            }

            @Override
            public void additionalQueryResults(Query<Map<String, String>> query, List<Map<String, String>> items) {
                console.log("More items streamed in to app: " + items.size());
            }

            @Override
            public void uniqueKeysLoaded(Attribute<Map<String, String>, ?> attribute, int totalCount) {
                console.log("unique keys for attribute " + attribute.getAttributeName() + " count: " + totalCount);
            }

            @Override
            public void uniqueKeysResults(Attribute<Map<String, String>, ?> attribute, List<?> results, int offset) {
                console.log(attribute.getAttributeName() + " : " + results);
            }
        });

        TextArea remoteTextArea = new TextArea();
        remoteTextArea.setValue("between(\"Date\", \"2002-01-01\", \"2002-02-01\")");
        Button remoteQuery = new Button("execute remote query", (ClickHandler) e -> {
            worker.parseQuery("traffic", remoteTextArea.getValue(), new Callback<ParseResult<Map<String, String>>, IllegalStateException>() {
                @Override
                public void onFailure(IllegalStateException reason) {
                    console.log(reason.getMessage());
                }

                @Override
                public void onSuccess(ParseResult<Map<String, String>> result) {
                    worker.runRemoteQuery(result.getQuery(), result.getQueryOptions());
                }
            });
        });
        remote = new FlowPanel();
        remote.add(remoteTextArea);
        remote.add(remoteQuery);
        RootPanel.get().add(remote);

        TextArea localTextArea = new TextArea();
        localTextArea.setValue("and(between(\"Date\", \"2002-01-05\", \"2002-01-10\"), equal(\"STA\", 187))");
        Button localQuery = new Button("execute local query", (ClickHandler) e -> {
            worker.parseQuery("traffic", localTextArea.getValue(), new Callback<ParseResult<Map<String, String>>, IllegalStateException>() {
                @Override
                public void onFailure(IllegalStateException reason) {
                    console.log(reason.getMessage());
                }

                @Override
                public void onSuccess(ParseResult<Map<String, String>> result) {
                    worker.runQuery(result.getQuery(), result.getQueryOptions());
                }
            });
        });

        local = new FlowPanel();
        local.add(localTextArea);
        local.add(localQuery);
        RootPanel.get().add(local);

    }
}
