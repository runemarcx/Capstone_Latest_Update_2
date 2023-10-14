package com.hcdc.capstone.transactionprocess;

public class TaskCompleteData
{
    String taskName,location,points;
    Boolean isConfirmed;

    public  TaskCompleteData()
    {
        //for firebase
    }

    public TaskCompleteData( String taskName,String location,String points, Boolean isConfirmed)
    {
        this.taskName = taskName;
        this.location = location;
        this.points = points;
        this.isConfirmed = isConfirmed;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getLocation() {
        return location;
    }

    public String getPoints() {
        return points;
    }

    public Boolean getConfirmed() {
        return isConfirmed;
    }
}
