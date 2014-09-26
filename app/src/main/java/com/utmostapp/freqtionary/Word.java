package com.utmostapp.freqtionary;

import android.view.View;
import android.widget.TextView;

/**
 * Created by plee on 9/21/14.
 */
public class Word
{
    public static final int HIGH   = 3;
    public static final int MEDIUM = 2;
    public static final int LOW    = 1;
    public static final int NEVER  = -1;

    private int frequency;
    private String nativeText;
    private String foreignText;
    private int repetition;

    public Word(int frequency, String nativeText, String foreignText, int repetition)
    {
        this.frequency   = frequency;
        this.nativeText  = nativeText;
        this.foreignText = foreignText;
        this.repetition  = repetition;
    }

    public void setHigh()
    {
        this.repetition = HIGH;
    }

    public void setMedium()
    {
        this.repetition = MEDIUM;
    }

    public void setLow()
    {
        this.repetition = LOW;
    }

    public void setNever()
    {
        this.repetition = NEVER;
    }

    public boolean isHigh()
    {
        return this.repetition == HIGH;
    }

    public boolean isMedium()
    {
        return this.repetition == MEDIUM;
    }

    public boolean isLow()
    {
        return this.repetition == LOW;
    }

    public boolean isNever()
    {
        return this.repetition == NEVER;
    }

    public void assignForeignText(TextView view)
    {
        view.setText(foreignText);
    }

    public void assignNativeText(TextView view)
    {
        view.setText(nativeText);
    }

    public void assignFrequencyText(TextView view)
    {
        view.setText(Integer.toString(frequency));
    }
}
