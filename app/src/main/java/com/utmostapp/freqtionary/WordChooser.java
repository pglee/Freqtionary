package com.utmostapp.freqtionary;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by plee on 9/21/14.
 */
public class WordChooser
{
    private static WordChooser singleton;
    private static final int LIST_TOTAL = 10;
    private Context mAppContext;
    private ArrayList<Word> wordList;
    private int currentIndex = 0;

    private WordChooser(Context appContext)
    {
        mAppContext   = appContext;
        this.wordList = new ArrayList<Word>();

        //DEBUG - dummy data
        for(int index = 0; index < LIST_TOTAL; index++)
        {
            Word word = new Word(index, "NativeWord " + index, "ForeignWord " + index, Word.HIGH);
            this.wordList.add(word);
        }
    }

    //factory method
    public static WordChooser getInstance(Context context)
    {
        if(singleton == null)
            singleton = new WordChooser(context.getApplicationContext());

        return singleton;
    }

    public Word getCurrentWord()
    {
        return wordList.get(this.currentIndex);
    }

    public Word getPreviousWord()
    {
        decrementIndex();

        return wordList.get(this.currentIndex);
    }

    public Word getNextWord()
    {
        incrementIndex();

        return wordList.get(this.currentIndex);
    }

    private void decrementIndex()
    {
        //this ensures the range stays within 0 and wordList size
        this.currentIndex = (wordList.size() + this.currentIndex - 1) % wordList.size();
    }

    private void incrementIndex()
    {
        //this ensures the range stays within 0 and wordList size
        this.currentIndex = (this.currentIndex + 1) % wordList.size();
    }
}
