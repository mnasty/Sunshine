package com.example.android.sunshine.app;

/**
 * Created by Mick on 10/13/16.
 */

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.text.format.Time;
import java.text.SimpleDateFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

//@SuppressWarnings("all")
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchWeatherTask f = new FetchWeatherTask();
            //f.doInBackground();
            f.execute("63101");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> strings = new ArrayList<>(Arrays.asList("Today-Sunny-88/63",
                "Tomorrow-Foggy-70/46", "Wednesday-Cloudy-72/63", "Wednesday-Cloudy-72/63",
                "Thursday-Rainy-64/51", "Friday-Foggy-70/46", "Saturday-Sunny-76/68",
                "Sunday-Shitty-20/45", "Monday-Hurricane-Your going to die"));

        mForecastAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview, strings);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... postcode) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            //user input postcode's Integer type converted to string for Uri.Builder
            //String postcodeString = postcode.toString();

            //**test purposes only
            //String postcodeString = "63101";

            try {
                //Uri.Builder to append the url based on users postal code input
                Uri.Builder uri = new Uri.Builder();
                uri.scheme("http");
                uri.authority("api.openweathermap.org");
                uri.appendPath("data");
                uri.appendPath("2.5");
                uri.appendPath("forecast");
                uri.appendPath("daily");
                uri.appendQueryParameter("q", postcode[0]);
                uri.appendQueryParameter("mode", "json");
                uri.appendQueryParameter("units", "metric");
                uri.appendQueryParameter("cnt", "7");
                //modified app/build.gradle to globally distribute the api key for openweathermap and contained it here for the network call
                uri.appendQueryParameter("APPID", BuildConfig.OPEN_WEATHER_MAP_API_KEY);

                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
                String baseUrl = uri.build().toString();

                //String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL(baseUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();


            } catch (IOException e)
            {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            }
            finally
            {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}




