package com.accessibility.card.model;

public class TimedPoint {
    public float x;
    public float y;
    public long timestamp;
    public int action;

    public TimedPoint() {
    }


    public TimedPoint(float x, float y, int action) {
        this.x = x;
        this.y = y;
        this.action = action;
        this.timestamp = System.currentTimeMillis();
    }

    public TimedPoint(float x, float y) {
        this.x = x;
        this.y = y;

        this.action = -1;
        this.timestamp = System.currentTimeMillis();
    }

    public float velocityFrom(TimedPoint start) {
        float velocity = distanceTo(start) / (this.timestamp - start.timestamp);
        if (velocity != velocity)
            return 0f;
        return velocity;
    }

    public float distanceTo(TimedPoint point) {
        return (float) Math.sqrt(Math.pow(point.x - this.x, 2) + Math.pow(point.y - this.y, 2));
    }
}