package com.myapp.projectzero;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class TweetMapOverlay extends ItemizedOverlay<OverlayItem> {
	
	// setup the overlay item array list to store the overlays
	private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();
   
	// setup the context ready to be passed in when called overlayitems from the mapview
	private Context context;
   
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
	
	@Override // create the items
	protected OverlayItem createItem(int i) {
      return mapOverlays.get(i);
	}

	@Override // get the number of overlays
	public int size() {
      return mapOverlays.size();
	}
   
	@Override // set the ontap event for the overlays
	protected boolean onTap(int index) {	
		// get the relevant overlay
		OverlayItem item = mapOverlays.get(index);
		
		// set up a dialog and pass in the context
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		
		// set the dialog parameters and show
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		
		return true;
	}
   
	// used to add overlays to the arraylist and populate
	public void addOverlay(OverlayItem overlay) {
		mapOverlays.add(overlay);
		this.populate();
	}

}

