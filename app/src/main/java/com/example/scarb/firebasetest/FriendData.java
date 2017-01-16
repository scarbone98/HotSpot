package com.example.scarb.firebasetest;

public class FriendData {
    private String username;
    private String photoID;
    public FriendData(String username, String photoID){
        this.username = username;
        this.photoID = photoID;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPhotoID(){
        return this.photoID;
    }
}
