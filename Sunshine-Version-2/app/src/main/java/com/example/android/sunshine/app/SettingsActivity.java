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
package com.example.android.sunshine.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    String mLatitude;
    String mLongitude;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("!!!LOCATION", "onCreate:SettingsActivity called");

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));



        Preference button = findPreference(getString(R.string.get_loc_shared_prefs_btn));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //on click initiate the request for location coordinates
                mGoogleApiClient.connect();
                Log.d("!!!LOCATION", "!!!Location Button Functioning..");
                return true;
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private void bindPreferenceSummaryGpsToZip(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        //validate that the length of the zip code is at least 5 digits
        if (stringValue.length() < 5)
        {
            android.widget.Toast invalidZipLength = android.widget.Toast.makeText(this, "ENTER A VALID 5 DIGIT ZIP CODE!", android.widget.Toast.LENGTH_LONG);
            invalidZipLength.show();
            return false;
        }

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }

        //refresh and populate new data
        SunshineSyncAdapter.syncImmediately(this, null, null);
        return true;
    }

    private void handleNewLocation(String lat, String lon) {
        SunshineSyncAdapter.syncImmediately(this, lat, lon);
        Log.d("!!!LOCATION", "!!!handleNewLocation Called Successfully");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("!!!!LOCATION", "Location services connected.");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            mLatitude = String.valueOf(location.getLatitude());
            mLongitude = String.valueOf(location.getLongitude());
        }
        else
        {
            Utility.displayGpsStatus(this);
        }

        handleNewLocation(mLatitude, mLongitude);

        //dialog to show location is being fetched on UI
        AlertDialog.Builder alrtDialogBldr = new AlertDialog.Builder(SettingsActivity.this);
        alrtDialogBldr.setMessage("Fetching Your Current Location. Please Wait..");
        alrtDialogBldr.setTitle("Fetching Location..");
        final AlertDialog alertDialog = alrtDialogBldr.create();
        alertDialog.show();

        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                    //check the status of the internet connection
                    if (!Utility.displayNetworkStatus(SettingsActivity.this))
                    {
                        //if there is no internet connectivity why display a toast with null in it?
                        return;
                    }
                    else if (!Utility.displayGpsStatus(SettingsActivity.this))
                    {
                        //if there is no GPS connectivity why display a toast with null in it?
                        return;
                    }
                    else {
                        //we make sure to grab the city name on the fly here so we only get values after the refresh takes place
                        Toast curLocationStatus = Toast.makeText(SettingsActivity.this, "Your Location Was Fetched Successfully!" +
                                " Press the back button to see weather for your location in: " + SunshineSyncAdapter.cityNameZipValidation, Toast.LENGTH_LONG);
                        curLocationStatus.show();
                    }

                }
            }
        };

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 3000);

        Log.d("!!!LOCATION", "Location appears to be | lat: " + mLatitude + " | long: " + mLongitude);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("!!!LOCATION", "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("!!!LOCATION", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
}