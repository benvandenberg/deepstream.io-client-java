package io.deepstream;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * The interface required for any connection endpoints. Currently we support websockets
 */
interface Endpoint {
    /**
     * Message to send to the deepstream server
     * @param message The message to send (TOPIC|ACTION|ARRAY+)
     */
    @ObjectiveCName("send:")
    void send(String message);

    /**
     * Close the connection
     */
    void close();

    /**
     * Open the connection
     */
    void open();

    /**
     * Forces the connection to be closed
     */
    void forceClose();

    /**
     * Retrieve the state of the endpoint. Useful for debugging purposes.
     * @return The endpoint state.
     */
    EndpointState getEndpointState();
}
