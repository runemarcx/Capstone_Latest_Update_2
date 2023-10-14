package com.hcdc.capstone;

import androidx.lifecycle.ViewModel;

public class TimerViewModel extends ViewModel {
    private long startTime;
    private boolean timerRunning;
    private boolean doneButtonClicked;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isTimerRunning() {
        return timerRunning;
    }

    public void setTimerRunning(boolean timerRunning) {
        this.timerRunning = timerRunning;
    }

    public TimerViewModel() {
        this.doneButtonClicked = false;
    }

    public boolean isDoneButtonClicked() {
        return doneButtonClicked;
    }

    public void setDoneButtonClicked(boolean doneButtonClicked) {
        this.doneButtonClicked = doneButtonClicked;
    }

    public boolean isTimerRunningAndNotFinished(long taskDurationMillis) {
        if (timerRunning) {
            long millis = System.currentTimeMillis() - startTime;
            long remainingMillis = taskDurationMillis - millis;
            return remainingMillis > 0;
        }
        return false;
    }
}

