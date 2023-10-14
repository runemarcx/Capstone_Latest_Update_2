package com.hcdc.capstone.rewardprocess;

public class RewardsModel {

    String points, rewardName;

    public RewardsModel() {
        // Default constructor required by Firestore
    }

    public RewardsModel(String points, String rewardName) {
        this.points = points;
        this.rewardName = rewardName;
    }

    public String getPoints() {
        return points;
    }

    public String getRewardName() {
        return rewardName;
    }
}
