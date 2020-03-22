package com.rdc.p2p.bean;

public class UserBean {

    private int userImageId;
    private String nickName;

    public UserBean(){

    }


    public int getUserImageId() {
        return userImageId;
    }

    public void setUserImageId(int userImageId) {
        this.userImageId = userImageId;
    }

    public String getNickName() {
        return nickName == null ? "" : nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? "" : nickName;
    }
}
