/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

import static com.example.android.sunshine.app.data.TestUtilities.createNorthPoleLocationValues;
import static com.example.android.sunshine.app.data.TestUtilities.createWeatherValues;
import static com.example.android.sunshine.app.data.TestUtilities.validateCurrentRecord;

@SuppressWarnings("all")
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public long locationTableTest()
    {
        // First step: Get reference to writable database
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = createNorthPoleLocationValues();
        // Insert ContentValues into database and get a row ID back
        long rowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);
        assertTrue("The row of data in the location table was not created properly. Revisit db.insert.", rowId != - 1);
        // Query the database and receive a Cursor back
        Cursor c = db.query(WeatherContract.LocationEntry.TABLE_NAME, null, null, null, null, null, null);

        // Move the cursor to a valid database row
        if (c.moveToFirst() == false)
        {
            fail("There is no data in the Location Table?!!");
        }
        else
        {
            String cityName = c.getString(c.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME));
            String coordLat = c.getString(c.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LAT));
            String coordLong = c.getString(c.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LONG));
            String locationSetting = c.getString(c.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING));

            Log.v("!!!!!!cityName", cityName);
            Log.v("!!!!!!coordLat", coordLat);
            Log.v("!!!!!!coordLong", coordLong);
            Log.v("!!!!!!locationSetting", locationSetting);

            assertEquals("Error in data transfer @ locationSetting!", "99705", locationSetting);
            assertEquals("Error in data transfer @ cityName", "North Pole", cityName);
            assertEquals("Error in data transfer @ coordLat", "64.7488", coordLat.toString());
            assertEquals("Error in data transfer @ coordLong", "-147.353", coordLong.toString());
        }


        // Validate data in resulting Cursor with the original ContentValues
        validateCurrentRecord("It appears data in table does not match the data inputted", c, testValues);

        assertFalse("There are multiple entries in the db!", c.moveToNext());
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        // Finally, close the cursor and database
        c.close();
        db.close();

        return rowId;
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());

        db.close();
    }


    public void testLocationTable() {

        locationTableTest();
    }


    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.
        long locRowId = locationTableTest();

        // First step: Get reference to writable database
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)
        ContentValues wthrValues = createWeatherValues(locRowId);
        // Insert ContentValues into database and get a row ID back
        long wthrRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, wthrValues);
        // Query the database and receive a Cursor back
        Cursor c = db.query(WeatherContract.WeatherEntry.TABLE_NAME, null, null, null, null, null, null);
        // Move the cursor to a valid database row
        if (c.moveToFirst() == false)
        {
            fail("There is no data in the Weather Table!?");
        }
        else
        {
            validateCurrentRecord("It appears data in table does not match the data inputted", c, wthrValues);
        }
        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        // Finally, close the cursor and database
        db.close();
        c.close();
    }
}
