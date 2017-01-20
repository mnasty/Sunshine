package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.android.sunshine.app.sync.SunshineSyncAdapter.isUsZip;

public class Utility {

    public static void showInvalidZipToast(Context c) {
        Log.d("!!!", "Outside if statement");

        View view = new View(c);
        //grab new data from server hot and validate the country code as US or return false and our toast
        if (isUsZip != true) {
            Log.d("!!!", "Inside if statement");
            Toast invalidUSZipToast = Toast.makeText(c, "THE ZIP CODE PROVIDED IS NOT A VALID US ZIP CODE! YOU ARE ATTEMPTING TO VIEW WEATHER FROM: " + SunshineSyncAdapter.cityNameZipValidation + ", " + SunshineSyncAdapter.countryCodeZipValidation, Toast.LENGTH_LONG);
            invalidUSZipToast.show();
        }
    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        }
        else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;

            //android studio bug id's this incorrectly as an error - known issue in google's kb
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static int getArtDrawable(int conditionId)
    {
        if (conditionId == 500 || conditionId >= 300 && conditionId <= 321 || conditionId == 906)
        {
            return R.drawable.art_light_rain;
        }
        else if (conditionId >= 501 && conditionId <= 531)
        {
            return R.drawable.art_rain;
        }
        else if (conditionId >= 600 && conditionId <= 622 || conditionId == 903)
        {
            return R.drawable.art_snow;
        }
        else if (conditionId >= 701 && conditionId <= 781 || conditionId == 900 || conditionId == 905 || conditionId >= 957 && conditionId <= 959)
        {
            return R.drawable.art_fog;
        }
        else if (conditionId == 800 || conditionId == 904 || conditionId >= 951 && conditionId <= 956)
        {
            return R.drawable.art_clear;
        }
        else if (conditionId == 801)
        {
            return R.drawable.art_light_clouds;
        }
        else if (conditionId >= 802 && conditionId <= 804)
        {
            return R.drawable.art_clouds;
        }
        else if (conditionId >= 200 && conditionId <= 232 || conditionId == 901 || conditionId == 902 || conditionId >= 960 && conditionId <= 962)
        {
            return R.drawable.art_storm;
        }
        else
        {
            Log.d("!!!OPENWEATHERMAP_ERROR", "getArtDrawable() Recieved Unknown Weather Status Code: " + String.valueOf(conditionId));
            return R.drawable.art_light_clouds;
        }
    }

    public static int getIcDrawable(int conditionId)
    {
        if (conditionId == 500 || conditionId >= 300 && conditionId <= 321 || conditionId == 906)
        {
            return R.drawable.ic_light_rain;
        }
        else if (conditionId >= 501 && conditionId <= 531)
        {
            return R.drawable.ic_rain;
        }
        else if (conditionId >= 600 && conditionId <= 622 || conditionId == 903)
        {
            return R.drawable.ic_snow;
        }
        else if (conditionId >= 701 && conditionId <= 781 || conditionId == 900 || conditionId == 905 || conditionId >= 957 && conditionId <= 959)
        {
            return R.drawable.ic_fog;
        }
        else if (conditionId == 800 || conditionId == 904 || conditionId >= 951 && conditionId <= 956)
        {
            return R.drawable.ic_clear;
        }
        else if (conditionId == 801)
        {
            return R.drawable.ic_light_clouds;
        }
        else if (conditionId >= 802 && conditionId <= 804)
        {
            return R.drawable.ic_cloudy;
        }
        else if (conditionId >= 200 && conditionId <= 232 || conditionId == 901 || conditionId == 902 || conditionId >= 960 && conditionId <= 962)
        {
            return R.drawable.ic_storm;
        }
        else
        {
            Log.d("!!!OPENWEATHERMAP_ERROR", "getIcDrawable() Recieved Unknown Weather Status Code: " + String.valueOf(conditionId));
            return R.drawable.ic_light_clouds;
        }
    }
}