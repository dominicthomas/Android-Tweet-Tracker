package com.myapp.projectzero;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class DatabaseHandler extends SQLiteOpenHelper {
 
	/*
	 * Usage: 
	 * 
	 * DatabaseHandler db = new DatabaseHandler(this);
	 * 
	 * db.addLocation(new Location("London", "51,58"));
	 * 
	 */	
	
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "locationsDatas";
 
    // Locations table name
    private static final String TABLE_LOCATIONS = "locations";
 
    // Locations Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_LAT + " TEXT," + KEY_LON + " TEXT" +")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new contact
    void addLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, location.getName()); // Location Name
        values.put(KEY_LAT, location.getLat()); // Location Lat
        values.put(KEY_LON, location.getLon()); // Location Lon
        
        // Inserting Row
        db.insert(TABLE_LOCATIONS, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single contact
    Location getLocation(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_LOCATIONS, new String[] { KEY_ID,
                KEY_NAME, KEY_LAT, KEY_LON }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Location contact = new Location(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3));
        
        // return contact
        return contact;
    }
 
    // Getting All Contacts
    public List<Location> getAllLocations() {
        List<Location> locationList = new ArrayList<Location>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	Location location = new Location();
            	location.setId(Integer.parseInt(cursor.getString(0)));
            	location.setName(cursor.getString(1));
            	location.setLat(cursor.getString(2));
            	location.setLon(cursor.getString(3));
                // Adding contact to list
            	locationList.add(location);
            } while (cursor.moveToNext());
        }

        // return contact list
        return locationList;
    }
 
    // Updating single contact
    public int updateLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, location.getName());
        values.put(KEY_LAT, location.getLat());
        values.put(KEY_LON, location.getLon());
        
        // updating row
        return db.update(TABLE_LOCATIONS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(location.getId()) });
    }
 
    // Deleting single contact
    public void deleteLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(location.getId()) });
        db.close();
    }
 
    // Getting contacts Count
    public int getLocationCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOCATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        // return count
        return cursor.getCount();
    }
    
    public void deleteAllItems(){
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_LOCATIONS, null, null);
    }
}