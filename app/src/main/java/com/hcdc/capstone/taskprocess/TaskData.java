package com.hcdc.capstone.taskprocess;

public class TaskData {

    String taskName, description, points, location;
    private boolean isAccepted;

    int hours, minutes;

    // Add public no-argument constructor
    public TaskData() {
        // Default constructor required by Firestore
    }

    public TaskData(String taskName, String description, String points, String location, boolean isAccepted, int hours, int minutes) {
        this.taskName = taskName;
        this.description = description;
        this.points = points;
        this.location = location;
        this.isAccepted = isAccepted;
        this.hours = hours ;
        this.minutes = minutes;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDescription() {
        return description;
    }

    public String getPoints() {
        return points;
    }

    public String getLocation() {
        return location;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }
}