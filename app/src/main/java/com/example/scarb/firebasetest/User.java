package com.example.scarb.firebasetest;

public class User {
    public String username;
    public String name;
    public String profileURL;
    public int age;
    public User(){

    }
    public User(String username){
        this.username = username;
    }

    public User(String username, String profileURL){
        this.username = username;
        this.profileURL = profileURL;
    }

    public User(String username, String profileURL,String name){
        this.username = username;
        this.name = name;
    }

    public User(String username, String name, int age){
        this.username = username;
        this.name = name;
        this.age = age;
    }
}
