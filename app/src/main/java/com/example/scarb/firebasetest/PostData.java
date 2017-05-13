package com.example.scarb.firebasetest;

/**
 * Created by scarbone on 5/11/17.
 */

public class PostData {
    private String postData;
    private String photoID;
    private String userName;
    public PostData(String username, String postData){
        this.postData = postData;
        this.userName = username;
    }
    public String getPostData(){
        return this.postData;
    }
    public String getUserName(){
        return this.userName;
    }
}
