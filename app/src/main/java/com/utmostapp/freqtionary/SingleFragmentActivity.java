package com.utmostapp.freqtionary;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.MenuItem;

import com.utmostapp.freqtionary.R;

/**
 * Created by plee on 9/19/14.
 */
public abstract class SingleFragmentActivity extends Activity
{
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getFragmentManager();

        //fragmentContainer is defined in activity_fragment.xml layout
        Fragment fragment  = fm.findFragmentById(R.id.fragmentContainer);

        //add a fragment to this activity if it wasn't already
        if(fragment == null)
        {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
