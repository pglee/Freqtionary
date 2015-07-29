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
 * Created by plee on 11/3/14.
 */
public class WordListFragment extends ListFragment
{
    private static final String TAG    = "WordListFragment";
    private static final String DEFAULT_LESSON_FILE = "lesson1.json";

    private static final int HIGH_POS  = 0;
    private static final int MED_POS   = 1;
    private static final int LOW_POS   = 2;
    private static final int NEVER_POS = 3;

    private WordChooser wordChooser;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.wordChooser         = WordChooser.getInstance(getActivity(), DEFAULT_LESSON_FILE);
        WordArrayAdapter adapter = new WordArrayAdapter(this.wordChooser.getMasterList());

        setListAdapter(adapter);
    }

    //Builds the custom view for the fragment by populating individual items
    private class WordArrayAdapter extends ArrayAdapter<Word>
    {
        public WordArrayAdapter(ArrayList<Word> words)
        {
            super(getActivity(), 0, words);
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

            Word word              = getItem(position);
            TextView rank          = (TextView)view.findViewById(R.id.rank);
            TextView primaryWord   = (TextView)view.findViewById(R.id.primary_word);
            TextView secondaryWord = (TextView)view.findViewById(R.id.secondary_word);
            Spinner repSpinner     = (Spinner)view.findViewById(R.id.repetition_spinner);

            word.assignRankText(rank);
            word.assignTopText(primaryWord);
            word.assignBottomText(secondaryWord);
            setSpinner(repSpinner, word);

            RepetitionSpinnerListener listener = new RepetitionSpinnerListener(word);
            repSpinner.setOnItemSelectedListener(listener);

            return view;
        }

        //sets the selection item for the spinner
        private void setSpinner(Spinner repSpinner, Word word)
        {
            if(word.isHigh())
            {
                repSpinner.setSelection(HIGH_POS);
            }
            else if(word.isMedium())
            {
                repSpinner.setSelection(MED_POS);
            }
            else if(word.isLow())
            {
                repSpinner.setSelection(LOW_POS);
            }
            else if(word.isNever())
            {
                repSpinner.setSelection(NEVER_POS);
            }
            else
                repSpinner.setSelection(HIGH_POS);
        }
    }

    private class RepetitionSpinnerListener implements AdapterView.OnItemSelectedListener
    {
        private Word spinnerWord;

        public RepetitionSpinnerListener(Word spinnerWord)
        {
            this.spinnerWord = spinnerWord;
        }

        //implementation of listener for the spinner
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
        {
            Log.d(TAG, "  pos:" + pos);

            if(pos == HIGH_POS)
            {
                wordChooser.setHighRepetition(this.spinnerWord);
            }
            else if(pos == MED_POS)
            {
                wordChooser.setMediumRepetition(this.spinnerWord);
            }
            else if(pos == LOW_POS)
            {
                wordChooser.setLowRepetition(this.spinnerWord);
            }
            else if(pos == NEVER_POS)
            {
                wordChooser.setNeverRepetition(this.spinnerWord);
            }
            else
            {
                wordChooser.setHighRepetition(this.spinnerWord);
            }
        }

        //implementation of listener for the spinner
        public void onNothingSelected(AdapterView<?> parent)
        {
            //do nothing
        }
    }
}
