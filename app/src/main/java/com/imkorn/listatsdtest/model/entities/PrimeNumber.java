package com.imkorn.listatsdtest.model.entities;

import java.io.Serializable;

/**
 * Created by imkorn on 22.09.17.
 */

public class PrimeNumber implements Serializable {
    private final int threadId;
    private final int number;

    private PrimeNumber(int threadId,
                       int number) {
        this.threadId = threadId;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public int getThreadId() {
        return threadId;
    }

    public static PrimeNumber create(int threadId,
                                     int number) {
        return new PrimeNumber(threadId, number);
    }
}
