package com.hcdc.capstone.rewardprocess;

import com.google.firebase.Timestamp;

public class CouponsModel {

    private String rewardName;
    private String userId;
    private boolean pendingStatus;
    private String userEmail;
    private int rewardPoints;
    private  String couponuserCode;
    private Timestamp claimDate;

    public CouponsModel()
    {
        //firebase shizz
    }

    public CouponsModel(String rewardName, String userId, boolean pendingStatus, String userEmail, int rewardPoints, String couponuserCode)
    {
        this.rewardName = rewardName;
        this.userId = userId;
        this.pendingStatus = pendingStatus;
        this.userEmail = userEmail;
        this.rewardPoints = rewardPoints;
        this.couponuserCode = couponuserCode;
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

    public String getUserEmail() {
        return userEmail;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public String getCouponuserCode() {return couponuserCode;}

    public Timestamp getClaimDate() { return claimDate;}
}
