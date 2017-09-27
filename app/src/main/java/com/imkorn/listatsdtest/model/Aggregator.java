package com.imkorn.listatsdtest.model;


import com.imkorn.listatsdtest.model.entities.PrimeNumber;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by imkorn on 24.09.17.
 */

public class Aggregator extends Thread {


    private BlockingQueue<PrimeNumber> queue = new LinkedBlockingQueue<>();

    private final Socket socket;
    private final ErrorDisplay errorDisplay;

    private volatile boolean closed;

    public Aggregator(Socket socket, ErrorDisplay errorDisplay) {
        this.socket = socket;
        this.errorDisplay = errorDisplay;
        start();
    }

    public void close() {
        if (closed) {
            return;
        }

        synchronized (this) {
            if (closed) {
                return;
            }

            closed = true;
            socket.close();
            queue.clear();
            interrupt();
        }
    }

    public void push(PrimeNumber primeNumber) {
        synchronized (socket) {
            queue.add(primeNumber);
            socket.notify();
        }
    }

    @Override
    public void run() {
        try {
            for (;;) {
                if (closed) {
                    return;
                }

                synchronized (socket) {
                    if (queue.isEmpty()) {
                        socket.wait();
                        continue;
                    }

                    if (socket.isConnected()) {
                        final Collection<PrimeNumber> primeNumbers = new LinkedList<>();
                        queue.drainTo(primeNumbers);
                        socket.display(primeNumbers);
                    } else {
                        socket.wait();
                    }
                }
            }

        } catch (InterruptedException e) {
            errorDisplay.displayError(e);
        }
    }
}
