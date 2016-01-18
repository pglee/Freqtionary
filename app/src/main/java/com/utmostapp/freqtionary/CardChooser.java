package com.utmostapp.freqtionary;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Tool for selecting the next or previous card in the stack
 *
 * Created by plee on 9/21/14.
 */
public class CardChooser implements Serializable
{
    private static final String TAG = "CardChooser";

    private static CardChooser singleton;

    private ArrayList<Card> masterList;
    private ArrayList<Card> highList;
    private ArrayList<Card> medList;
    private ArrayList<Card> lowList;
    private ArrayList<Card> neverList;
    private ArrayList<Card> historyList = new ArrayList<Card>();
    
    private int historyIndex   = 0;
    private int newCardCounter = 0;
    private String fileName;
    private Card lessonComplete;

    private CardChooser(Context context, Lesson prevLesson, String newFileName)
    {
        loadLesson(context, prevLesson, newFileName);

        this.lessonComplete = new Card(0, context.getResources().getString(R.string.lesson_complete), "Lesson Complete!", Card.NEVER);

    }

    /************************************************************************
     * factory method
     * @param context Used to save and retrieve resources
     * @param prevLesson The lesson that was being used previous to this one. Saves the lesson.
     * @param newFileName The file name that contains the new lesson.
     * @return Instance of the CardChooser
     *************************************************************************/
    public static CardChooser getInstance(Context context, Lesson prevLesson, String newFileName)
    {
        if(singleton == null)
        {
            Log.d(TAG, "Creating a new instance of CardChooser. " + newFileName);
            singleton = new CardChooser(context, prevLesson, newFileName);
        }

        return singleton;
    }

    /************************************************************************
     * loads the lesson from the file system either from the sandbox if saved before or from the raw resource
     * @param context Used to save and retrieve resources
     * @param prevLesson The lesson that was being used previous to this one. Saves the lesson.
     * @param newFileName The file name that contains the new lesson.
     *************************************************************************/
    public void loadLesson(Context context, Lesson prevLesson, String newFileName)
    {
        Log.d(TAG, "loadLesson. Saving cards " + prevLesson);

        saveCards(context, prevLesson);

        ArrayList<Card> holdList;

        try
        {
            Log.d(TAG, "newFileName " + newFileName);

            int resourceId = context.getResources().getIdentifier(resourceName(newFileName), "raw", context.getPackageName());
            holdList       = LessonJSONSerializer.loadWords(context, newFileName, resourceId);

            loadLists(holdList);

            this.masterList = holdList;
            this.fileName   = newFileName;

            this.historyIndex   = 0;
            this.newCardCounter = 0;
            this.historyList.clear();

            Log.d(TAG, "JSON load success. ");
        }
        catch(Exception exception)
        {
            Log.e(TAG, "JSON load failed.", exception);
        }
    }

    /************************************************************************
     * save words to the sandbox
     * @param context Used to save and retrieve resources
     * @param lesson The lesson to save
     *************************************************************************/
    public boolean saveCards(Context context, Lesson lesson)
    {
        boolean isOkay;

        if(lesson != null && this.fileName != null && this.fileName.length() > 0)
        {
            try
            {
                Log.d(TAG, "saveCards." + this.fileName + " lesson: " + lesson);

                LessonJSONSerializer.saveLesson(context, lesson, this.fileName, this.masterList);
                Log.d(TAG, "Words saved to file.");
                isOkay = true;
            } catch (Exception e)
            {
                Log.e(TAG, "ERROR saving words: ", e);
                isOkay = false;
            }
        }
        else
            isOkay = true;

        return isOkay;
    }

    //utility to get the resource name of the file
    private String resourceName(String fileName)
    {
        String resourceName = fileName.substring(0, fileName.lastIndexOf('.'));

        Log.d(TAG, "resourceName: "  + resourceName);
        return resourceName;
    }

    //The sum of all items with a high frequency
    public String highTotal()
    {
        return Integer.toString(highList.size());
    }

    //The sum of all items with a medium frequency
    public String mediumTotal()
    {
        return Integer.toString(medList.size());
    }

    //The sum of all items with a low frequency
    public String lowTotal()
    {
        return Integer.toString(lowList.size());
    }

    //The sum of all items with a never frequency
    public String neverTotal()
    {
        return Integer.toString(neverList.size());
    }
    
    private void loadLists(ArrayList<Card> masterList)
    {
        this.highList  = new ArrayList<Card>();
        this.medList   = new ArrayList<Card>();
        this.lowList   = new ArrayList<Card>();
        this.neverList = new ArrayList<Card>();

        for(Card card : masterList)
        {
            if(card.isHigh())
                highList.add(card);
            else if(card.isMedium())
                medList.add(card);
            else if(card.isLow())
                lowList.add(card);
            else
                neverList.add(card);
        }
    }

    //purges the given card from all lists
    private boolean removeFromRepetitionLists(Card card)
    {
        return (this.highList.remove(card) ||
            this.medList.remove(card) ||
            this.lowList.remove(card) ||
            this.neverList.remove(card));
    }

