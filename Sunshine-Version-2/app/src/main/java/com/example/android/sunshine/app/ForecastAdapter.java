package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.android.sunshine.app.Utility.getArtDrawable;
import static com.example.android.sunshine.app.Utility.getIcDrawable;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext, high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
        return highLowStr;
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }



    //implement view holders, reducing unnecessary calls traversing the view hierarchy for layout children id's
    public static class ViewConstants {

        //declare constants
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descView;
        public final TextView highView;
        public final TextView lowView;
        public final TextView humidityView;
        public final TextView windView;
        public final TextView pressureView;

        //store our constants in the constructor
        public ViewConstants(View view)
        {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
            humidityView = (TextView) view.findViewById(R.id.list_item_humidity_textview);
            windView = (TextView) view.findViewById(R.id.list_item_wind_textview);
            pressureView = (TextView) view.findViewById(R.id.list_item_pressure_textview);
        }
    }

    /*
        These views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //get the view type representation integer using the overridden abstract method
        int viewType = getItemViewType(cursor.getPosition());
        //set the resource id linking to the layout type based on the view type representation integer
        int layoutId = (viewType > 0) ? R.layout.list_item_forecast : R.layout.list_item_forecast_today;

        //store the layout to be inflated
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        //implement view holders, reducing unnecessary calls traversing the view hierarchy for layout children id's
        ViewConstants vc = new ViewConstants(view);
        view.setTag(vc);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //implement view holders, reducing unnecessary calls traversing the view hierarchy for layout children id's
        ViewConstants vc = (ViewConstants) view.getTag();

        //retrieve weather status code
        int conditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

        //determine how to differentiate by view type to get the correct drawable to display
        if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY)
        {
            vc.iconView.setImageResource(getArtDrawable(conditionId));
        }
        else
        {
            vc.iconView.setImageResource(getIcDrawable(conditionId));
        }

        //read date from cursor
        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        //find textview and set formatted date
        vc.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        //read forecast description from cursor and set textview accordingly
        String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        vc.descView.setText(desc);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor and set textview accordingly
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        vc.highView.setText(Utility.formatTemperature(mContext, high, isMetric));

        // Read low temperature from cursor and set textview accordingly
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        vc.lowView.setText(Utility.formatTemperature(mContext, low, isMetric));
    }
}