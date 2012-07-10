package com.myapp.projectzero;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class TweetTracker extends Activity{
	
	DatabaseHandler db;
	List<Address> myAddresses;
	Geocoder geocoder;
	List<TweetLocationInfo> locations;
	
    /** Called when the activity is first created. */	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // set the view for the activity
        setContentView(R.layout.main);  

		// setup geocode object 
		geocoder = new Geocoder(this, Locale.getDefault());        
        
    	// get the list view from R.id
    	final ListView lv = (ListView) findViewById(R.id.main_list_view);        
        
        // set up geo points and location name
    	/*
        TweetLocationInfo location1 = new TweetLocationInfo("London", 51.513425, -0.127168); 
        TweetLocationInfo location2 = new TweetLocationInfo("Bristol", 51.459968, -2.587395); 
        TweetLocationInfo location3 = new TweetLocationInfo("Manchester", 53.483644, -2.246038); 
        */
        
        // create array of the LocationInfo objects
        locations = new ArrayList<TweetLocationInfo>();
        //TweetLocationInfo[] locations = new TweetLocationInfo[]{};        
        
        // setup database handler
        db = new DatabaseHandler(this);
        //db.deleteAllItems();
        
        // reading all locations
        final List<Location> location_list = db.getAllLocations();  
        
        //Toast.makeText(getApplicationContext(), db.getLocationCount() + " locations", Toast.LENGTH_SHORT).show();
        /*
        for (Location lc : location_list) {
            String log = "Id: "+lc.getId()+" ,Name: " + lc.getName() + " ,Lat: " + lc.getLat() + " ,Lon: " + lc.getLon();
                // Writing Contacts to log
            Log.d("Name: ", log);
        } 
        */       
        
        // cycle through and build TweetLocationInfo object for each result
        for (Location loc : location_list) {        	
        	TweetLocationInfo location = new TweetLocationInfo(loc.getName(), Double.parseDouble(loc.getLat()), Double.parseDouble(loc.getLon()));
        	// add the object to the locations array
        	locations.add(location);
        }        
        
        // set up an adapter using and inbuild view and text view to pass the values into
        final ArrayAdapter<TweetLocationInfo> adapter = new ArrayAdapter<TweetLocationInfo>(TweetTracker.this, android.R.layout.simple_list_item_1, android.R.id.text1, locations);
        
        // attach the adapter to the previously gotten list view
        lv.setAdapter(adapter);                           
        
        // set up list dialog items
        final CharSequence[] items = {"View Tweets in a List", "View Tweets on a Map"};
        
        // set the onclick of each list item, access the content and do something with it
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
			public void onItemClick(AdapterView<?> parent, View view,
                final int i, long id) {
            		
            		// set up new alert dialog
	                AlertDialog.Builder dialog = new AlertDialog.Builder(TweetTracker.this);
	                
	                // set items to dialog and set up the onclick listener 
	                dialog.setItems(items, new DialogInterface.OnClickListener() {
	                    @Override
						public void onClick(DialogInterface dialog, int item) {	 
	                    	
                        	// get the item from the object array using the position of the item clicked
                        	TweetLocationInfo selectedItem = (TweetLocationInfo)(lv.getItemAtPosition(i));
	                    	
	                    	switch(item) {
		                        case 0: // show tweets in a list
		                        	dialog.dismiss();
		                        	showTweetsInList(selectedItem);
		                            break;
		                        case 1: // show tweets on a map
		                        	dialog.dismiss();
		                        	showTweetsOnMap(selectedItem);
		                            break;
		                        default:
	                    	}
	                    }
	                });
	                
	                // show dialog
	                dialog.show();
            	}
          });     
        

        
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int i, long id) {
		        
				// setup options for long click menu
		        final CharSequence[] options = {"Remove location"};
		        
				// set up new alert dialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(TweetTracker.this);
                
                // set items to dialog and set up the onclick listener 
                dialog.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int item) {	                     	
                    	switch(item) {
	                        case 0: // remove location		                        	
	                        		
	                        		db.deleteLocation(location_list.get(i));
	                        		//locations.remove(i);
	                        		//adapter.notifyDataSetChanged();
	                        		Toast.makeText(getApplicationContext(), "index: " + Integer.toString(i), Toast.LENGTH_SHORT).show();
	                        		/*
	                        		runOnUiThread(new Runnable() {
	                        		    public void run() {
	                        		    	locations.remove(i);
	                        		        adapter.notifyDataSetChanged();
	                        		    }
	                        		});
	                        		*/
	                        		
	                            break;
	                        default:
                    	}
                    }
                });                
                
                dialog.show();
                return false;
			}
        		
		});
    }
    
    public void showTweetsInList(TweetLocationInfo selectedItem){
    	
    	// set up new intent
    	Intent intent = new Intent(TweetTracker.this, TweetListView.class);
    	
    	// attach the location name, lat and lon
    	intent.putExtra("locationinfo", selectedItem.getName().toString() + "," + Double.toString(selectedItem.lat) + "," + Double.toString(selectedItem.lon));    	
    	
    	// start the activity with the intent
    	startActivity(intent);
    }
    
    public void showTweetsOnMap(TweetLocationInfo selectedItem){

    	// set up intent and attach text to pass to the next activity
		Intent intent = new Intent(TweetTracker.this, TweetMapView.class);

		// pass the selecteditem object location through to the next intent
		intent.putExtra("location", selectedItem.getLocation().toString());
		
		// launch intent
		startActivity(intent);
    	
    	// when clicked, show a toast with the TextView text
    	Toast.makeText(getApplicationContext(), selectedItem.getName() + ": " + selectedItem.getLocation().toString(), Toast.LENGTH_SHORT).show();		                        	
    }
    
	@Override // inflate specified menu
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.home_menu, menu);
	    return true;		
	}	
	
	@Override // handle menu clicks
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.add_location:
	        	addLocation();
	            return true;            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}	
	
	public void addLocation(){
		
		// show text input dialog and re-run the current_url with a q= attached!
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		// set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		dialog.setView(input);

		dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				
				if(isNetworkAvailable()){
		        	try {
						myAddresses = geocoder.getFromLocationName(input.getText().toString(), 1);
				        // output the lat and long of an address
				        if (myAddresses != null && myAddresses.size() > 0 ){
				        	// output results
				        	updateLocations(myAddresses, input.getText().toString());
				        	//showLoader(false);
				        }else {
				        	 Toast.makeText(getApplicationContext(), "Location not found!", Toast.LENGTH_LONG).show();
				        } 						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}else{
					Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_LONG).show();	
				}
				
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

	public void updateLocations(List<Address> addressData, String input){
		
    	// set results
    	Address resultAddress = addressData.get(0);            	
        
    	// get lat and lon
    	double mylat = resultAddress.getLatitude();
    	double mylon = resultAddress.getLongitude();       	
    	
    	// get things
    	String locality = resultAddress.getLocality();
        
    	// add the location to the database
    	db.addLocation(new Location(input, Double.toString(mylat), Double.toString(mylon)));
    	TweetLocationInfo location = new TweetLocationInfo(input, mylat, mylon);
    	locations.add(location);
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
	
	private void showSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager)TweetTracker.this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    if (imm != null) {
	        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	    }
	}

	private void dismissSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager)TweetTracker.this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    if (imm != null) {
	        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	    }
	}		
}