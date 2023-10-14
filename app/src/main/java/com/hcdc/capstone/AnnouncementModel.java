package com.hcdc.capstone;

import java.util.Date;

public class AnnouncementModel {

    private String description;
    private String title;
    private Date timestamp;

    public AnnouncementModel(){
        //for firebase
    }

    public AnnouncementModel(String description, String title, Date timestamp) {
        this.description = description;
        this.title = title;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
