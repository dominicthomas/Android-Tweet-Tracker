package com.myapp.projectzero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class TweetMapView extends MapActivity implements LocationListener, AsyncTaskCompleteListener<String> {
	
	public MapView mapView;
	private MapController mapController;
	public int tweet_count = 200; 
    public int lat;
    public int lon;
    private ViewGroup popupParent;
    private View popup; 
    
	// Original onCreate Method
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // connect with the map xml layout
        setContentView(R.layout.map);
        
        // get location coords from intent data, split into array
        String[] coords = getIntent().getStringExtra("location").split(",");
        String user_name = getIntent().getStringExtra("from_user_name");
        String profile_image_url = getIntent().getStringExtra("profile_image_url");
        String text = getIntent().getStringExtra("text");
        
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
        
        // set up a temp drawable
        Drawable drawable = this.getResources().getDrawable(R.drawable.icon);
        
        
        // MAP OVERLAYS: GET OBJECT INFO AND BUILD OVERLAY 
        
        // get the map view overlays
        List<Overlay> mapOverlays = mapView.getOverlays();
        
        // create a new tweet map overlay with the temp drawable
        //TweetMapOverlay tweetMapOverlay = new TweetMapOverlay(drawable, this);
        TweetMapOverlay tweetMapOverlay = new TweetMapOverlay(drawable, this, user_name, profile_image_url, text);
        
        // set up a new overlay item using the geo point passed in with the intent
        OverlayItem overlayitem = new OverlayItem(location, "Hello", "I'm a Tweet!");
        
        // add the overlay item to the tweet map overlay list
        tweetMapOverlay.addOverlay(overlayitem);
        
        // add the tweet map overlay to the mapview overlays
        mapOverlays.add(tweetMapOverlay);
        
        //
        
        
                
        // set up map options
        mapView.setBuiltInZoomControls(true);             
        mapController = mapView.getController();
        mapController.setCenter(location);
        mapController.setZoom(15);
        
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
	
	public class TweetMapOverlay extends ItemizedOverlay<OverlayItem> {

		// setup the overlay item array list to store the overlays
		private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();
	   
		// setup the context ready to be passed in when called overlayitems from the mapview
		private Context context;
		public String user_name;
		public String profile_image_url;
		public String text;
				
		// constructor with just the drawable
		public TweetMapOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}
	   
		// constuctor with the drawable and the context
		public TweetMapOverlay(Drawable defaultMarker, Context context) {
	        this(defaultMarker);
	        //set the context
	        this.context = context;
		}
		
		// constuctor with the drawable and the context
		public TweetMapOverlay(Drawable defaultMarker, Context context, String user_name, String profile_image_url, String text) {
	        this(defaultMarker);
	        //set the context
	        this.context = context;
	        this.user_name = user_name;
	        this.profile_image_url = profile_image_url;
	        this.text = text;
		}		
		
		// used to add overlays to the arraylist and populate
		public void addOverlay(OverlayItem overlay) {
			mapOverlays.add(overlay);
			this.populate();
		}	

		@Override // create the items
		protected OverlayItem createItem(int i) {
	      return mapOverlays.get(i);
		}	
		
		public void removeOverlay(int i) {
			mapOverlays.remove(i);
			this.populate();
		}		

		@Override // get the number of overlays
		public int size() {
	      return mapOverlays.size();
		}
	   
		@Override // set the ontap event for the overlays
		protected boolean onTap(int index) {	
			// get the relevant overlay
			//OverlayItem item = mapOverlays.get(index);
			
			// get the geopoint of the item clicked and pass to addPopup
			addPopup(index);
			
			/*
			// set up a dialog and pass in the context
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			
			// set the dialog parameters and show
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			*/
			
			return true;
		}
		
		public void addPopup(int index){
			
			// hide any existing popups
			hidePopup();
			
			if(popup == null){
				
				// get the item at said index
				OverlayItem item = mapOverlays.get(index);
				
				// get the map viewgroup
				popupParent = (ViewGroup) mapView.getParent();
				popup = getLayoutInflater().inflate(R.layout.map_popup, popupParent, false);
				
				((TextView)popup.findViewById(R.id.pop_user_name)).setText(this.user_name);
				((TextView)popup.findViewById(R.id.pop_tweet)).setText(this.text);
				
				// set image drawable from url
				try {
					  ImageView i = (ImageView)popup.findViewById(R.id.pop_thumbnail);
					  Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(this.profile_image_url).getContent());
					  i.setImageBitmap(bitmap); 
				} catch (MalformedURLException e) {
					  e.printStackTrace();
				} catch (IOException e) {
					  e.printStackTrace();
				}				
				
				//MapView.LayoutParams(int width, int height, GeoPoint point, int x, int y, int alignment) 
				
				// setup layout parameters
				MapView.LayoutParams mvlp = new MapView.LayoutParams(
	                    LayoutParams.WRAP_CONTENT, // width
	                    LayoutParams.WRAP_CONTENT, // height
	                    item.getPoint(), // geo point
	                    0, // x
	                    -85, // y
	                    MapView.LayoutParams.BOTTOM_CENTER // alignment
	            ); 						
				
				// setup click listener to hide view
				popup.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mapView.removeView(popup);
						popup = null;
					}
				});
				
				mapView.addView(popup, mvlp);
				mapController.animateTo(item.getPoint());
			}
		}
		
		public void hidePopup(){
			if(popup != null){
				mapView.removeView(popup);
				popup = null;
			}
		}
	   
	}
		
}
