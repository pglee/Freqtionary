package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.FrameLayout;
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

    private CardChooser cardChooser;
    private TextView highTotalView;
    private TextView medTotalView;
    private TextView lowTotalView;
    private TextView neverTotalView;
    private RadioGroup repetitionChoice;
    private TableLayout progressLayout;

    //private ImageButton playButton;
    private Button previousButton;
    private Button nextButton;
    private FrameLayout wordContainer;
    private Fragment frontFragment;
    private Fragment backFragment;
    private AudioPlayer audioPlayer = new AudioPlayer();
    private Thread autoRunThread;

    private boolean isAutoRun = false;
    private boolean isAudioOn = true;
    private boolean isFrontShown = true;
    private boolean isReversed = false;

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
        this.cardChooser = CardChooser.getInstance(getActivity(), DEFAULT_LESSON_FILE);

        Intent intent = getActivity().getIntent();
        Lesson lesson = LessonListFragment.selectedLesson(intent);

        if(lesson != null)
        {
            Log.d(TAG, "new lesson found");
            lesson.activateLesson(getActivity(), cardChooser);
        }

        this.frontFragment = FrontCardFragment.newInstance(this.cardChooser);
        this.backFragment = BackCardFragment.newInstance(this.cardChooser);

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
                handleRadioChanged(cardChooser.getCurrentCard(), group, checkedId);
            }
        });

        this.previousButton = (Button)layout.findViewById(R.id.prev_button);
        this.previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                displayWordData(cardChooser.getPreviousCard());
                playAudio();
            }
        });

        this.nextButton = (Button)layout.findViewById(R.id.next_button);
        this.nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                displayWordData(cardChooser.getNextCard());
                playAudio();
            }
        });

        this.progressLayout = (TableLayout)layout.findViewById(R.id.progressLayout);
        this.progressLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Progress layout clicked.");
                Intent intent = new Intent(getActivity(), CardListActivity.class);
                startActivity(intent);
            }
        });

        addChildFragment(this.frontFragment);
        displayWordData(this.cardChooser.getCurrentCard());
        updateProgress();
        playAudio();

        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean isCompleted = true;

        switch(item.getItemId())
        {
            case R.id.menu_item_reverse_card:
                Log.d(TAG, "menu_item_reverse_card clicked.");
                Fragment hold      = this.frontFragment;
                this.frontFragment = this.backFragment;
                this.backFragment  = hold;
                this.isFrontShown  = !this.isFrontShown;
                this.isReversed    = !this.isReversed;

                item.setChecked(!item.isChecked());

                flipCard();

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

                isCompleted = true;
                break;

            case R.id.menu_item_audio_on:
                isAudioOn = !isAudioOn;
                item.setChecked(isAudioOn);

                isCompleted = true;
                break;

            default:
                isCompleted = super.onOptionsItemSelected(item);
        }

        return isCompleted;
    }

    private void displayWordData(Card card)
    {
        if(!this.isFrontShown)
            flipCard();

        ((ICardFragment)this.frontFragment).assignWord(card);

        if(card.isHigh())
            this.repetitionChoice.check(R.id.highChoice);
        else if(card.isMedium())
            this.repetitionChoice.check(R.id.mediumChoice);
        else if(card.isLow())
            this.repetitionChoice.check(R.id.lowChoice);
        else if(card.isNever())
            this.repetitionChoice.check(R.id.neverChoice);
    }

    public void handleRadioChanged(Card card, RadioGroup group, int checkedId)
    {
        switch(checkedId)
        {
            case R.id.highChoice:
                this.cardChooser.setHighRepetition(card);
                break;
            case R.id.mediumChoice:
                this.cardChooser.setMediumRepetition(card);
                break;
            case R.id.lowChoice:
                this.cardChooser.setLowRepetition(card);
                break;
            case R.id.neverChoice:
                this.cardChooser.setNeverRepetition(card);
                break;
            default:
                this.cardChooser.setHighRepetition(card);
        }

        updateProgress();
    }

    private void updateProgress()
    {
        this.highTotalView.setText(this.cardChooser.highTotal());
        this.medTotalView.setText(this.cardChooser.mediumTotal());
        this.lowTotalView.setText(this.cardChooser.lowTotal());
        this.neverTotalView.setText(this.cardChooser.neverTotal());
    }

    //reveals the answer
    private void flipCard()
    {
        FragmentManager fm              = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if(!this.isFrontShown)
        {
            transaction.setCustomAnimations(
                    R.animator.card_flip_down_in, R.animator.card_flip_down_out);

            transaction.replace(R.id.fragment_word_container, this.frontFragment);
            transaction.commit();
            this.isFrontShown = true;
        }
        else
        {
            transaction.setCustomAnimations(
                    R.animator.card_flip_up_in, R.animator.card_flip_up_out);

            transaction.replace(R.id.fragment_word_container, this.backFragment);
            transaction.commit();
            this.isFrontShown = false;
        }

        playAudio();
    }

    private void playAudio()
    {
        if(this.isAudioOn)
        {
            ICardFragment currentFragment;

            if(this.isFrontShown)
                currentFragment = ((ICardFragment)this.frontFragment);
            else
                currentFragment = ((ICardFragment)this.backFragment);

            currentFragment.playAudio(getActivity(), this.cardChooser.getCurrentCard(), this.audioPlayer);
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

    public static class FrontCardFragment extends Fragment implements ICardFragment
    {
        private TextView textView;
        private CardChooser cardChooser;
        private static final String WORD_CHOOSER = "FrontCardFragment";

        public static final FrontCardFragment newInstance(CardChooser cardChooser)
        {
            FrontCardFragment instance = new FrontCardFragment();
            Bundle bundle               = new Bundle(1);
            bundle.putSerializable(WORD_CHOOSER, cardChooser);
            instance.setArguments(bundle);

            return instance;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            this.cardChooser = (CardChooser)getArguments().getSerializable(WORD_CHOOSER);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View layout   = inflater.inflate(R.layout.fragment_foreign_word, container, false);
            this.textView = (TextView)layout.findViewById(R.id.foreign_text);

            assignWord(this.cardChooser.getCurrentCard());

            return layout;
        }

        public void assignWord(Card card)
        {
            if(textView != null)
                card.assignTopText(textView);
        }

        public void playAudio(Context context, Card currentCard, AudioPlayer audioPlayer)
        {
            currentCard.frontAudio(context, audioPlayer);
        }
    }

    public static class BackCardFragment extends Fragment implements ICardFragment
    {
        private static final String WORD_CHOOSER = "BackCardFragment";
        private TextView textView;
        private CardChooser cardChooser;

        public static final BackCardFragment newInstance(CardChooser cardChooser)
        {
            BackCardFragment instance = new BackCardFragment();
            Bundle bundle                = new Bundle(1);
            bundle.putSerializable(WORD_CHOOSER, cardChooser);
            instance.setArguments(bundle);

            return instance;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            this.cardChooser = (CardChooser)getArguments().getSerializable(WORD_CHOOSER);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View layout    = inflater.inflate(R.layout.fragment_native_word, container, false);
            this.textView  = (TextView)layout.findViewById(R.id.native_text);

            assignWord(this.cardChooser.getCurrentCard());

            return layout;
        }

        public void assignWord(Card card)
        {
            if(textView != null)
                card.assignBottomText(textView);
        }

        public void playAudio(Context context, Card currentCard, AudioPlayer audioPlayer)
        {
            currentCard.backAudio(context, audioPlayer);
        }
    }

    public interface ICardFragment
    {
        public void assignWord(Card card);
        public void playAudio(Context context, Card currentCard, AudioPlayer audioPlayer);
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

                        displayWordData(cardChooser.getNextCard());
                        isActive = false;
                    }

                    //fling right
                    else if (velocity > 8000)
                    {
                        Log.d(TAG, "fling right");

                        displayWordData(cardChooser.getPreviousCard());
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
                    autoRunThread.sleep(3000);

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
                                displayWordData(cardChooser.getNextCard());
                            }
                        });
                    }
                }

                //show top card if auto run stopped
                if(!isFrontShown)
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

        this.cardChooser.saveCards(getActivity());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        displayWordData(this.cardChooser.getCurrentCard());
        updateProgress();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        audioPlayer.stop();
    }
}
