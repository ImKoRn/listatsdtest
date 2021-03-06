package com.imkorn.listatsdtest.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;

import com.imkorn.listatsdtest.model.entities.Interval;
import com.imkorn.listatsdtest.model.entities.PrimeNumber;
import com.imkorn.listatsdtest.parser.Factory;
import com.imkorn.listatsdtest.parser.elements.XmlElement;
import com.imkorn.listatsdtest.parser.elements.XmlGroup;
import com.imkorn.listatsdtest.parser.elements.XmlObject;
import com.imkorn.listatsdtest.parser.exceptions.ParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by imkorn on 22.09.17.
 */
public class PrimeNumberSearchHelper {
    public static final String SRC_FOLDER = "intervals";

    // Worker
    private final WorkerThread workerThread;
    private volatile SearchHandler searchHandler = null;

    @NonNull
    private final ErrorDisplay display;
    private Aggregator aggregator;

    private volatile boolean closed;

    public PrimeNumberSearchHelper(@NonNull String name,
                                   @NonNull ErrorDisplay display,
                                   @NonNull Aggregator aggregator) {
        this.display = display;
        this.aggregator = aggregator;
        workerThread = new WorkerThread(name);
        workerThread.start();
    }

    public void findIn(Collection<Interval> intervals) {
        if (closed) {
            return;
        }

        synchronized (this) {
            if (closed) {
                return;
            }

            getSearchHandler().findIn(intervals);
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
            searchHandler.close();
            aggregator.close();
            workerThread.quitSafely();
        }
    }

    public void parseAndFind(String xml) {
        if (closed) {
            return;
        }

        synchronized (this) {
            if (closed) {
                return;
            }

            getSearchHandler().parseAndFind(xml);
        }
    }

    private SearchHandler getSearchHandler() {
        SearchHandler snapshot = this.searchHandler;
        if (snapshot == null) {
            synchronized (workerThread) {
                for (;;) {
                    snapshot = this.searchHandler;
                    if (snapshot != null) {
                        break;
                    }
                    try {
                        workerThread.wait();
                    } catch (InterruptedException e) {
                        display.displayError(e);
                    }
                }
            }
        }

        return snapshot;
    }

    private static class SearchHandler extends Handler {
        // Tasks
        private static final int TASK_PROCESS_INTERVALS = 1;
        private static final int TASK_PARSE_AND_FIND = 4;

        // Processor
        private final ExecutorService executor;
        {
            // Nice approach for performance
            // Runtime.getRuntime().availableProcessors()
            // but now checking multithreading

            final int count = 5;
            executor = Executors.newFixedThreadPool(count);
        }

        // Aggregator
        private Aggregator aggregator;

        private final ErrorDisplay display;

        // State
        private volatile boolean closed;

        private SearchHandler(Aggregator aggregator,
                              ErrorDisplay display) {
            this.display = display;
            this.aggregator = aggregator;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TASK_PROCESS_INTERVALS: {
                    startHunt((Collection<Interval>) msg.obj);
                    break;
                }
                case TASK_PARSE_AND_FIND: {
                    try {
                        XmlElement<XmlGroup<XmlObject>> root = new XmlElement<>("root",
                                             (String) msg.obj);

                        final Factory<String, XmlGroup<XmlObject>> xmlIntervalsFactory =
                                new Factory<String, XmlGroup<XmlObject>>() {
                            @Override
                            public XmlGroup<XmlObject> create(String s) throws
                                                                        ParseException {
                                return new XmlGroup<>("intervals",
                                                      s);
                            }
                        };

                        final XmlGroup<XmlObject> intervalsXml = root.getValue(xmlIntervalsFactory);


                        final List<Interval> intervals = new ArrayList<>(intervalsXml.size());

                        final Factory<String, XmlObject> intervalFactory =
                                new Factory<String,XmlObject>() {
                                    @Override
                                    public XmlObject create(String s) throws
                                                                      ParseException {
                                        final XmlObject interval = new XmlObject("interval",
                                                                                 s);

                                        final int id = interval.getValueAsInt("id");
                                        final int from = interval.getValueAsInt("low");
                                        final int to = interval.getValueAsInt("high");

                                        intervals.add(Interval.create(id, from, to));
                                        return interval;
                                    }
                                };

                        intervalsXml.getValues(intervalFactory);

                        removeMessages(TASK_PROCESS_INTERVALS);
                        startHunt(intervals);
                    } catch (ParseException e) {
                        display.displayError(e);
                    }
                    break;
                }
            }
        }

        private void startHunt(Collection<Interval> intervals) {
            for (Interval interval : intervals) {
                executor.execute(new PrimeNumberHunter(interval));
            }
        }

        private void findIn(@NonNull Collection<Interval> intervals) {
            sendMessage(obtainMessage(TASK_PROCESS_INTERVALS, intervals));
        }

        private void close() {
            closed = true;
            executor.shutdownNow();
            removeCallbacksAndMessages(null);
        }

        private void parseAndFind(String xml) {
            sendMessage(obtainMessage(TASK_PARSE_AND_FIND, xml));
        }

        private class PrimeNumberHunter implements Runnable {
            private final Interval interval;

            private PrimeNumberHunter(@NonNull Interval interval) {
                this.interval = interval;
            }

            @Override
            public void run() {
                for (int number = interval.getFrom() + 1;
                     number < interval.getTo() && !closed;
                     number++) {
                    if (isPrimeNumber(number)) {
                        aggregator.push(PrimeNumber.create(interval.getId(),
                                                           number));
                    }
                }
            }

            private boolean isPrimeNumber(int number) {
                if (number <= 1) {
                    return false;
                }

                if (number % 2 == 0)  {
                    return false;
                }

                for(int index = 3; index * index <= number; index += 2) {
                    if(number % index == 0) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    private class WorkerThread extends HandlerThread {
        private WorkerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            synchronized (this) {
                searchHandler = new SearchHandler(aggregator, display);
                notify();
            }
        }
    }
}
