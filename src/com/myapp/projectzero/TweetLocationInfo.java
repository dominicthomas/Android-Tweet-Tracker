package com.myapp.projectzero;

import com.google.android.maps.GeoPoint;

public class TweetLocationInfo {

	String name;
	GeoPoint geo;
	double lat;
	double lon;
	
	public TweetLocationInfo(String name, Double lat, Double lon){
		this.name = name;
		this.geo = new LatLonPoint(lat, lon);
		this.lat = lat;
		this.lon = lon;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String getName(){
		return name;
	}
	
	public GeoPoint getLocation(){
		return geo;
	}

	public static final class LatLonPoint extends GeoPoint {
	    public LatLonPoint(double latitude, double longitude) {
	    	// call the super consstructor with the lat and long variables
	        super((int) (latitude * 1E6), (int) (longitude * 1E6));
	    }
	}
}

