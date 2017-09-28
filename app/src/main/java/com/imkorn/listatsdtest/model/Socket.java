package com.imkorn.listatsdtest.model;

import com.imkorn.listatsdtest.model.entities.PrimeNumber;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by imkorn on 24.09.17.
 */
public class Socket extends Thread {

    private volatile boolean connected;

    private volatile boolean closed;

    private final ComposeDisplay display;

    private final BlockingQueue<Collection<PrimeNumber>> queue = new LinkedBlockingQueue<>();

    private final Random random = new Random();

    public Socket(ComposeDisplay display) {
        this.display = display;
        start();
    }

    @Override
    public void run() {
        for (;;) {
            if (closed) {
                return;
            }

            try {
                synchronized (this) {
                    if (closed) {
                        return;
                    }

                    wait(1);
                    final boolean newState = random.nextInt(100) % 2 == 0;

                    if (newState) {
                        connected = true;
                        notify();

                        for (;;) {
                            if (queue.isEmpty()) {
                                wait();
                                continue;
                            }

                            display.displayResult(queue.take());
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                display.displayError(e);
            }
        }
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
            notify();
        }
    }

    public void display(Collection<PrimeNumber> primeNumbers) {
        if (closed) {
            return;
        }

        connected = false;
        queue.add(primeNumbers);
        notify();
    }

    public boolean isConnected() {
        return connected;
    }
}
