package com.example.android.sunshine.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class WeatherDataParser {

    public static double getMaxTemperatureForDay(String weatherString, int dayIndex) throws JSONException {
        //solution formatted to provide best performance by keeping all 7 days in memory
        //instantiate object/array
        JSONObject jObj = new JSONObject(weatherString);
        JSONArray a = jObj.getJSONArray("list");

        //array for the seven days weather forecast
        double[] sevenDays = new double[7];

        //for loop to iterate through JSON Object/Array
        for (int i = 0; i < 7; i++) {
            String RawJSON = a.getJSONObject(i).toString();
            JSONObject jObjEval = new JSONObject(RawJSON);
            String firstDayTempJSON = jObjEval.getJSONObject("temp").toString();
            JSONObject jObjEvalSubclass = new JSONObject(firstDayTempJSON);

            sevenDays[i] = jObjEvalSubclass.getDouble("max");
        }
        //return the location of the array based on dayIndex
        return sevenDays[dayIndex];
    }
}