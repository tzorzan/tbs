package it.tzorzan.tbs.model;

import java.util.List;

public class Status {
    public String state;
    public List<String> queue;
    public Integer countdown;

    public Status(String state, List<String> queue, Integer countdown) {
        this.state = state;
        this.queue = queue;
        this.countdown = countdown;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getQueue() {
        return queue;
    }

    public void setQueue(List<String> queue) {
        this.queue = queue;
    }

    public Integer getCountdown() {
        return countdown;
    }

    public void setCountdown(Integer countdown) {
        this.countdown = countdown;
    }
}

