package com.imkorn.listatsdtest.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.imkorn.listatsdtest.model.entities.PrimeNumber;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by imkorn on 9/27/17.
 */
public class SchedulerDisplay extends Handler implements ResultDisplay, ErrorDisplay {

    private static final int TASK_DISPLAY = 1;
    private static final int TASK_SHOW_ERROR = 2;

    @Nullable
    private volatile ComposeDisplay display;

    private final List<Collection<PrimeNumber>> primeNumbers = new LinkedList<>();

    private final List<Throwable> throwables = new LinkedList<>();

    public SchedulerDisplay(@NonNull Looper looper) {
        super(looper);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case TASK_DISPLAY: {
                final ResultDisplay display = this.display;
                if (display != null) {
                    if (primeNumbers.isEmpty()) {
                        return;
                    }

                    synchronized (primeNumbers) {
                        if (primeNumbers.isEmpty()) {
                            return;
                        }

                        for (Collection<PrimeNumber> primeNumber : primeNumbers) {
                            display.displayResult(primeNumber);
                        }
                        primeNumbers.clear();
                    }
                }
                break;
            }
            case TASK_SHOW_ERROR: {
                final ErrorDisplay display = this.display;
                if (display != null) {
                    if (throwables.isEmpty()) {
                        return;
                    }

                    synchronized (throwables) {
                        if (throwables.isEmpty()) {
                            return;
                        }
                        for (Throwable throwable : throwables) {
                            display.displayError(throwable);
                        }
                        throwables.clear();
                    }
                }
                break;
            }
        }
    }

    public void displayResult(@NonNull Collection<PrimeNumber> primeNumbers) {
        removeMessages(TASK_DISPLAY);

        synchronized (this.primeNumbers) {
            this.primeNumbers.add(primeNumbers);
        }
        if (display != null) {
            sendMessage(obtainMessage(TASK_DISPLAY,
                                      primeNumbers));
        }
    }

    public void displayError(@NonNull Throwable throwable) {
        removeMessages(TASK_SHOW_ERROR);

        synchronized (this.throwables) {
            this.throwables.add(throwable);
        }

        if (display != null) {
            sendMessage(obtainMessage(TASK_SHOW_ERROR));
        }
    }

    public void setDisplay(@Nullable ComposeDisplay display) {
        this.display = display;

        removeMessages(TASK_DISPLAY);
        removeMessages(TASK_SHOW_ERROR);

        if (display != null && !primeNumbers.isEmpty()) {
            sendMessage(obtainMessage(TASK_DISPLAY,
                                      primeNumbers));
        }

        if (display != null && !throwables.isEmpty()) {
            sendMessage(obtainMessage(TASK_SHOW_ERROR));
        }
    }
}
