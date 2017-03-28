package superstore.ui.client;

import com.colinalworth.gwt.websockets.client.ServerBuilder;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import superstore.common.shared.StoreClient;
import superstore.common.shared.StoreServer;
import superstore.worker.client.ClientMessageHandler;
import superstore.worker.client.ClientMessageHandler.Console;

import java.util.List;
import java.util.Map;

/**
 * Created by colin on 3/25/17.
 */
public class App implements EntryPoint {

    interface Factory extends ServerBuilder<StoreServer> {}

    @Override
    public void onModuleLoad() {
        Factory factory = GWT.create(Factory.class);
        factory.setPath("/socket");

        StoreServer server = factory.start();

        server.setClient(new ClientMessageHandler());

        TextArea textArea = new TextArea();
        textArea.setValue("between(\"Date\", \"2010-01-01\", \"2010-01-02\")");
        Button query = new Button("go", (ClickHandler) e -> {
            server.parseQuery("traffic", textArea.getValue(), new Callback<ParseResult<?>, IllegalStateException>() {
                @Override
                public void onFailure(IllegalStateException reason) {
                    Console.log(reason.getMessage());
                }

                @Override
                public void onSuccess(ParseResult<?> result) {
                    server.runQuery(result.getQuery(), result.getQueryOptions());
                }
            });
        });

        RootPanel.get().add(textArea);
        RootPanel.get().add(query);
    }
}
