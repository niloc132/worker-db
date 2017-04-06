package superstore.ui.client;

import com.colinalworth.gwt.worker.client.WorkerFactory;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import superstore.common.client.StoreApp;
import superstore.common.client.StoreWorker;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.List;
import java.util.Map;

/**
 * Created by colin on 3/25/17.
 */
public class App implements EntryPoint {
    private FlowPanel remote;
    private FlowPanel local;

//    interface Factory extends ServerBuilder<StoreServer> {}

    interface Factory extends WorkerFactory<StoreWorker, StoreApp> {}
    @Override
    public void onModuleLoad() {
        RootPanel.get().add(new Label("" +
                "This demo app is best run in Chrome, with the developer tools open, to see network data stream in" +
                "(Network tab, click on 'socket'), and with the Console open to see results logged from the worker" +
                "and from the app as various events take place and to show unique index values as they are " +
                "available."
        ));
//        Factory factory = GWT.create(Factory.class);
//        factory.setPath("/socket");
//
//        StoreServer server = factory.start();

        Factory factory = GWT.create(Factory.class);
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
            public void schemaLoaded(String name, Map<String, AbstractMapAttribute<?>> columns) {
                Console.log("Schema loaded, " + columns.size() + " columns");

                for (AbstractMapAttribute<?> attribute : columns.values()) {
                    RootPanel.get().add(new Button(attribute.getAttributeName() + " values", (ClickHandler) e -> {
                        getRemote().loadLocalUniqueKeysForColumn(name, attribute);
                    }));
                }
            }

//            @Override
//            public void dataLoadedFromDisk(String schema, double percent, int rows) {
//                Console.log("Data loading for " + schema + " from disk, " + (percent * 100) + ", " + rows + " loaded");
//            }

            @Override
            public void queryFinished(Query<?> query, int totalCount) {
                Console.log("Query had " + totalCount + " results");
            }

            @Override
            public void queryResults(Query<?> query, List<Map<String, String>> results, int offset) {
                Console.log("Results loading " + offset);
            }

            @Override
            public void additionalQueryResults(Query<?> query, List<Map<String, String>> items) {
                Console.log("More items streamed in to app: " + items.size());
            }

            @Override
            public void uniqueKeysLoaded(Attribute<Map<String, String>, ?> attribute, int totalCount) {
                Console.log("unique keys for attribute " + attribute.getAttributeName() + " count: " + totalCount);
            }

            @Override
            public <T> void uniqueKeysResults(Attribute<Map<String, String>, T> attribute, List<T> results, int offset) {
                Console.log(attribute.getAttributeName() + " : " + results);
            }
        });

        TextArea remoteTextArea = new TextArea();
        remoteTextArea.setValue("between(\"Date\", \"2002-01-01\", \"2002-02-01\")");
        Button remoteQuery = new Button("go", (ClickHandler) e -> {
            worker.parseQuery("traffic", remoteTextArea.getValue(), new Callback<ParseResult<?>, IllegalStateException>() {
                @Override
                public void onFailure(IllegalStateException reason) {
                    Console.log(reason.getMessage());
                }

                @Override
                public void onSuccess(ParseResult<?> result) {
                    worker.runRemoteQuery(result.getQuery(), result.getQueryOptions());
                }
            });
        });
        remote = new FlowPanel();
        remote.add(remoteTextArea);
        remote.add(remoteQuery);
        RootPanel.get().add(remote);

        TextArea localTextArea = new TextArea();
        localTextArea.setValue("between(\"Date\", \"2010-01-01\", \"2010-01-02\")");
        Button localQuery = new Button("go", (ClickHandler) e -> {
            worker.parseQuery("traffic", localTextArea.getValue(), new Callback<ParseResult<?>, IllegalStateException>() {
                @Override
                public void onFailure(IllegalStateException reason) {
                    Console.log(reason.getMessage());
                }

                @Override
                public void onSuccess(ParseResult<?> result) {
                    worker.runLocalQuery(result.getQuery(), result.getQueryOptions());
                }
            });
        });
        local = new FlowPanel();
        local.add(localTextArea);
        local.add(localQuery);
        RootPanel.get().add(local);

    }

    @JsType(isNative = true, name = "console", namespace = JsPackage.GLOBAL)
    public static class Console {
        public native static void log(String message);
    }
}
