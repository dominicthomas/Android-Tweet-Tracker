package com.myapp.projectzero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.myapp.projectzero.TweetListView.GetTweetsTask;

public class TweetMapView extends MapActivity implements LocationListener, AsyncTaskCompleteListener<String> {
	
	private MapView mapView;
	private MapController mapController;
	public int tweet_count = 200; 
    public int lat;
    public int lon;
	
	// Original onCreate Method
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // connect with the map xml layout
        setContentView(R.layout.map);
        
        // get location coords from intent data, split into array
        String[] coords = getIntent().getStringExtra("location").split(","); 
        
        // create a GeoPoint from strings passed in to this activity
        GeoPoint location = new GeoPoint(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));        

		if(isNetworkAvailable()){
			
			// build the initial url
			TweetSearchUrlBuilder url = new TweetSearchUrlBuilder(null, Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), 20, tweet_count, 1, true, true, "recent", "en");
			
			// kick off async http request
			new GetTweetsTask(TweetMapView.this).execute(url.getUrl());
			
		}else{
			Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_LONG).show();
		}
		
		
		
		
        // get the map view
        mapView = (MapView) findViewById(R.id.map_view);
		
        // get the map view overlays
        List<Overlay> mapOverlays = mapView.getOverlays();

        // set up a temp drawable
        Drawable drawable = this.getResources().getDrawable(R.drawable.icon);
        
        // create a new tweet map overlay with the temp drawable
        TweetMapOverlay tweetMapOverlay = new TweetMapOverlay(drawable, this);
        
        // set up a new overlay item using the geo point passed in with the intent
        OverlayItem overlayitem = new OverlayItem(location, "Hello", "I'm a Tweet!");
        
        // add the overlay item to the tweet map overlay list
        tweetMapOverlay.addOverlay(overlayitem);
        
        // add the tweet map overlay to the mapview overlays
        mapOverlays.add(tweetMapOverlay);
                
        // set up map options
        mapView.setBuiltInZoomControls(true);             
        mapController = mapView.getController();
        mapController.setCenter(location);
        mapController.setZoom(16);
        
    }   
	
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onTaskComplete(String result) {
		// TODO Auto-generated method stub
		
	}

	public void onResultObtained(String result){
		
		// parse the json and get the required fields
		try {
	    	// setup the main json object
	    	JSONObject json = new JSONObject(result);
	    	
	    	// get the json array called results
	    	JSONArray results = json.getJSONArray("results");    		
	
			for(int i=0; i < results.length(); i++){
				
				// get the contents of results as json objects
				JSONObject jsonObject = results.getJSONObject(i);
				
				String created_at = jsonObject.getString("created_at");
				String from_user_name = jsonObject.getString("from_user_name");
				String from_user = jsonObject.getString("from_user");
				String profile_image_url = jsonObject.getString("profile_image_url");
				String text = jsonObject.getString("text");
				String geo = jsonObject.getString("geo");
				String location = jsonObject.getString("location");			
				
			}	
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			Toast.makeText(getApplicationContext(), "Tweet issues! Try again.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			// go back from this activity ?
			super.onBackPressed();			
		} 

	}
	
	public boolean isNetworkAvailable() {		
		
		// get the connectivity manager
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    
	    // get the active network info
	    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
	    
	    // return true or false depending on the response
	    if (networkInfo != null && networkInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}	

	class GetTweetsTask extends AsyncTask<String, String, String>{
		
		// set up async task to do http get calls in seperate thread
		
		// setup vars for activity, dialog and context
		private Activity activity;
		private ProgressDialog dialog;
		private Context context;
		boolean appendnew = false;
		
		// constructor to pass in activity and set context for dialog
		public GetTweetsTask(Activity activity){
			this.activity = activity;
			context = activity;
			dialog = new ProgressDialog(context);
		}
		
	    @Override
	    protected String doInBackground(String... urls) {
	    	
	    	// set up required http request objects
	        HttpClient httpclient = new DefaultHttpClient();
	        String responseString = null;
	        
	        try {
	        	HttpGet httpget = new HttpGet(urls[0]);
	        	HttpResponse execute = httpclient.execute(httpget);
	            StatusLine statusLine = execute.getStatusLine();
	            	            
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	            	
	            	// get the http entity?
	            	HttpEntity entity = execute.getEntity();
	            	
	            	// set response string to the response of the inputStreamToString
	            	responseString = inputStreamToString(entity.getContent()).toString();	            	            		            	

	            } else{
	                //Closes the connection.
	                execute.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	        	e.printStackTrace();
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }catch (IllegalStateException e){
	        	e.printStackTrace();
	        }
	        
	        return responseString;

	    }
	    
	    // convert the input stream to a string
	    private StringBuilder inputStreamToString(InputStream inputstream) throws IOException {
	        StringBuilder sbuilder = new StringBuilder();
	        String line = "";
	        
	        // wrap a BufferedReader around the InputStream
	        BufferedReader rd = new BufferedReader(new InputStreamReader(inputstream));

	        // read response until the end
	        while ((line = rd.readLine()) != null) { 
	        	sbuilder.append(line); 
	        }
	        
	        // return full string
	        return sbuilder;
	    }	    
	    
	    @Override
	    protected void onPreExecute() {
	    	// show loader
	        this.dialog.setMessage("Loading tweets...");
	        this.dialog.show();
	    };
	    
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        
	        // hide loader
	        if (dialog.isShowing()) {
                dialog.dismiss();
            }
	        
	        // pass result back into the tweet list activity
	        TweetMapView.this.onResultObtained(result);
	    }
	    
	    @Override
	    protected void onCancelled() {
	        // hide loader in case of async task being cancelled. on post execute may not run and everything will break!
	        if (dialog.isShowing()) {
                dialog.dismiss();
            }
	    }
	}
}