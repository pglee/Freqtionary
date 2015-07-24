package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
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
public class FlashFragment extends Fragment
{
    private static final String TAG                 = "FlashFragment";
    private static final String DEFAULT_LESSON_FILE = "lesson1.json";

    private WordChooser wordChooser;
    private TextView highTotalView;
    private TextView medTotalView;
    private TextView lowTotalView;
    private TextView neverTotalView;
    private RadioGroup repetitionChoice;
    private TableLayout progressLayout;

    private ImageButton playButton;
    private Button previousButton;
    private Button nextButton;
    private FrameLayout wordContainer;
    private TopCardFragment topFragment;
    private BottomCardFragment bottomFragment;
    private AudioPlayer audioPlayer = new AudioPlayer();
    private Thread autoRunThread;

    private boolean isAutoRun;
    private boolean isSwitched;
    private boolean isTopShown;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View layout          = inflater.inflate(R.layout.fragment_flash, container, false);

        layout.setOnTouchListener(new FlashTouchListener());

        this.highTotalView   = (TextView)layout.findViewById(R.id.highCount);
        this.medTotalView    = (TextView)layout.findViewById(R.id.medCount);
        this.lowTotalView    = (TextView)layout.findViewById(R.id.lowCount);
        this.neverTotalView  = (TextView)layout.findViewById(R.id.neverCount);
        this.wordChooser     = WordChooser.getInstance(getActivity(), DEFAULT_LESSON_FILE);

        Intent intent = getActivity().getIntent();
        Lesson lesson = LessonListFragment.selectedLesson(intent);

        if(lesson != null)
        {
            Log.d(TAG, "new lesson found");
            lesson.activateLesson(getActivity(), wordChooser);
        }

        this.topFragment    = TopCardFragment.newInstance(this.wordChooser);
        this.bottomFragment = BottomCardFragment.newInstance(this.wordChooser);

        this.wordContainer = (FrameLayout)layout.findViewById(R.id.fragment_word_container);
        this.wordContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                flipCard();
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

        playButton = (ImageButton)layout.findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playAudio();
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

