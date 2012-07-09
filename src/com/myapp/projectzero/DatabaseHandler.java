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
    private static final String DATABASE_NAME = "contactsManager";
 
    // Contacts table name
    private static final String TABLE_LOCATIONS = "locations";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LOC = "location";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_LOC + " TEXT" + ")";
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
    void addLocation(Locations location) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, location.getName()); // Contact Name
        values.put(KEY_LOC, location.getLocation()); // Contact Phone
 
        // Inserting Row
        db.insert(TABLE_LOCATIONS, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single contact
    Locations getLocation(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_LOCATIONS, new String[] { KEY_ID,
                KEY_NAME, KEY_LOC }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Locations contact = new Locations(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        
        // return contact
        return contact;
    }
 
    // Getting All Contacts
    public List<Locations> getAllContacts() {
        List<Locations> locationList = new ArrayList<Locations>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	Locations location = new Locations();
            	location.setId(Integer.parseInt(cursor.getString(0)));
            	location.setName(cursor.getString(1));
            	location.setLocation(cursor.getString(2));
                // Adding contact to list
            	locationList.add(location);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return locationList;
    }
 
    // Updating single contact
    public int updateContact(Locations location) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, location.getName());
        values.put(KEY_LOC, location.getLocation());
 
        // updating row
        return db.update(TABLE_LOCATIONS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(location.getId()) });
    }
 
    // Deleting single contact
    public void deleteContact(Locations location) {
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
 
}