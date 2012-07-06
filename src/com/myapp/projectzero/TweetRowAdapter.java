package com.myapp.projectzero;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetRowAdapter extends ArrayAdapter<Tweet> {

	private ArrayList<Tweet> tweets;
	private Context context;
    private final LinkedHashMap<String, Bitmap> bitmapCache = new LinkedHashMap<String, Bitmap>();	
	
	// adapter constructor
    public TweetRowAdapter(Context context, int textViewResourceId, ArrayList<Tweet> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.tweets = items;
    }
    
    // class to holder a reference to the required views
    private static class ViewHolder {
    	private TextView user_name;
    	private TextView tweet;
    	private TextView created;
    	private ImageView thumbnail;
    	private BitmapDownloaderTask task;
    }
    
    @Override
    public View getView(int position, View view, ViewGroup parent) {
    		// if view is null inflate the required custom layout - must reference the context passed in by the constructor
    	ViewHolder h;
    	
            if (view == null) {
            	
            	// inflate the required view
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.tweet_row, null);

                // setup view holder to stop having to do a findViewById each time
                h = new ViewHolder();
                
                // set tag to cancel asynctask later on
                view.setTag(h);
                
                // assign required views to the holder class
                h.user_name = (TextView) view.findViewById(R.id.user_name);
                h.tweet = (TextView) view.findViewById(R.id.tweet);
                h.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
                h.created = (TextView) view.findViewById(R.id.created);      
    
            }else{
            	// get the task from the view and cancel it
            	h = (ViewHolder) view.getTag(); 
            	if(h.task != null){
            		h.task.cancel(true);
            	}
            }
                        
            // get current item at position
            Tweet this_tweet = tweets.get(position);
            
            // rest background color?
            view.setBackgroundColor(Color.rgb(0, 0, 0));
        	h.user_name.setTextColor(Color.rgb(255, 255, 255));
        	h.tweet.setTextColor(Color.rgb(255, 255, 255));
        	h.created.setTextColor(Color.rgb(255, 255, 255));
            
            if (this_tweet != null) {
            	
            	// get the items we want to populate
            	
                    // set the items if the content is not null                    
                    if (h.user_name != null) {
                    	h.user_name.setText(this_tweet.getUserName());                            
                    }
                    
                    // tweet
                    if(h.tweet != null){
                    	h.tweet.setText(this_tweet.getText());
                    }

                    // timestamp
                    if(h.created != null){
                    	h.created.setText(this_tweet.getCreatedAt());
                    }                    

                    
                    /*if(h.thumbnail != null){
                    	// parse the url as a new Uri
                    	//thumbnail.setImageURI(Uri.parse(this_tweet.getProfileImageUrl()));
                    	BitmapDownloaderTask task = new BitmapDownloaderTask(h.thumbnail);
                    	// tag the view with the task
                    	h.task = task;
                    	
                    	try {                    		
							task.execute(new URL(this_tweet.getProfileImageUrl()));
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }*/ 
                    
                    // reset image view bitmap
                	h.thumbnail.setImageBitmap(null);                        
                    
                    // set local url to store url
                    String img_url = this_tweet.getProfileImageUrl();
                    
                    // check if url exists
                    if (img_url != null) {
                    	
                    	// attempt to fetch the bitmap from the cache using the url
                        Bitmap bitmap = fetchBitmapFromCache(img_url);
                        
                        // if the call returns null, kick off an async download task
                        if (bitmap == null) {
                        	
                        	// start new async task and pass in a reference to the image view
                        	BitmapDownloaderTask task = new BitmapDownloaderTask(h.thumbnail);
                        	
                        	// this is used for tagging to stop the async task later on
                        	h.task = task;
                        	
                        	try {
                        		// execute the task and pass in the url to download
    							task.execute(new URL(img_url));
    						} catch (MalformedURLException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}                        	
                        }
                        else {
                        	// if not null, set the bitmap retrieved from the cache
                        	h.thumbnail.setImageBitmap(bitmap);
                        }
                    }
                    else {
                    	// if no url, set the bitmap to null
                    	h.thumbnail.setImageBitmap(null);
                    	// TODO: load in placeholder?                    	
                    }
                    
                    
                    if(this_tweet.getGeo() != null){
                    	h.user_name.setTextColor(Color.rgb(0, 0, 0));
                    	h.tweet.setTextColor(Color.rgb(0, 0, 0));
                    	h.created.setTextColor(Color.rgb(0, 0, 0));
                    	view.setBackgroundColor(Color.argb(255, 51, 161, 201));
                    	//view.getBackground().setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.DARKEN);
                    }
                    
            }                      
            
            return view;
    }        

    // function to add a url and the related bitmap to the linked hash map
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (bitmapCache) {
                bitmapCache.put(url, bitmap);
            }
        }
    }
    
    // function to get the bitmap from the cache if it exists - called should check for null
    private Bitmap fetchBitmapFromCache(String url) {
        synchronized (bitmapCache) {
            final Bitmap bitmap = bitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in cache
                // Move element to first position, so that it is removed last
                bitmapCache.remove(url);
                bitmapCache.put(url, bitmap);
                return bitmap;
            }
        }
        return null;
    }        
    
    public class BitmapDownloaderTask extends AsyncTask<URL, Void, Bitmap> {
    	
    	private final ImageView imageViewReference;
    	private String str_url;
    	
    	// constructor to store reference to the imageview
    	public BitmapDownloaderTask(ImageView imageView){
    		imageViewReference = imageView;
    	}
    	
    	@Override
		protected Bitmap doInBackground(URL... urls) {
    		
    		// make url object out of incoming url
    		URL url = urls[0];
    		
    		// store string reference to the url
    		str_url = urls[0].toString();
    		
    		try{
    			URLConnection connection = url.openConnection();
    			connection.setUseCaches(true);
    			return BitmapFactory.decodeStream(connection.getInputStream());
    		} catch (IOException e) {
    			e.printStackTrace();
    			return null;
    		}
		}
    	
    	@Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
    		
    		// add the bitmap to the hash map using the url as a reference
    		addBitmapToCache(str_url, bitmap);
    		
    		imageViewReference.setImageBitmap(bitmap);
        }    	  	
    }  

}
