package com.utmostapp.freqtionary;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by plee on 9/21/14.
 */
public class WordChooser implements Serializable
{
    private static final String TAG = "WordChooser";

    private static WordChooser singleton;

    private ArrayList<Word> masterList;
    private ArrayList<Word> highList;
    private ArrayList<Word> medList;
    private ArrayList<Word> lowList;
    private ArrayList<Word> neverList;
    private ArrayList<Word> historyList = new ArrayList<Word>();
    
    private int historyIndex   = 0;
    private int newWordCounter = 0;
    private String fileName;
    private Word lessonComplete;

    private WordChooser(Context context, String newFileName)
    {
        loadLesson(context, newFileName);

        this.lessonComplete = new Word(0, context.getResources().getString(R.string.lesson_complete), "Lesson Complete!", Word.NEVER);

    }

    //factory method
    public static WordChooser getInstance(Context context, String newFileName)
    {
        if(singleton == null)
        {
            Log.d(TAG, "Creating a new instance of WordChooser. " + newFileName);
            singleton = new WordChooser(context, newFileName);
        }

        return singleton;
    }

    //loads the lesson from the file system either from the sandbox if saved before or from the raw resource
    public void loadLesson(Context context, String newFileName)
    {
        saveWords(context);
        ArrayList<Word> holdList;

        try
        {
            Log.d(TAG, "newFileName " + newFileName);

            int resourceId = context.getResources().getIdentifier(resourceName(newFileName), "raw", context.getPackageName());
            holdList       = WordJSONSerializer.loadWords(context, newFileName, resourceId);

            loadLists(holdList);

            this.masterList = holdList;
            this.fileName   = newFileName;

            this.historyIndex   = 0;
            this.newWordCounter = 0;
            this.historyList.clear();

            Log.d(TAG, "JSON load success. ");
        }
        catch(Exception exception)
        {
            Log.e(TAG, "JSON load failed.", exception);
        }
    }

    //save words to the sandbox
    public boolean saveWords(Context context)
    {
        boolean isOkay;

        if(this.fileName != null && this.fileName.length() > 0)
        {
            try
            {
                WordJSONSerializer.saveWords(context, this.fileName, this.masterList);
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

    private String resourceName(String fileName)
    {
        String resourceName = fileName.substring(0, fileName.lastIndexOf('.'));

        Log.d(TAG, "resourceName: "  + resourceName);
        return resourceName;
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
    public void setNeverRepetition(Word word)
    {
        removeFromRepetitionLists(word);
        word.setNever();
        this.neverList.add(word);
        Log.d(TAG, "setNeverFrequency");
    }

    public ArrayList<Word> getMasterList()
    {
        return this.masterList;
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
        int lastIndex = this.historyList.size() - 1;

        //If we are still traversing history
        if(this.historyIndex < lastIndex)
        {
            this.historyIndex++;
            word = historyList.get(this.historyIndex);
            Log.d(TAG, "Getting the next word in history");
        }

        //get a new word
        else
        {
            Word currentWord = historyList.get(lastIndex);
            word             = getNewWord();

            //don't add to history if the new word is the same as the current word
            if(word != currentWord && word != this.lessonComplete)
            {
                historyList.add(word);
                this.historyIndex = this.historyList.size() - 1;
                Log.d(TAG, "Getting a new word and putting it in history");
            }

            //if lesson complete then force last word as the previous word in history
            //else if(word == this.lessonComplete)
              //  this.historyIndex = this.historyList.size();
        }

        return word;
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
    private Word getNewWord()
    {
        this.newWordCounter++;
        Word word;

        int highRepRate = 1;
        int totalHighWords = this.highList.size();
        int totalMedWords  = this.medList.size();
        int medRepRate     = calculateRepRate(totalHighWords);
        int lowRepRate     = calculateRepRate(totalHighWords + totalMedWords);

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

    //gets the first word from the list and then puts it last in the list
    private static Word getWord(ArrayList<Word> list)
    {
        //remove from the top and add to the bottom of the list
        Word word = list.remove(0);
        list.add(word);

        return word;
    }

    //true if the passed repetition rate is the current rate
    private boolean isCurrentRate(int repetitionRate, ArrayList<Word> list)
    {
        return (list != null && list.size() > 0 && (repetitionRate == 0 || (this.newWordCounter % repetitionRate) == 0));
    }
}
