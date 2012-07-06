package com.myapp.projectzero;

import com.google.android.maps.GeoPoint;


public class Tweet {
	String created_at;
	String from_user_name;
	String from_user;
	String profile_image_url;
	String text;
	GeoPoint location = null;

	public Tweet(String created_at, String from_user_name, String from_user, String profile_image_url, String text){
		this.created_at = created_at;
		this.from_user_name = from_user_name;
		this.from_user = from_user;
		this.profile_image_url = profile_image_url;
		this.text = text;
	}

	public Tweet(String created_at, String from_user_name, String from_user, String profile_image_url, String text, GeoPoint location){
		this.created_at = created_at;
		this.from_user_name = from_user_name;
		this.from_user = from_user;
		this.profile_image_url = profile_image_url;
		this.text = text;
		this.location = location;
	}
	
    public String getCreatedAt() {
        return created_at;
    }

    public String getUserName() {
        return from_user_name;
    }

    public String getUser() {
        return from_user;
    }    
    
    public String getProfileImageUrl() {
        return profile_image_url;
    }
    
    public String getText() {
        return text;
    }
    
    public GeoPoint getGeo(){
    	return location;
    }
}
