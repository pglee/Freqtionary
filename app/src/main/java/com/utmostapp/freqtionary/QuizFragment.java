package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

/**
 * Created by plee on 9/21/14.
 */
public class QuizFragment extends Fragment
{
    private static final String TAG = "QuizFragment";
    private WordChooser wordChooser;
    private TextView frequencyView;
    private TextView highTotalView;
    private TextView medTotalView;
    private TextView lowTotalView;
    private TextView neverTotalView;
    private RadioGroup repetitionChoice;
    private Button previousButton;
    private Button nextButton;
    private FrameLayout wordContainer;
    private NativeWordFragment nativeFragment;
    private ForeignWordFragment foreignFragment;

    private boolean isNativeShown;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View layout          =  inflater.inflate(R.layout.fragment_quiz, container, false);

        this.frequencyView   = (TextView)layout.findViewById(R.id.frequency_value);

        this.highTotalView   = (TextView)layout.findViewById(R.id.highCount);
        this.medTotalView    = (TextView)layout.findViewById(R.id.medCount);
        this.lowTotalView    = (TextView)layout.findViewById(R.id.lowCount);
        this.neverTotalView  = (TextView)layout.findViewById(R.id.neverCount);

        this.wordChooser     = WordChooser.getInstance(getActivity());
        this.nativeFragment  = NativeWordFragment.newInstance(this.wordChooser);
        this.foreignFragment = ForeignWordFragment.newInstance(this.wordChooser);
        this.wordContainer   = (FrameLayout)layout.findViewById(R.id.fragment_word_container);
        this.wordContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switchLanguage();
            }
        });

        this.repetitionChoice = (RadioGroup)layout.findViewById(R.id.repetitionChoice);
        this.repetitionChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                handleRadioChanged(wordChooser.getCurrentWord(), group, checkedId);
            }
        });

        this.previousButton = (Button)layout.findViewById(R.id.prev_button);
        this.previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                displayWordData(wordChooser.getPreviousWord());
            }
        });

        this.nextButton = (Button)layout.findViewById(R.id.next_button);
        this.nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                displayWordData(wordChooser.getNextWord());
            }
        });

        displayWordData(this.wordChooser.getCurrentWord());
        addChildFragment(this.foreignFragment);
        updateStatistics();

        return layout;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        this.wordChooser.saveWords(getActivity());
    }

    private void displayWordData(Word word)
    {
        if(this.isNativeShown)
            switchLanguage();

        this.nativeFragment.assignWord(word);
        this.foreignFragment.assignWord(word);
        word.assignRankText(this.frequencyView);

        if(word.isHigh())
            this.repetitionChoice.check(R.id.highChoice);
        else if(word.isMedium())
            this.repetitionChoice.check(R.id.mediumChoice);
        else if(word.isLow())
            this.repetitionChoice.check(R.id.lowChoice);
        else if(word.isNever())
            this.repetitionChoice.check(R.id.neverChoice);
    }

    public void handleRadioChanged(Word word, RadioGroup group, int checkedId)
    {
        switch(checkedId)
        {
            case R.id.highChoice:
                this.wordChooser.setHighRepetition(word);
                break;
            case R.id.mediumChoice:
                this.wordChooser.setMediumRepetition(word);
                break;
            case R.id.lowChoice:
                this.wordChooser.setLowRepetition(word);
                break;
            case R.id.neverChoice:
                this.wordChooser.setNeverFrequency(word);
                break;
            default:
                this.wordChooser.setHighRepetition(word);
        }

        updateStatistics();
    }

    private void updateStatistics()
    {
        this.highTotalView.setText(this.wordChooser.highTotal());
        this.medTotalView.setText(this.wordChooser.mediumTotal());
        this.lowTotalView.setText(this.wordChooser.lowTotal());
        this.neverTotalView.setText(this.wordChooser.neverTotal());
    }

    //switches between foreign and native word
    private void switchLanguage()
    {
        this.isNativeShown = !this.isNativeShown;

        FragmentManager fm              = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if(this.isNativeShown)
        {
            transaction.setCustomAnimations(
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out,
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out);
            transaction.remove(this.foreignFragment);
            transaction.remove(this.nativeFragment);
            transaction.add(R.id.fragment_word_container, this.nativeFragment);
            transaction.commit();
        }
        else
        {
            transaction.setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out);
            transaction.remove(this.foreignFragment);
            transaction.remove(this.nativeFragment);
            transaction.add(R.id.fragment_word_container, this.foreignFragment);
            transaction.commit();
        }
    }

    private void addChildFragment(Fragment fragmentToAdd)
    {
        FragmentManager fm = getFragmentManager();

        //fragmentContainer is defined in activity_fragment.xml layout
        Fragment fragment  = fm.findFragmentById(R.id.fragment_word_container);

        //add a fragment to this activity if it wasn't already
        if(fragment == null)
        {
            fragment = fragmentToAdd;
            fm.beginTransaction().add(R.id.fragment_word_container, fragment).commit();
        }
    }

    public static class ForeignWordFragment extends Fragment
    {
        private static final String WORD_CHOOSER = "ForeignWordFragment";
        private TextView foreignWordView;
        private WordChooser wordChooser;

        public static final ForeignWordFragment newInstance(WordChooser wordChooser)
        {
            ForeignWordFragment instance = new ForeignWordFragment();
            Bundle bundle                = new Bundle(1);
            bundle.putSerializable(WORD_CHOOSER, wordChooser);
            instance.setArguments(bundle);

            return instance;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            this.wordChooser = (WordChooser)getArguments().getSerializable(WORD_CHOOSER);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View layout          = inflater.inflate(R.layout.fragment_foreign_word, container, false);
            this.foreignWordView = (TextView)layout.findViewById(R.id.foreign_text);

            assignWord(this.wordChooser.getCurrentWord());

            return layout;
        }

        private void assignWord(Word word)
        {
            if(foreignWordView != null)
                word.assignForeignText(foreignWordView);
        }
    }

    public static class NativeWordFragment extends Fragment
    {
        private TextView nativeWordView;
        private WordChooser wordChooser;
        private static final String WORD_CHOOSER = "NativeWordFragment";

        public static final NativeWordFragment newInstance(WordChooser wordChooser)
        {
            NativeWordFragment instance = new NativeWordFragment();
            Bundle bundle               = new Bundle(1);
            bundle.putSerializable(WORD_CHOOSER, wordChooser);
            instance.setArguments(bundle);

            return instance;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            this.wordChooser = (WordChooser)getArguments().getSerializable(WORD_CHOOSER);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View layout          = inflater.inflate(R.layout.fragment_native_word, container, false);
            this.nativeWordView  = (TextView)layout.findViewById(R.id.native_text);

            assignWord(this.wordChooser.getCurrentWord());

            return layout;
        }

        private void assignWord(Word  word)
        {
            if(nativeWordView != null)
                word.assignNativeText(nativeWordView);
        }
    }
}
