package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.sunshine.app.R.menu.detail;

//@SuppressWarnings("all")
public class DetailActivity extends ActionBarActivity {

    protected static String shareString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(detail, menu);
        //find share menu button
        MenuItem shareItem = menu.findItem(R.id.action_share);
        //get the action provider and therefore define the action for the action_share button
        MenuItemCompat.getActionProvider(shareItem);

        //set an onClickItemListener to the share button in the menu so we know when to launch
        shareItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                //create the intent to be used
                Intent shareIntent = new Intent(Intent.ACTION_SEND);

                //assign the relevant data to the share intent and format it appropriately
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.setType("text/plain");

                // Verify the original intent will resolve to at least one activity
                if (shareIntent.resolveActivity(getPackageManager()) != null)
                {
                    startActivity(shareIntent);
                }
                else
                {
                    //gracefully display toast informing user they live under a rock
                    CharSequence toastText = "There is no app available to share with. Install a suitable app to continue..";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(getBaseContext(), toastText, duration);
                    toast.show();
                }

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent settingsMenu = new Intent(this, SettingsActivity.class);
            this.startActivity(settingsMenu);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //create intent to receive the data passed to DetailActivity
            Intent detailIntentReceiver = getActivity().getIntent();
            //attach inflated layout to the rootView
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            //store the item info text passed into the activity here
            CharSequence text = detailIntentReceiver.getCharSequenceExtra("text");
            //implement the static string declared globally to store the item info text plus hashtag0
            shareString = text.toString() + " #SunshineApp";
            //create the single entity TextView for the item info for now and set the text
            TextView detailTextView = (TextView) rootView.findViewById(R.id.detail_textView);
            detailTextView.setText(text);

            return rootView;
        }
    }
}