        this.progressLayout = (TableLayout)layout.findViewById(R.id.progressLayout);
        this.progressLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Progress layout clicked.");
                Intent intent = new Intent(getActivity(), WordListActivity.class);
                startActivity(intent);
            }
        });

        addChildFragment(this.bottomFragment);
        displayWordData(this.wordChooser.getCurrentWord());
        updateProgress();

        return layout;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.question, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean isCompleted = true;

        switch(item.getItemId())
        {
            case R.id.menu_item_reverse_card:
                Log.d(TAG, "menu_item_reverse_card clicked.");
                isSwitched = !isSwitched;

                if(isSwitched)
                {
                    isTopShown = false;
                    flipCard();
                }
                else
                {
                    isTopShown = true;
                    flipCard();
                }

                isCompleted = true;
                break;

            case R.id.menu_item_choose_lesson:
                Log.d(TAG, "menu_item_choose_lesson clicked.");
                updateProgress();
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                startActivity(intent);
                isCompleted = true;
                break;

            case R.id.menu_item_auto_run:
                Log.d(TAG, "menu_item_auto_run clicked.");
                isAutoRun = !isAutoRun;
                item.setChecked(isAutoRun);

                //wait for the existing thread to stop via the flag change.
                try
                {
                    if(this.autoRunThread != null && this.autoRunThread.isAlive())
                        this.autoRunThread.join();
                }
                catch(InterruptedException e)
                {
                    Log.d(TAG, "autoRunThread InterruptedException." + e);
                    this.autoRunThread = null;
                }

                //start a new thread if required
                if(isAutoRun)
                {
                    this.autoRunThread = new Thread(new AutoRun());
                    this.autoRunThread.start();
                }

                break;

            default:
                isCompleted = super.onOptionsItemSelected(item);
        }

        return isCompleted;
    }

    private void displayWordData(Word word)
    {
        if((this.isTopShown && !this.isSwitched) || (!this.isTopShown && this.isSwitched))
            flipCard();

        this.topFragment.assignWord(word);
        this.bottomFragment.assignWord(word);

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
                this.wordChooser.setNeverRepetition(word);
                break;
            default:
                this.wordChooser.setHighRepetition(word);
        }

        updateProgress();
    }

    private void updateProgress()
    {
        this.highTotalView.setText(this.wordChooser.highTotal());
        this.medTotalView.setText(this.wordChooser.mediumTotal());
        this.lowTotalView.setText(this.wordChooser.lowTotal());
        this.neverTotalView.setText(this.wordChooser.neverTotal());
    }

    private void playAudio()
    {
        Word currentWord   = this.wordChooser.getCurrentWord();

        if(this.isTopShown)
            currentWord.nativeAudio(getActivity(), this.audioPlayer);
        else
            currentWord.foreignAudio(getActivity(), this.audioPlayer);

    }

    //reveals the answer
    private void flipCard()
    {
        this.isTopShown = !this.isTopShown;

        FragmentManager fm              = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if(this.isTopShown)
        {
            transaction.setCustomAnimations(
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out,
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out);
            transaction.remove(this.bottomFragment);
            transaction.remove(this.topFragment);
            transaction.add(R.id.fragment_word_container, this.topFragment);
            transaction.commit();

        }
        else
        {
            transaction.setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out);
            transaction.remove(this.bottomFragment);
            transaction.remove(this.topFragment);
            transaction.add(R.id.fragment_word_container, this.bottomFragment);
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

    public static class BottomCardFragment extends Fragment
    {
        private static final String WORD_CHOOSER = "BottomCardFragment";
        private TextView foreignWordView;
        private WordChooser wordChooser;

        public static final BottomCardFragment newInstance(WordChooser wordChooser)
        {
            BottomCardFragment instance = new BottomCardFragment();
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

    public static class TopCardFragment extends Fragment
    {
        private TextView nativeWordView;
        private WordChooser wordChooser;
        private static final String WORD_CHOOSER = "TopCardFragment";

        public static final TopCardFragment newInstance(WordChooser wordChooser)
        {
            TopCardFragment instance = new TopCardFragment();
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

    /*************************************************************
     * Listener class for touch event on the flash card
     *************************************************/
    public class FlashTouchListener implements View.OnTouchListener
    {
        private static final String TAG = "TouchListener";

        private VelocityTracker velocityTracker;

        public boolean onTouch(View view, MotionEvent event)
        {
            int index        = event.getActionIndex();
            int action       = event.getActionMasked();
            int pointerId    = event.getPointerId(index);
            boolean isActive = true;

            switch(action)
            {
                case MotionEvent.ACTION_DOWN:
                {
                    if(velocityTracker == null)
                        velocityTracker = VelocityTracker.obtain();
                    else
                        velocityTracker.clear();

                    velocityTracker.addMovement(event);
                    break;
                }

                case MotionEvent.ACTION_MOVE:
                {
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1000);
                    float velocity = VelocityTrackerCompat.getXVelocity(velocityTracker, pointerId);

                    Log.d(TAG, "X velocity: " + velocity);

                    //fling left
                    if(velocity < -8000)
                    {
                        Log.d(TAG, "fling left");

                        displayWordData(wordChooser.getNextWord());
                        isActive = false;
                    }

                    //fling right
                    else if (velocity > 8000)
                    {
                        Log.d(TAG, "fling right");

                        displayWordData(wordChooser.getPreviousWord());
                        isActive = false;
                    }

                    break;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                {
                    //return a velocity tracker object back to be re-used by others
                    velocityTracker.recycle();
                    velocityTracker = null;
                    break;
                }
            }

            return isActive;
        }
    }

    /*************************************************************
     * Listener class for the auto run thread
     *************************************************/
    private class AutoRun implements Runnable
    {
        public void run()
        {
            try
            {
                while(isActive())
                {
                    autoRunThread.sleep(4000);

                    //android requires that view objects can only be modified by the thread that originally created it
                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            flipCard();
                        }

                    });

                    if(isActive())
                    {
                        autoRunThread.sleep(2000);

                        getActivity().runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                flipCard();
                                displayWordData(wordChooser.getNextWord());
                            }
                        });
                    }
                }

                //show top card if auto run stopped
                if(!isTopShown || isSwitched)
                {
                    //android requires that view objects can only be modified by the thread that originally created it
                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            flipCard();
                        }

                    });
                }
            }
            catch(Exception e)
            {
                Log.d(TAG, "AutoRun stopped unexpectedly: " + e);
            }
        }

        private boolean isActive()
        {
            return (isAutoRun && autoRunThread != null && autoRunThread.isAlive());
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        isAutoRun = false;

        try
        {
            if (autoRunThread != null)
                autoRunThread.join();
        }
        catch(InterruptedException e)
        {
            Log.d(TAG, "onDestroy autoRunThread InterruptedException." + e);
            this.autoRunThread = null;
        }

        this.wordChooser.saveWords(getActivity());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        displayWordData(this.wordChooser.getCurrentWord());
        updateProgress();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        audioPlayer.stop();
    }
}
