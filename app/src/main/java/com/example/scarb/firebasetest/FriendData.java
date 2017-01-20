package com.example.scarb.firebasetest;

public class FriendData {
    private String username;
    private String photoID;
    private boolean pending;
    public FriendData(String username, String photoID, boolean pending){
        this.username = username;
        this.photoID = photoID;
        this.pending = pending;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPhotoID(){
        return this.photoID;
    }
    public boolean getPeding(){
        return pending;
    }
}
