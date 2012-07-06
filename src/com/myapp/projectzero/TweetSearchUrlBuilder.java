package com.myapp.projectzero;

import java.net.URLEncoder;

public class TweetSearchUrlBuilder {
	
	String url;
	public final String baseurl = "http://search.twitter.com/search.json";
	

	public TweetSearchUrlBuilder(String query, double lat, double lon, int radius, int rpp, int page, boolean show_user, boolean include_entities, String result_type, String lang) {

		//return "http://search.twitter.com/search.json?q=bus&geocode=51.513425,-0.127168,20mi&rpp=20&page=1&show_user=true&include_entities=true&result_type=recent";
		
		// set up string builder
		StringBuilder url = new StringBuilder();
		
		// append base url with the ?
		url.append(baseurl + "?");
		
		// check query and append encoded to url
		if(notEmpty(query)){
			url.append("q=" + URLEncoder.encode(query) + "&");
		}
		
		// append lat, lon and radius to the url
		url.append("geocode=" + lat + "," + lon);
		
		// check radius and add to lat and lon
		if(radius > 0){
			url.append("," + radius + "mi&" );
		}else{
			// not radius, end lat and lon query
			url.append("&");
		}

		// check the results per page
		if(rpp > 0){
			url.append("rpp=" + rpp + "&");
		}
		
		// check page number
		if(page > 0){
			url.append("page=" + page + "&");
		}
		
		// set to show user or not
		url.append("show_user=" + show_user + "&");
		
		// set to include entities or not
		url.append("include_entities=" + include_entities + "&");	
		
		// check result type
		if(result_type != null){
			if(result_type == "mixed" || result_type == "recent" || result_type == "popular"){
				url.append("result_type=" + result_type + "&");
			}
		}
		
		if(lang != null){
			url.append("lang=" + lang);
		}
		
		this.url = url.toString();
		
	}
	
	// return the url
	public String getUrl(){		
		return url;	
	}
	
	public static boolean notEmpty(String s) {
		return (s != null && s.length() > 0);
	}		
}
