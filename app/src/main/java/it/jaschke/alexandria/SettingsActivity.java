package it.jaschke.alexandria;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity {

    private static final String SETTINGS_FRAGMENT = "settings_fr";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag(SETTINGS_FRAGMENT) == null){
            fm.beginTransaction()
                    .add(android.R.id.content, new SettingsFragment(), SETTINGS_FRAGMENT)
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle bundle){
            super.onCreate(bundle);
            addPreferencesFromResource(R.xml.preferences);
        }

    }

}
