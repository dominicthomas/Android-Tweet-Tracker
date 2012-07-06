package com.myapp.projectzero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class TweetListView extends Activity implements AsyncTaskCompleteListener<String> {
		
	public String original_url;
	public String current_url;	
	public ArrayList<Tweet> arrayList;
	public int tweet_count = 20; 
	public int scroll_flag = 0;
	public TweetRowAdapter adapter;
	public String next_page;
	public String refresh_url;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// override the super onCreate method
		super.onCreate(savedInstanceState);
		
		// set the list view
		setContentView(R.layout.list);		
		
		// get the intent data 
		String[] locationinfo = getIntent().getStringExtra("locationinfo").split(","); // [0] Location Name / [1] lat / [2] lon
		
		// call new async task with the url required - check internet connectivity
		if(isNetworkAvailable()){
			
			// build the initial url
			TweetSearchUrlBuilder url = new TweetSearchUrlBuilder(null, Double.parseDouble(locationinfo[1]), Double.parseDouble(locationinfo[2]), 10, tweet_count, 1, true, true, "recent", "en");
			
			// set public current url for access in other methods
			original_url = url.getUrl();
			current_url = original_url;
			
			//new TweetGetterAsyncTask(this).execute();			
//			new TweetGetterAsyncTask(new AsyncTaskCompleteListener<String>() {
//
//				@Override
//				public void onTaskComplete(String result) {
//					// TODO Auto-generated method stub
//					Toast.makeText(TweetListView.this, result, Toast.LENGTH_LONG).show();
//				}
//				
//			}, TweetListView.this, false).execute(current_url);
			
			// kick off async http request
			new GetTweetsTask(TweetListView.this, false).execute(current_url);
			
		}else{
			Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override // inflate specified menu
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.tweetlist_menu, menu);
	    return true;		
	}	
	
	@Override // handle menu clicks
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.refresh_item:
	        	refreshTweets();
	            return true;
	        case R.id.search_item:
	        	searchTweets();
	            return true;	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onResultObtained(String result, boolean appendnew){        
       
		// run garbage collector?
        //System.gc();      
        
        // update list with results
        if(appendnew){
        	// called when adding results to the list
        	addTweetsToArraylist(result, appendnew);
        }else{
        	// called when doing first load or a refresh
            outputResultsToList(result);	
        }
                
    	// reset the onscroll flag
    	scroll_flag = 0;  
	}

	@SuppressWarnings("unchecked")
	public void outputResultsToList(String result){		
    
			// get the list view and setup the list adapter
		
        	// get the list view to populate
    		final ListView tweetlist = (ListView) findViewById(R.id.tweet_list);

    		// remove any existing data?
    		tweetlist.invalidateViews();
    		
    		// set the list content - get from the last configuration instance
    		arrayList = (ArrayList<Tweet>)getLastNonConfigurationInstance();
    		//ArrayList<String> arrayList = new ArrayList<String>();    		
    		
    		// if the last configuration instance is null, create a new tweet array
    		if(arrayList == null){
    			arrayList = new ArrayList<Tweet>();    			
    		}
    		
    		// add the tweets to the arraylist
    		addTweetsToArraylist(result, false);
    		
    		// set the list adapter to our custom adapter
    		adapter = new TweetRowAdapter(TweetListView.this, R.layout.tweet_row, arrayList);
    		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(TweetListView.this, android.R.layout.simple_list_item_1, android.R.id.text1, arrayList);    		
    		
    		// bind to the adapter
    		tweetlist.setAdapter(adapter);
    		
    		// make sure the listview is clickable
    		tweetlist.setClickable(true);
    		
    		
    		final CharSequence[] items = {"Reply to tweet"};
    		
    		tweetlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    			
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int i, long id) {
            		// set up new alert dialog
	                AlertDialog.Builder dialog = new AlertDialog.Builder(TweetListView.this);
	                
	                /*
	                String url_tweet = "No url found...";
	                Pattern patt = Pattern.compile("(?:http://)?.+?(/.+?)?/\\d+/\\d{2}/\\d{2}(/.+?)?/\\w{22}");
	                Matcher matcher = patt.matcher(arrayList.get(i).text.toString()); 
	                if(matcher.matches()){
	                	url_tweet = matcher.group(0);
	                }	                
	                Toast.makeText(getApplicationContext(), url_tweet, Toast.LENGTH_LONG).show();
	                */
	                
	                // set items to dialog and set up the onclick listener 
	                dialog.setItems(items, new DialogInterface.OnClickListener() {
	                    @Override
						public void onClick(DialogInterface dialog, int item) {	 
	                    	switch(item) {
		                        case 0: // show tweets in a list		                        	
		                        	replyToTweet(arrayList.get(i).from_user.toString());
		                            break;
		                        default:
	                    	}
	                    }
	                });
	                
	                // show dialog
	                dialog.show();
	                                	
                	return false;
				}
    		});
    		
    		// setup onscroll listener to get more results when user gets to the bottom of the page
    		tweetlist.setOnScrollListener(new OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
						// TODO: Handle
					}
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
										
					if(firstVisibleItem + visibleItemCount >= totalItemCount && scroll_flag == 0){
						
						// set flag to stop listener running again
						scroll_flag = 1;												
						
						// build the next_url from the current_url
						String new_url = current_url.substring(0, current_url.indexOf("?")) + next_page;
						
						// log the next url to make sure the page number is incrementing
						Log.i("next_page: ", new_url);
						
						// do new call - TODO: get the next page from the current set of twitter data and call that
						new GetTweetsTask(TweetListView.this, true).execute(new_url);						
					}
				}
			});    		
	}
	
	
	public void replyToTweet(String user){
		try{
            			
			// set up intent and attach required extra info
			Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "@" + user + " ");
            intent.setType("text/plain");
            
            // TODO: search through tweet and extra all user names mentioned. Use these in the reply
            
            // set up the package manager to query intents
            final PackageManager pm = getPackageManager();
            final List<?> activityList = pm.queryIntentActivities(intent, 0);
            int len =  activityList.size();
                        
            // loop through to find the required intent action
            for (int i = 0; i < len; i++) {

            	// get the activity list
                final ResolveInfo app = (ResolveInfo) activityList.get(i);
                
                // check for the required intent activity
                if ("com.twitter.android.PostActivity".equals(app.activityInfo.name)) {
                    
                	// get the activity info
                	final ActivityInfo activity = app.activityInfo;
                    
                	// get the component name
                	final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                    
                    // set the intent category
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    
                    // set the intent flags
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    
                    // set the intent component name
                    intent.setComponent(name);
                    
                    // launch the intent
                    startActivity(intent);
                    
                    break;
                }
            }
      }catch(final ActivityNotFoundException e) {
            Log.i("twitter", "no twitter native", e);
            Toast.makeText(getApplicationContext(), "Cannot find Twitter!", Toast.LENGTH_LONG).show();
      }
	}
	
	public void addTweetsToArraylist(String result, boolean appendnew){        
		
		// parse the json and add the tweets to the array list
		
		try {
			
	    	// setup the main json object
	    	JSONObject json = new JSONObject(result);
	    	
	    	// get the json array called results
	    	JSONArray results = json.getJSONArray("results");    		
			
	    	// get the refresh url from the json
	    	refresh_url = json.getString("refresh_url");
	    	
	    	// get the next page field from the twitter json
	    	next_page = json.getString("next_page"); 
	    	
			for(int i=0; i < results.length(); i++){
				
				// get the contents of results as json objects
				JSONObject jsonObject = results.getJSONObject(i);
				
				String created_at = jsonObject.getString("created_at");
				String from_user_name = jsonObject.getString("from_user_name");
				String from_user = jsonObject.getString("from_user");
				String profile_image_url = jsonObject.getString("profile_image_url");
				String text = jsonObject.getString("text");
				
				int hasg = 0;
				GeoPoint location = null;
				
				// check for geo and then for coords, create new location from points
				if(!jsonObject.isNull("geo")){
					JSONObject geo = jsonObject.getJSONObject("geo");
					JSONArray coords = geo.getJSONArray("coordinates");
					
					if(coords.length() > 0){		
						location = new GeoPoint((int)(coords.optDouble(0) * 1E6), (int)(coords.optDouble(1) * 1E6));												
						hasg = 1;
					}	
				}					
				
				
				Tweet tweet;
				// use a different Tweet constructor if a location has been found
				if(hasg == 0){
					// no location
					tweet = new Tweet(created_at, from_user_name, from_user, profile_image_url, text);
				}else{
					// location
					tweet = new Tweet(created_at, from_user_name, from_user, profile_image_url, text, location);
				}
				
				// add the item called text from the json object to the array
				arrayList.add(tweet);
				
				// appendnew is true
				if(appendnew){														
					// tell adapter to update if the appendnew boolean has been set
					adapter.notifyDataSetChanged();
				}
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
	
	public void searchTweets(){
		
		// show text input dialog and re-run the current_url with a q= attached!
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		// set an EditText view to get user input 
		final EditText input = new EditText(this);
		dialog.setView(input);

		dialog.setPositiveButton("Search", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				
				String new_url;
				
				new_url = original_url + "&q=" + Uri.encode(input.getText().toString()); 
				
				/*
				if(current_url.indexOf("&q=") == -1){
					new_url = current_url + "&q=" + input.getText().toString();
				}else{
					current_url = current_url.substring(current_url.indexOf("&q="), current_url.length());
					new_url = current_url + "&q=" + input.getText().toString();
				}*/
				if(isNetworkAvailable()){
					new GetTweetsTask(TweetListView.this, false).execute(new_url);
				}else{
					Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_LONG).show();	
				}
				
				new_url = null;
				dismissSoftKeyboard();
				
			}
		});

		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				dismissSoftKeyboard();
			}
		});

		dialog.show();
		showSoftKeyboard();
	}

	public void refreshTweets(){
		
		// call the async http call again with the stored refresh url
		
		if(isNetworkAvailable()){
			
			// call the garbage cleaner
			//System.gc();
			
			// build new url for the refresh call
			String new_url = current_url.substring(0, current_url.indexOf("?")) + refresh_url;
			
			// kick off new async task with the new url
			new GetTweetsTask(TweetListView.this, false).execute(new_url);
			
		}else{
			Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return arrayList;
	};
		
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
	
	private void showSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager)TweetListView.this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    if (imm != null) {
	        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	    }
	}

	private void dismissSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager)TweetListView.this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    if (imm != null) {
	        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	    }
	}		
	
	class GetTweetsTask extends AsyncTask<String, String, String>{
		
		// set up async task to do http get calls in seperate thread
		
		// setup vars for activity, dialog and context
		private Activity activity;
		private ProgressDialog dialog;
		private Context context;
		boolean appendnew = false;
		
		// constructor to pass in activity and set context for dialog
		public GetTweetsTask(Activity activity, boolean appendnew){
			this.activity = activity;
			context = activity;
			dialog = new ProgressDialog(context);
			
			// if true, async task will add the results to the existing listview
			if(appendnew){
				this.appendnew = appendnew;
			}
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
	        this.dialog.setMessage(!appendnew ? "Loading tweets..." : "Loading more tweets...");
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
	        TweetListView.this.onResultObtained(result, appendnew);
	    }
	    
	    @Override
	    protected void onCancelled() {
	        // hide loader in case of async task being cancelled. on post execute may not run and everything will break!
	        if (dialog.isShowing()) {
                dialog.dismiss();
            }
	    }
	}

	@Override
	public void onTaskComplete(String result) {
		// TODO Auto-generated method stub
		
	}

}
