package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.content.Intent;
import android.util.Log;

/**
 * Created by plee on 11/3/14.
 */
public class CardListActivity extends SingleFragmentActivity
{
    private static final String TAG                = "CardListActivity";
    private static final String CARD_CHOOSER_EXTRA = "cce";

    public static void addExtra(Intent intent, CardChooser cardChooser)
    {
        intent.putExtra(CARD_CHOOSER_EXTRA, cardChooser);
    }

    //factory method for parent
    protected Fragment createFragment()
    {
        Log.d(TAG, "create CardListFragment");
        CardChooser cardChooser = (CardChooser)getIntent().getSerializableExtra(CARD_CHOOSER_EXTRA);

        //pass the cardChooser to the CardListFragment
        return CardListFragment.newInstance(cardChooser);
    }
}