package com.utmostapp.freqtionary;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by plee on 9/21/14.
 */
public class WordChooser implements Serializable
{
    private static final int LIST_TOTAL  = 10;
    private static final String TAG      = "WordChooser";
    private static final String FILENAME = "words.json";

    
    private static WordChooser singleton;

    private ArrayList<Word> masterList;
    private ArrayList<Word> highList;
    private ArrayList<Word> medList;
    private ArrayList<Word> lowList;
    private ArrayList<Word> neverList;
    private ArrayList<Word> historyList = new ArrayList<Word>();
    
    private int historyIndex   = 0;
    private int newWordCounter = 0;
    private Word lessonComplete;

    private WordChooser(Context context)
    {
        try
        {
            this.masterList     = WordJSONSerializer.loadWords(context, FILENAME, R.raw.words);
            this.lessonComplete = new Word(0, context.getString(R.string.lesson_complete), "Lesson Complete!", Word.NEVER);
            Log.d(TAG, "JSON load success. " + this.masterList.size());

            loadLists(this.masterList);
        }
        catch(Exception exception)
        {
            Log.e(TAG, "JSON load failed.", exception);
        }
    }

    public String wordTotal()
    {
        return Integer.toString(masterList.size());
    }

    public String highTotal()
    {
        return Integer.toString(highList.size());
    }

    public String mediumTotal()
    {
        return Integer.toString(medList.size());
    }

    public String lowTotal()
    {
        return Integer.toString(lowList.size());
    }

    public String neverTotal()
    {
        return Integer.toString(neverList.size());
    }
    
    private void loadLists(ArrayList<Word> masterList)
    {
        this.highList  = new ArrayList<Word>();
        this.medList   = new ArrayList<Word>();
        this.lowList   = new ArrayList<Word>();
        this.neverList = new ArrayList<Word>();

        for(Word word: masterList)
        {
            if(word.isHigh())
                highList.add(word);
            else if(word.isMedium())
                medList.add(word);
            else if(word.isLow())
                lowList.add(word);
            else
                neverList.add(word);
        }
    }

    //factory method
    public static WordChooser getInstance(Context context)
    {
        if(singleton == null)
            singleton = new WordChooser(context);

        return singleton;
    }

    //save words to the file defined in the constructor
    public boolean saveWords(Context context)
    {
        boolean isOkay;

        try
        {
            WordJSONSerializer.saveWords(context, FILENAME, this.masterList);
            Log.d(TAG, "Crimes saved to file.");
            isOkay = true;
        }
        catch(Exception e)
        {
            Log.e(TAG, "ERROR saving words: ", e);
            isOkay = false;
        }

        return isOkay;
    }

    //purges the given word from all lists
    private boolean removeFromRepetitionLists(Word word)
    {
        return (this.highList.remove(word) ||
            this.medList.remove(word) ||
            this.lowList.remove(word) ||
            this.neverList.remove(word));
    }

    //sets the word in the high frequency rate
    public void setHighRepetition(Word word)
    {
        removeFromRepetitionLists(word);
        word.setHigh();
        this.highList.add(word);
        Log.d(TAG, "setHighFrequency");
    }

    //sets the word in the medium frequency rate
    public void setMediumRepetition(Word word)
    {
        removeFromRepetitionLists(word);
        word.setMedium();
        this.medList.add(word);
        Log.d(TAG, "setMediumFrequency");
    }

    //sets the word in the low frequency rate
    public void setLowRepetition(Word word)
    {
        removeFromRepetitionLists(word);
        word.setLow();
        this.lowList.add(word);
        Log.d(TAG, "setLowFrequency");
    }

    //sets the word in the never frequency rate
    public void setNeverFrequency(Word word)
    {
        removeFromRepetitionLists(word);
        word.setNever();
        this.neverList.add(word);
        Log.d(TAG, "setNeverFrequency");
    }

    //gets the first word from the list and then puts it last in the list
    private static Word getWord(ArrayList<Word> list)
    {
        //remove from the top and add to the bottom of the list
        Word word = list.remove(0);
        list.add(word);

        return word;
    }

    //get the current word
    public Word getCurrentWord()
    {
        Log.d(TAG, "Getting the current word in history: " + historyIndex);

        if(historyList.size() == 0)
        {
            historyList.add(getNewWord());
            this.historyIndex = this.historyList.size() - 1;
            Log.d(TAG, "Getting a new word and putting it in history");
        }

        return historyList.get(this.historyIndex);
    }

    //Get the previous word in history
    public Word getPreviousWord()
    {
        if((this.historyIndex - 1) > -1)
            this.historyIndex--;

        Log.d(TAG, "Getting the previous word in history: " + historyIndex);

        return historyList.get(this.historyIndex);
    }

    //Gets the next word in history or a new word if the last word in history is already reached
    public Word getNextWord()
    {
        Word word;

        //If we are still traversing history
        if(this.historyIndex + 1 < this.historyList.size())
        {
            this.historyIndex++;
            word = historyList.get(this.historyIndex);
            Log.d(TAG, "Getting the next word in history");
        }

        //get a new word
        else
        {
            Word currentWord = historyList.get(historyList.size() - 1);
            word             = getNewWord();

            //don't add to history if the new word is the same as the current word
            if(word != currentWord)
            {
                historyList.add(word);
                this.historyIndex = this.historyList.size() - 1;
            }

            Log.d(TAG, "Getting a new word and putting it in history");
        }

        return word;
    }

    //gets the word based on the frequency rate
    private Word getNewWord()
    {
        this.newWordCounter++;
        Word word;

        int highRepRate = 1;
        int medRepRate  = this.highList.size() * 2;
        int lowRepRate  = medRepRate * 2;

        Log.d(TAG, "Rates High: " + highRepRate + "       Med: " + medRepRate + "       Low: " + lowRepRate);

        if(isCurrentRate(lowRepRate, this.lowList))
        {
            Log.d(TAG, "Getting low frequency");
            word = getWord(this.lowList);
        }
        else if(isCurrentRate(medRepRate, this.medList))
        {
            Log.d(TAG, "Getting med frequency");
            word = getWord(this.medList);
        }
        else if(isCurrentRate(highRepRate, this.highList))
        {
            Log.d(TAG, "Getting high frequency");
            word = getWord(this.highList);
        }

        //assume highList is empty
        else
        {
            Log.d(TAG, "No more high frequency");

            // grab from the next frequent list
            if(this.medList.size() > 0)
            {
                Log.d(TAG, "Getting med freq");
                word = getWord(this.medList);
            }
            else if(this.lowList.size() > 0)
            {
                Log.d(TAG, "Getting low freq");
                word = getWord(this.lowList);
            }
            else
            {
                word = this.lessonComplete;
            }
        }

        return word;
    }

    //true if the passed repetition rate is the current rate
    private boolean isCurrentRate(int repetitionRate, ArrayList<Word> list)
    {
        return (list != null && list.size() > 0 && (repetitionRate == 0 || (this.newWordCounter % repetitionRate) == 0));
    }

}
