package com.imkorn.listatsdtest.model;

import com.imkorn.listatsdtest.model.entities.PrimeNumber;

import java.util.Collection;
import java.util.Random;

/**
 * Created by imkorn on 24.09.17.
 */
public class Socket extends Thread {

    private volatile boolean connected;

    private volatile boolean closed;

    private final ResultDisplay display;

    private final Random random = new Random();

    public Socket(ResultDisplay display) {
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
                    if (connected) {
                        notify();
                        wait();
                    } else {
                        wait(1);
                        connected = random.nextInt(100) % 2 == 0;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        display.displayResult(primeNumbers);
        notify();
    }

    public boolean isConnected() {
        return connected;
    }
}
