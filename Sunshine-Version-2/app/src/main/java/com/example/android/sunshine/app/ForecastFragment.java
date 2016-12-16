package com.example.android.sunshine.app;

/**
 * Created by Mick on 10/13/16.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@SuppressWarnings("all")
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForecastAdapter;

    //set number of days for the forecast query also kept here temporarily for rendering in DetailActivity
    int numDays = 14;

    public void ForecastFragment() {
    }

    //series of helper methods to simplify repeated processes & reduce repeating self

    //launches the asyncTask to get weather data from server with the zip code currently stored in the settings
    private void updateForecast()
    {
        FetchWeatherTask f = new FetchWeatherTask(getActivity(), mForecastAdapter);
        f.execute(getZip());
    }

    //retrieves the current units of measure stored in SharedPreferences
//    public String getUnits()
//    {
//        //creates object, retrieves defaults
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        //assigns key (choice) or the specified default if otherwise unavailable
//        String units = sharedPref.getString(getString(R.string.pref_temp_key), getString(R.string.pref_temp_default));
//        return units;
//    }

    //retrieves the current zip code location stored in SharedPreferences
    private String getZip()
    {
        //..
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //..
        String location = sharedPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        return location;
    }

    //provides the actionable code for the menu option to show current location (zip in SharedPrefs) on map
    private void showMap(String zip)
    {
        //uses the get zip method with the beginning of the query to construct the URI
        Uri geoLocation = Uri.parse("geo:0,0?q=" + zip);
        Context mContext = getActivity();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(geoLocation);

        //launches implicit intent if there is a supported app available
        if (mapIntent.resolveActivity(mContext.getPackageManager()) != null)
        {
            startActivity(mapIntent);
        }
        //if there is no suitable app to open the geoLocation query in, we write a toast notifying user of this
        else
        {
            CharSequence toastText = "There is no maps app available to open. Install a suitable maps app to continue..";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(mContext, toastText, duration);
            toast.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh)
        {
            //updates the forecast upon selection of the menu item
            updateForecast();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            //launches the SettingsActivity upon selection of the menu item
            Context sContext = getActivity();
            Intent settingsMenu = new Intent(sContext, SettingsActivity.class);
            sContext.startActivity(settingsMenu);
            return true;
        }

        if (id == R.id.action_showMap)
        {
            showMap(getZip());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //dummy ArrayList updated to show the user something if there is time enough for any lapse while rendering/receiving weather data
        final List<String> forecastList = new ArrayList<>(Arrays.asList("gathering data...", "gathering data...", "gathering data...", "gathering data...", "gathering data...", "gathering data...", "gathering data..."));
        mForecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, forecastList);

        //open the item contents in it's own DetailActivity
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context thisContext = getActivity().getApplicationContext();
                CharSequence text = "Day " + (position + 1) + "/" + numDays + ": " + forecastList.get(position);
                Intent detailIntent = new Intent(thisContext, DetailActivity.class);
                detailIntent.putExtra("text", text);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateForecast();
    }

//    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//
//        private String getReadableDateString(long time){
//            // Because the API returns a unix timestamp (measured in seconds),
//            // it must be converted to milliseconds in order to be converted to valid date.
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(time);
//        }
//
//        /**
//         * Prepare the weather high/lows for presentation.
//         */
//        private String formatHighLows(double high, double low) {
//            //the user doesn't care about tenths of a degree.
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }
//
//        /**
//         * Take the String representing the complete forecast in JSON Format and
//         * pull out the data we need to construct the Strings needed for the wireframes.
//         *
//         * Constructor takes the JSON string and converts it
//         * into an Object hierarchy.
//         */
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//                throws JSONException {
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            // OWM returns daily forecasts based upon the local time of the city that is being
//            // asked for, which means that we need to know the GMT offset to translate this data
//            // properly.
//
//            // Since this data is also sent in-order and the first day is always the
//            // current day, we're going to take advantage of that to get a nice
//            // normalized UTC date for all of our weather.
//
//            Time dayTime = new Time();
//            dayTime.setToNow();
//
//            // we start at the day returned by local time. Otherwise this is a mess.
//            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//            // now we work exclusively in UTC
//            dayTime = new Time();
//
//            String[] resultStrs = new String[numDays];
//
//            for(int i = 0; i < weatherArray.length(); i++)
//            {
//                // For now, using the format "Day, description, hi/low"
//                String day;
//                String description;
//                String highAndLow;
//
//                // Get the JSON object representing the day
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                // The date/time is returned as a long.  We need to convert that
//                // into something human-readable, since most people won't read "1400356800" as
//                // "this saturday".
//                long dateTime;
//
//                // Cheating to convert this to UTC time, which is what we want anyhow
//                dateTime = dayTime.setJulianDay(julianStartDay+i);
//                day = getReadableDateString(dateTime);
//
//                // description is in a child array called "weather", which is 1 element long.
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                // Temperatures are in a child object called "temp"
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//
//            for (String s : resultStrs) {
//                Log.v(LOG_TAG, "Forecast entry!: " + s);
//            }
//            return resultStrs;
//
//        }
//
//        @Override
//        protected String[] doInBackground(String... postcode) {
//
//            // If there's no zip code, there's nothing to look up.  Verify size of params.
//            if (postcode.length == 0) {
//                return null;
//            }
//
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//
//            try {
//                //Uri.Builder to append the url based on users postal code input
//                Uri.Builder uri = new Uri.Builder();
//                uri.scheme("http");
//                uri.authority("api.openweathermap.org");
//                uri.appendPath("data");
//                uri.appendPath("2.5");
//                uri.appendPath("forecast");
//                uri.appendPath("daily");
//                uri.appendQueryParameter("zip", postcode[0]);
//                uri.appendQueryParameter("mode", "json");
//                uri.appendQueryParameter("units", getUnits());
//                uri.appendQueryParameter("cnt", "7");
//
//                //modified app/build.gradle to globally distribute the api key for OpenWeatherMap and contained it here for the network call
//                uri.appendQueryParameter("APPID", BuildConfig.OPEN_WEATHER_MAP_API_KEY);
//
//                //Possible parameters @ http://openweathermap.org/API#forecast
//                String baseUrl = uri.build().toString();
//
//                //String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
//                URL url = new URL(baseUrl);
//
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                forecastJsonStr = buffer.toString();
//
//            }
//            catch (IOException e)
//            {
//                Log.e(LOG_TAG, "Error ", e);
//                // If the code didn't successfully get the weather data, there's no point in attemping
//                // to parse it.
//                return null;
//            }
//            finally
//            {
//                //close the stream whether successful or not..
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//
//            //implemented in a try/catch so app dosen't crash if the data isn't received or parsed right
//            try
//            {
//                return getWeatherDataFromJson(forecastJsonStr, numDays);
//            }
//            catch (JSONException e)
//            {
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//            // null will only happen if there was an error getting or parsing the forecast.
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String[] result) {
//            super.onPostExecute(result);
//
//            if (result != null)
//            {
//                //clear the forecast adapter array and add the real-time weather data one block at a time
//                mForecastAdapter.clear();
//                for(String dayForecastString : result)
//                {
//                    mForecastAdapter.add(dayForecastString);
//                }
//            }
//        }
//    }
}




