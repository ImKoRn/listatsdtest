package com.imkorn.listatsdtest.model;

import java.io.IOException;

/**
 * Created by imkorn on 24.09.17.
 */

public class TcpSocket {

    private volatile boolean connected;

    private volatile boolean delivered;

    private volatile boolean disconnected;

    public void reset() {
        connected = false;
        delivered = false;
        disconnected = false;
    }

    public synchronized void connectWithServer() throws
                                       IOException {
        while (!connected) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public synchronized void write(Object object) throws
                                                  IOException {
        while (!delivered) {
            if (disconnected) {
                throw new IOException("Disconnected");
            }

            try {
                wait();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public synchronized void connect() {
        this.connected = true;
        this.disconnected = false;
        notify();
    }

    public synchronized void receive() {
        this.delivered = true;
        notify();
    }

    public synchronized void disconnect() {
        this.disconnected = true;
        this.connected = false;
        notify();
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public boolean isDisconnected() {
        return disconnected;
    }
}
