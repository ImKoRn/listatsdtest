package com.imkorn.listatsdtest.model.entities;

/**
 * Created by imkorn on 23.09.17.
 */

public class Interval {
    private final int id;

    private final int from;

    private final int to;

    private Interval(int id,
                    int from,
                    int to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public int getId() {
        return id;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public static Interval create(int id,
                           int from,
                           int to) {
        return new Interval(id, from, to);
    }
}
