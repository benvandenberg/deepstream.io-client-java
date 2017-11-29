package io.deepstream;

import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

class JavaEndpointWebsocket implements Endpoint {

    private final URI uri;
    private WebSocket websocket;
    private final Connection connection;
    private final Integer connectTimeout;  // milliseconds

    JavaEndpointWebsocket( URI uri, Connection connection ) {
        this.uri = uri;
        this.connection = connection;
        this.connectTimeout = null;
    }

    JavaEndpointWebsocket( URI uri, Connection connection, int connectTimeout ) {
        this.uri = uri;
        this.connection = connection;
        this.connectTimeout = connectTimeout;
    }

    @Override
    public EndpointState getEndpointState() {
        EndpointState endpointState = null;
        if (this.websocket == null) {
            endpointState = EndpointState.CLOSED;
        } else if (this.websocket.getReadyState() == READYSTATE.CLOSED) {
            endpointState = EndpointState.CLOSED;
        } else if (this.websocket.getReadyState() == READYSTATE.CLOSING) {
            endpointState = EndpointState.CLOSING;
        } else if (this.websocket.getReadyState() == READYSTATE.CONNECTING) {
            endpointState = EndpointState.CONNECTING;
        } else if (this.websocket.getReadyState() == READYSTATE.OPEN) {
            endpointState = EndpointState.OPEN;
        } else if (this.websocket.getReadyState() == READYSTATE.NOT_YET_CONNECTED) {
            endpointState = EndpointState.NOT_YET_CONNECTED;
        }
        return endpointState;
    }

    @Override
    public void send(String message) {
        this.websocket.send( message );
    }

    @Override
    public void close() {
        this.websocket.close();
        this.websocket = null;
    }

    @Override
    public void forceClose() {
        this.websocket.getConnection().closeConnection(1, "Forcing connection close due to network loss");
    }

    @Override
    public void open() {
        if (this.connectTimeout != null) {
            this.websocket = new WebSocket(this.uri, new Draft_6455(), connectTimeout);
        } else {
            this.websocket = new WebSocket(this.uri, new Draft_6455());
        }
        this.websocket.connect();
    }

    private class WebSocket extends WebSocketClient {
        WebSocket( URI serverUri , Draft draft ) {
            super( serverUri, draft );
            // Set the SSL context if the socket server is using Secure WebSockets
            if (serverUri.toString().startsWith("wss:")) {
                setSslContext();
            }
        }

        WebSocket( URI serverUri , Draft draft, int connectTimeout ) {
            super( serverUri, draft, null, connectTimeout );
            // Set the SSL context if the socket server is using Secure WebSockets
            if (serverUri.toString().startsWith("wss:")) {
                setSslContext();
            }
        }

        private void setSslContext() {
            SSLContext sslContext;
            SSLSocketFactory factory;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                factory = sslContext.getSocketFactory();
                this.setSocket(factory.createSocket());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (KeyManagementException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            connection.onOpen();
        }

        @Override
        public void onMessage(String message) {
            connection.onMessage( message );
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            try {
                connection.onClose();
            } catch( Exception e ) {
            }
        }

        @Override
        public void onError(Exception ex) {
            if (ex instanceof NullPointerException && ex.getMessage().equals("ssl == null")) {
                return;
            }
            connection.onError( ex.getMessage() );
        }
    }
}
