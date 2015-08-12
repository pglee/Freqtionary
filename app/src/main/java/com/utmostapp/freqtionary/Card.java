package com.utmostapp.freqtionary;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;


/**
 * Created by plee on 9/21/14.
 */
public class Card implements Serializable
{
    private static final String TAG         = "Card";
    private static final String RANK        = "rank";
    private static final String FRONT_TEXT  = "ftx";
    private static final String BACK_TEXT   = "btx";
    private static final String REPETITION  = "rep";

    private static final String FRONT_AUDIO = "fau";
    private static final String BACK_AUDIO  = "bau";


    public static final int HIGH   = 3;
    public static final int MEDIUM = 2;
    public static final int LOW    = 1;

    public static final int NEVER  = -1;

    private int rank;
    private String frontText;
    private String backText;

    private String frontAudioFile;
    private String backAudioFile;

    private int repetition;

    public Card(JSONObject json) throws JSONException
    {
        this(json.getInt(RANK), json.getString(FRONT_TEXT),
            json.getString(BACK_TEXT), json.getInt(REPETITION),
            optionalAudio(json, FRONT_AUDIO), optionalAudio(json, BACK_AUDIO));
    }

    public Card(int rank, String frontText, String backText, int repetition)
    {
        this(rank, frontText, backText, repetition, null, null);
    }

    public Card(int rank, String frontText, String backText, int repetition,
                String frontAudio, String backAudio)
    {
        this.rank       = rank;
        this.frontText  = frontText;
        this.backText   = backText;
        this.repetition = repetition;

        //optional. null if no audio exists.
        this.frontAudioFile = frontAudio;
        this.backAudioFile  = backAudio;
    }

    private static String optionalAudio(JSONObject json, String key) throws JSONException
    {
        String value;

        if(!json.isNull(key))
            value = json.getString(key).toLowerCase();
        else
            value = null;

        return value;
    }

    @Override
    public String toString()
    {
        return frontText + " : " + backText + " : " + repetition;
    }


    public JSONObject toJSON() throws JSONException
    {
        JSONObject json = new JSONObject();

        json.put(RANK, Integer.toString(rank));
        json.put(BACK_TEXT, backText);
        json.put(FRONT_TEXT, frontText);
        json.put(REPETITION, Integer.toString(repetition));

        return json;
    }

    public void frontAudio(Context context, AudioPlayer player)
    {
        if(frontAudioFile != null && frontAudioFile.length() > 0)
            player.play(context, frontAudioFile);
    }

    public void backAudio(Context context, AudioPlayer player)
    {
        if(backAudioFile != null && backAudioFile.length() > 0)
            player.play(context, backAudioFile);
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
        view.setText(frontText);
    }

    public void assignBottomText(TextView view)
    {
        view.setText(backText);
    }

    public void assignRankText(TextView view)
    {
        view.setText(Integer.toString(rank));
    }
}
