package com.k1ngdr3w.supercontacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by k1ngdr3w on 6/1/16.
 */

class DatabaseHelper {

    //Field Names:
    public DatabaseHelper(Context context) {
        // create a new DatabaseOpenHelper
        dboh = new DatabaseOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TAG = "DBAdapter"; //used for logging database version changes

    //DataBase info:
    private static final String DATABASE_NAME = "Super Contacts";
    static final String DATABASE_TABLE = "CONTACTS";
    public static final int DATABASE_VERSION = 2;

    private SQLiteDatabase sqlDb;
    private final DatabaseOpenHelper dboh;

    public static final String KEY_ROWID = "_id";
    public static final String KEY_LASTNAME = "last_name";
    public static final String KEY_FIRSTNAME = "first_name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_BIRTHDAY = "bday";
    public static final String KEY_PHONENUMBER = "phone_number";
    public static final String KEY_GEOLOCATION = "geo_loc";

    private final String[] ALL_KEYS = new String[]{KEY_ROWID, KEY_LASTNAME, KEY_FIRSTNAME, KEY_ADDRESS, KEY_BIRTHDAY, KEY_PHONENUMBER, KEY_GEOLOCATION};


    //SQL statement to create database
    private final String DATABASE_CREATE_SQL =
            "CREATE TABLE " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_LASTNAME + " TEXT, "
                    + KEY_FIRSTNAME + " TEXT, "
                    + KEY_ADDRESS + " TEXT, "
                    + KEY_BIRTHDAY + " TEXT, "
                    + KEY_PHONENUMBER + " TEXT, "
                    + KEY_GEOLOCATION + " TEXT "
                    + ");";


    public SQLiteDatabase open() throws SQLiteException {
        sqlDb = dboh.getWritableDatabase();
        return sqlDb;
    }

    public void close() {
        if (sqlDb != null)
            sqlDb.close();
    }


    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return sqlDb.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void updateContact(long id, String lastName, String firstName, String address, String bday, String phoneNumber, String geoLoc) {
        Log.w("CS3287 ____-----______!", "sqlDb helper updateCourse the course:\nROW_ID: " + id + "\ncourseID: " + lastName + "\ncourse Name: " + firstName + "\nCourse Code: " + address + "\nstart/end at " + bday + "/" + phoneNumber);
        ContentValues updateContact = new ContentValues();
        updateContact.put(KEY_LASTNAME, lastName);
        updateContact.put(KEY_FIRSTNAME, firstName);
        updateContact.put(KEY_ADDRESS, address);
        updateContact.put(KEY_BIRTHDAY, bday);
        updateContact.put(KEY_PHONENUMBER, phoneNumber);
        updateContact.put(KEY_GEOLOCATION, geoLoc);
        open();
        sqlDb.update(DATABASE_TABLE, updateContact, "_id=" + id, null);
        close();
    }

    public void deleteAll() {
        Cursor c = getAllContacts();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    public long insertContact(String lastName, String firstName, String address, String bday, String phoneNumber, String geoLoc) {
        long rowID;
        ContentValues newContact = new ContentValues();
        newContact.put(KEY_LASTNAME, lastName);
        newContact.put(KEY_FIRSTNAME, firstName);
        newContact.put(KEY_ADDRESS, address);
        newContact.put(KEY_BIRTHDAY, bday);
        newContact.put(KEY_PHONENUMBER, phoneNumber);
        newContact.put(KEY_GEOLOCATION, geoLoc);
        open();
        rowID = sqlDb.insert(DATABASE_TABLE, null, newContact);
        close();
        return rowID;
    }


    public Cursor getOneContact(long id) {
        Log.d("_____-------!", "sqlDb helper getOneCourse called once for the ID: " + id);
        String where = KEY_ROWID + "=" + id;
        Cursor c = sqlDb.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null, null);

        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getAllContacts() {
        String where = null;
        Cursor c = sqlDb.query(true, DATABASE_TABLE, ALL_KEYS, where, null, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    private class DatabaseOpenHelper extends SQLiteOpenHelper {

        //Create a const from the DBH so it's always inst properly
        public DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            Log.d("CS3287 ____-----______!", "CREATING sdf DB YO!" + version);

        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("CS3287 ____-----______!", "CREATING THE DB YO!");
            db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DatabaseHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
