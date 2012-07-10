package com.myapp.projectzero;

public class Location {

	private int id;
	private String name;
	private String lat;
	private String lon;
	
	public Location(){
		// empty
	}
	
	public Location(int id, String name, String lat, String lon){
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
	}
	
	public Location(String name, String lat, String lon){
		this.name = name;
		this.lat = lat;
		this.lon = lon;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getLat(){
		return this.lat;
	}

	public String getLon(){
		return this.lon;
	}
	
	public void setLat(String lat){
		this.lat = lat;
	}
	
	public void setLon(String lon){
		this.lon = lon;
	}	
}
