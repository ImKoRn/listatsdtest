package com.imkorn.listatsdtest.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.imkorn.listatsdtest.model.entities.PrimeNumber;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import static com.imkorn.listatsdtest.model.SaveDisplay.State.*;

/**
 * Created by imkorn on 24.09.17.
 */

public class SaveDisplay extends Thread implements Display {

    @IntDef({STATE_WAITING_DATA,
            STATE_DISCONNECTED,
            STATE_RECONNECTING,
            STATE_SENDING_DATA,
            STATE_SENT, STATE_TERMINATED,
            STATE_WAITING_CONNECTION})
    public @interface State {
        int STATE_WAITING_DATA = 1;
        int STATE_WAITING_CONNECTION = 2;
        int STATE_RECONNECTING = 3;
        int STATE_SENDING_DATA = 4;
        int STATE_DISCONNECTED = 5;
        int STATE_SENT = 6;
        int STATE_TERMINATED = 7;
    }

    private BlockingQueue<Collection<PrimeNumber>> queue = new LinkedBlockingQueue<>();

    @NonNull
    private final Publisher publisher;

    private final TcpSocket tcpSocket = new TcpSocket();

    private volatile boolean closed;

    private volatile int state = STATE_WAITING_DATA;

    public SaveDisplay(@NonNull Looper looper) {
        publisher = new Publisher(looper);
        start();
    }

    @Override
    public void display(@NonNull Collection<PrimeNumber> primeNumbers) {
        if (closed) {
            return;
        }

        synchronized (this) {
            if (closed) {
                return;
            }

            try {
                queue.put(primeNumbers);
            } catch (InterruptedException e) {
                displayError(e);
            }

            publisher.display(primeNumbers);
        }
    }

    @Override
    public void displayError(@NonNull Throwable throwable) {
        if (closed) {
            return;
        }

        synchronized (this) {
            if (closed) {
                return;
            }
            publisher.displayError(throwable);
        }
    }

    private void publishState(@State int state) {
        if (closed) {
            return;
        }

        synchronized (this) {
            if (closed) {
                return;
            }
            this.state = state;
            publisher.displayState(state);
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
            interrupt();
            queue.clear();
            publisher.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void run() {
        for (;;) {
            final Collection<PrimeNumber> data;
            try {
                publishState(STATE_WAITING_DATA);
                data = queue.take();
            } catch (InterruptedException e) {
                displayError(e);
                break;
            }

            tcpSocket.reset();

            boolean disconnected = false;
            for (;;) {
                try {
                    if (disconnected) {
                        publishState(STATE_RECONNECTING);
                    } else {
                        publishState(STATE_WAITING_CONNECTION);
                    }
                    tcpSocket.connectWithServer();

                    publishState(STATE_SENDING_DATA);
                    tcpSocket.write(data);
                    publishState(STATE_SENT);
                    break;
                } catch (IOException e) {
                    disconnected = true;
                    publishState(STATE_DISCONNECTED);
                }
            }
        }
        publishState(STATE_TERMINATED);
    }

    public void setDisplay(@Nullable Display display) {
        publisher.setDisplay(display);
    }

    public void setEventListener(@Nullable EventListener eventListener) {
        publisher.setEventListener(eventListener);
    }

    public TcpSocket getTcpSocket() {
        return tcpSocket;
    }

    @State
    public int getSendingState() {
        return state;
    }

    private class Publisher extends Handler {

        private static final int TASK_DISPLAY = 1;
        private static final int TASK_SHOW_ERROR = 2;
        private static final int TASK_SHOW_STATE = 3;

        @Nullable
        private volatile Display display;

        @Nullable
        private volatile EventListener eventListener;

        private final AtomicReference<Collection<PrimeNumber>> dataRef = new AtomicReference<>();

        private final AtomicReference<Throwable> throwableRef = new AtomicReference<>();

        private Publisher(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case TASK_DISPLAY: {
                    final Display display = this.display;
                    final Collection<PrimeNumber> data = (Collection<PrimeNumber>) msg.obj;
                    if (display != null &&
                        this.dataRef.compareAndSet(data, null)) {
                        display.display(data);
                    }
                    break;
                }
                case TASK_SHOW_ERROR: {
                    final Display display = this.display;
                    final Throwable throwable = (Throwable) msg.obj;
                    if (display != null &&
                        this.throwableRef.compareAndSet(throwable, null)) {
                        display.displayError(throwable);
                    }
                    break;
                }
                case TASK_SHOW_STATE: {
                    final EventListener eventListener = this.eventListener;

                    if (eventListener != null) {
                        @State int state = msg.arg1;
                        eventListener.onStateChange(state);
                    }
                    break;
                }
            }
        }

        private void display(Collection<PrimeNumber> primeNumbers) {
            removeMessages(TASK_DISPLAY);
            dataRef.set(primeNumbers);
            sendMessage(obtainMessage(TASK_DISPLAY, primeNumbers));
        }

        private void displayState(@State int state) {
            sendMessage(obtainMessage(TASK_SHOW_STATE, state, 0));
        }

        private void displayError(Throwable throwable) {
            removeMessages(TASK_SHOW_ERROR);
            throwableRef.set(throwable);
            sendMessage(obtainMessage(TASK_SHOW_ERROR, throwable));
        }

        private void setDisplay(@Nullable Display display) {
            removeMessages(TASK_DISPLAY);
            this.display = display;
            final Collection<PrimeNumber> primeNumbers = dataRef.get();
            if (display != null &&
                primeNumbers != null) {
                sendMessage(obtainMessage(TASK_DISPLAY, primeNumbers));
            }
        }

        private void setEventListener(@Nullable EventListener eventListener) {
            removeMessages(TASK_SHOW_ERROR);
            this.eventListener = eventListener;
            final Throwable throwable = this.throwableRef.get();
            if (eventListener != null &&
                throwable != null) {
                sendMessage(obtainMessage(TASK_SHOW_ERROR, throwable));
            }
        }
    }

    public interface EventListener {
        void onStateChange(@State int state);
    }
}
