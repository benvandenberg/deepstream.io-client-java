package io.deepstream;

/**
 * Maps to the WebSocket.READYSTATE enum, representing the state a websocket may be in
 */
public enum EndpointState {
    NOT_YET_CONNECTED,
    CLOSED,
    CLOSING,
    CONNECTING,
    OPEN
}