    //sets the card in the high frequency rate
    public void setHighRepetition(Card card)
    {
        removeFromRepetitionLists(card);
        card.setHigh();
        this.highList.add(card);
        Log.d(TAG, "setHighFrequency");
    }

    //sets the card in the medium frequency rate
    public void setMediumRepetition(Card card)
    {
        removeFromRepetitionLists(card);
        card.setMedium();
        this.medList.add(card);
        Log.d(TAG, "setMediumFrequency");
    }

    //sets the card in the low frequency rate
    public void setLowRepetition(Card card)
    {
        removeFromRepetitionLists(card);
        card.setLow();
        this.lowList.add(card);
        Log.d(TAG, "setLowFrequency");
    }

    //sets the card in the never frequency rate
    public void setNeverRepetition(Card card)
    {
        removeFromRepetitionLists(card);
        card.setNever();
        this.neverList.add(card);
        Log.d(TAG, "setNeverFrequency");
    }

    //The master list of all cards
    public ArrayList<Card> getMasterList()
    {
        return this.masterList;
    }

    //get the current word
    public Card getCurrentCard()
    {
        Log.d(TAG, "Getting the current word in history: " + historyIndex);

        if(historyList.size() == 0)
        {
            historyList.add(getNewCard());
            this.historyIndex = this.historyList.size() - 1;
            Log.d(TAG, "Getting a new word and putting it in history");
        }

        return historyList.get(this.historyIndex);
    }

    //Get the previous word in history
    public Card getPreviousCard()
    {
        if((this.historyIndex - 1) > -1)
            this.historyIndex--;

        Log.d(TAG, "Getting the previous word in history: " + historyIndex);

        return historyList.get(this.historyIndex);
    }

    //Gets the next word in history or a new word if the last word in history is already reached
    public Card getNextCard()
    {
        Card card;
        int lastIndex = this.historyList.size() - 1;

        //If we are still traversing history
        if(this.historyIndex < lastIndex)
        {
            this.historyIndex++;
            card = historyList.get(this.historyIndex);
            Log.d(TAG, "Getting the next card in history");
        }

        //get a new card
        else
        {
            Card currentCard = historyList.get(lastIndex);
            card = getNewCard();

            //don't add to history if the new card is the same as the current card
            if(card != currentCard && card != this.lessonComplete)
            {
                historyList.add(card);
                this.historyIndex = this.historyList.size() - 1;
                Log.d(TAG, "Getting a new card and putting it in history");
            }

            //if lesson complete then force last card as the previous card in history
            //else if(card == this.lessonComplete)
              //  this.historyIndex = this.historyList.size();
        }

        return card;
    }

    private int calculateRepRate(int baseSize)
    {
        int medRate;

        //if base size is 1 then med Rep Rate should be equal to the base RepRate
        //otherwise it should be double the size of the base List
        if(baseSize > 1)
            medRate = baseSize + 1;
        else
            medRate = 2;

        return medRate;
    }

    //gets the word based on the frequency rate
    private Card getNewCard()
    {
        this.newCardCounter++;
        Card card;

        int highRepRate = 1;
        int totalHighCards = this.highList.size();
        int totalMedCards  = this.medList.size();
        int medRepRate     = calculateRepRate(totalHighCards);
        int lowRepRate     = calculateRepRate(totalHighCards + totalMedCards);

        Log.d(TAG, "Rates High: " + highRepRate + "       Med: " + medRepRate + "       Low: " + lowRepRate);

        if(isCurrentRate(lowRepRate, this.lowList))
        {
            Log.d(TAG, "Getting low frequency");
            card = getCard(this.lowList);
        }
        else if(isCurrentRate(medRepRate, this.medList))
        {
            Log.d(TAG, "Getting med frequency");
            card = getCard(this.medList);
        }
        else if(isCurrentRate(highRepRate, this.highList))
        {
            Log.d(TAG, "Getting high frequency");
            card = getCard(this.highList);
        }

        //assume highList is empty
        else
        {
            Log.d(TAG, "No more high frequency");

            // grab from the next frequent list
            if(this.medList.size() > 0)
            {
                Log.d(TAG, "Getting med freq");
                card = getCard(this.medList);
            }
            else if(this.lowList.size() > 0)
            {
                Log.d(TAG, "Getting low freq");
                card = getCard(this.lowList);
            }
            else
            {
                card = this.lessonComplete;
            }
        }

        return card;
    }

    //gets the first word from the list and then puts it last in the list
    private static Card getCard(ArrayList<Card> list)
    {
        //remove from the top and add to the bottom of the list
        Card card = list.remove(0);
        list.add(card);

        return card;
    }

    //true if the passed repetition rate is the current rate
    private boolean isCurrentRate(int repetitionRate, ArrayList<Card> list)
    {
        return (list != null && list.size() > 0 && (repetitionRate == 0 || (this.newCardCounter % repetitionRate) == 0));
    }
}
