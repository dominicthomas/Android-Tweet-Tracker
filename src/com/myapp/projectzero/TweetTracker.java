package com.myapp.projectzero;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TweetTracker extends Activity{
	
    /** Called when the activity is first created. */	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // set the view for the activity
        setContentView(R.layout.main);  

    	// get the list view from R.id
    	final ListView lv = (ListView) findViewById(R.id.main_list_view);        
        
        // set up geo points and location name
        TweetLocationInfo location1 = new TweetLocationInfo("London", 51.513425, -0.127168); 
        TweetLocationInfo location2 = new TweetLocationInfo("Bristol", 51.459968, -2.587395); 
        TweetLocationInfo location3 = new TweetLocationInfo("Manchester", 53.483644, -2.246038); 
        
        // create array of the LocationInfo objects
        TweetLocationInfo[] locations = new TweetLocationInfo[]{location1, location2, location3};        
        
        // set up an adapter using and inbuild view and text view to pass the values into
        ArrayAdapter<TweetLocationInfo> adapter = new ArrayAdapter<TweetLocationInfo>(TweetTracker.this, android.R.layout.simple_list_item_1, android.R.id.text1, locations);
        
        // attach the adapter to the previously gotten list view
        lv.setAdapter(adapter);   
        
        // set up list dialog items
        final CharSequence[] items = {"View Tweets in a List", "View Tweets on a Map"};
        
        // set the onclick of each list item, access the content and do something with it
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                final int i, long id) {
            		
            		// set up new alert dialog
	                AlertDialog.Builder dialog = new AlertDialog.Builder(TweetTracker.this);
	                
	                // set items to dialog and set up the onclick listener 
	                dialog.setItems(items, new DialogInterface.OnClickListener() {
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

}