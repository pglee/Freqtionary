package com.utmostapp.freqtionary;

import android.content.Context;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;


/**
 * Created by plee on 9/21/14.
 */
public class Word implements Serializable
{
    private static final String RANK       = "rank";
    private static final String NATIVE     = "nat";
    private static final String FOREIGN    = "for";
    private static final String REPETITION = "rep";

    public static final int HIGH   = 3;
    public static final int MEDIUM = 2;
    public static final int LOW    = 1;
    public static final int NEVER  = -1;

    private int rank;
    private String topText;
    private String bottomText;
    private int repetition;

    public Word(JSONObject json) throws JSONException
    {
        this(json.getInt(RANK), json.getString(NATIVE), json.getString(FOREIGN), json.getInt(REPETITION));
    }

    @Override
    public String toString()
    {
        return topText + " : " + bottomText + " : " + repetition;
    }

    public Word(int rank, String bottomText, String topText, int repetition)
    {
        this.rank        = rank;
        this.bottomText  = bottomText;
        this.topText = topText;
        this.repetition  = repetition;
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject json = new JSONObject();

        json.put(RANK, Integer.toString(rank));
        json.put(NATIVE, bottomText);
        json.put(FOREIGN, topText);
        json.put(REPETITION, Integer.toString(repetition));

        return json;
    }

    public void nativeAudio(Context context, AudioPlayer player)
    {
        player.play(context, "test1.mp3");
    }

    public void foreignAudio(Context context, AudioPlayer player)
    {
        player.play(context, "test2.mp3");
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

    public void assignTopText(TextView view)
    {
        view.setText(topText);
    }

    public void assignBottomText(TextView view)
    {
        view.setText(bottomText);
    }

    public void assignRankText(TextView view)
    {
        view.setText(Integer.toString(rank));
    }
}
