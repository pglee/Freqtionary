package com.utmostapp.freqtionary;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * The controller for displaying the list of cards
 *
 * Created by plee on 11/3/14.
 */
public class CardListFragment extends ListFragment
{
    private static final String TAG                 = "CardListFragment";
    private static final String DEFAULT_LESSON_FILE = "lesson1.json";
    private static final String CARD_CHOOSER_TAG    = "lesson1.json";

    private static final int HIGH_POS  = 0;
    private static final int MED_POS   = 1;
    private static final int LOW_POS   = 2;
    private static final int NEVER_POS = 3;

    private CardChooser cardChooser;

    /************************************************************************
     * call back when the page is first opened
     *
     * @param savedInstanceState contains the card chooser being passed to this page
     *************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.cardChooser         = (CardChooser)getArguments().getSerializable(CARD_CHOOSER_TAG);
        WordArrayAdapter adapter = new WordArrayAdapter(this.cardChooser.getMasterList());

        setListAdapter(adapter);
    }

    /************************************************************************
     * Factory method
     *
     * @param cardChooser The chooser object used to retrieve the card to display
     *************************************************************************/
    public static CardListFragment newInstance(CardChooser cardChooser)
    {
        Bundle args = new Bundle();
        args.putSerializable(CARD_CHOOSER_TAG, cardChooser);

        CardListFragment fragment = new CardListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    //Builds the custom view for the fragment by populating individual items
    private class WordArrayAdapter extends ArrayAdapter<Card>
    {
        public WordArrayAdapter(ArrayList<Card> cards)
        {
            super(getActivity(), 0, cards);
        }

        @Override
        //populates the view for each list item
        public View getView(int position, View view, ViewGroup parent)
        {
            //if no view given, inflate one
            if(view == null)
            {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_word, null);
            }

            Card card = getItem(position);
            TextView rank          = (TextView)view.findViewById(R.id.rank);
            TextView primaryWord   = (TextView)view.findViewById(R.id.primary_word);
            TextView secondaryWord = (TextView)view.findViewById(R.id.secondary_word);
            Spinner repSpinner     = (Spinner)view.findViewById(R.id.repetition_spinner);

            card.assignRankText(rank);
            card.assignTopText(primaryWord);
            card.assignBottomText(secondaryWord);
            setSpinner(repSpinner, card);

            RepetitionSpinnerListener listener = new RepetitionSpinnerListener(card);
            repSpinner.setOnItemSelectedListener(listener);

            return view;
        }

        //sets the selection item for the spinner
        private void setSpinner(Spinner repSpinner, Card card)
        {
            if(card.isHigh())
            {
                repSpinner.setSelection(HIGH_POS);
            }
            else if(card.isMedium())
            {
                repSpinner.setSelection(MED_POS);
            }
            else if(card.isLow())
            {
                repSpinner.setSelection(LOW_POS);
            }
            else if(card.isNever())
            {
                repSpinner.setSelection(NEVER_POS);
            }
            else
                repSpinner.setSelection(HIGH_POS);
        }
    }

    /************************************************************************
     * The listener for the selection widget for the repetition selection in the list
     *************************************************************************/
    private class RepetitionSpinnerListener implements AdapterView.OnItemSelectedListener
    {
        private Card spinnerCard;

        public RepetitionSpinnerListener(Card spinnerCard)
        {
            this.spinnerCard = spinnerCard;
        }

        //implementation of listener for the spinner
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
        {
            Log.d(TAG, "  pos:" + pos);

            if(pos == HIGH_POS)
            {
                cardChooser.setHighRepetition(this.spinnerCard);
            }
            else if(pos == MED_POS)
            {
                cardChooser.setMediumRepetition(this.spinnerCard);
            }
            else if(pos == LOW_POS)
            {
                cardChooser.setLowRepetition(this.spinnerCard);
            }
            else if(pos == NEVER_POS)
            {
                cardChooser.setNeverRepetition(this.spinnerCard);
            }
            else
            {
                cardChooser.setHighRepetition(this.spinnerCard);
            }
        }

        //implementation of listener for the spinner
        public void onNothingSelected(AdapterView<?> parent)
        {
            //do nothing
        }
    }
}
