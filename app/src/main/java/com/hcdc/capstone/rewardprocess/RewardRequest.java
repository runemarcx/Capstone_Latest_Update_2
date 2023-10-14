package com.hcdc.capstone.rewardprocess;

public class RewardRequest {
    private String rewardName;
    private String userId;
    private boolean pendingStatus;
    private String userEmail;
    private int rewardPoints;
    private  String usercouponCode;



    public RewardRequest() {
        // Default constructor required for Firestore
    }

    public RewardRequest(String rewardName, String userId, boolean pendingStatus, String userEmail, int rewardPoints, String usercouponCode) {
        this.rewardName = rewardName;
        this.userId = userId;
        this.pendingStatus = pendingStatus;
        this.userEmail = userEmail;
        this.rewardPoints = rewardPoints;
        this.usercouponCode = usercouponCode;

    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getCouponuserCode() {
        return usercouponCode;
    }

    public String getRewardName() {
        return rewardName;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isPendingStatus() {
        return pendingStatus;
    }

    public int getRewardPoints() { // Fix: Method name should start with lowercase
        return rewardPoints;
    }
}
