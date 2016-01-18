package com.utmostapp.freqtionary;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;


/**
 * Contains the main information displayed to the user. This
 * is the data that the user is attempting to memorize. Each
 * card also has a ranking associated with it. A Card has
 * a front which generally is the definition of the
 * item to memorize, and a back
 * which is the item to memorize.
 *
 * Created by plee on 9/21/14.
 */
public class Card implements Serializable
{
    //these could be enums but since it's internal it's not necessary
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

    /********************************************************************************
     * Initializes a card with the data found in the JSON object
     * @param json Contains the data to populate the card with
     * @throws JSONException The json file could not be parsed based on the format.
     * Make sure the format is correct.
     ********************************************************************************/
    public Card(JSONObject json) throws JSONException
    {
        this(json.getInt(RANK), json.getString(FRONT_TEXT),
            json.getString(BACK_TEXT), json.getInt(REPETITION),
            optionalAudio(json, FRONT_AUDIO), optionalAudio(json, BACK_AUDIO));
    }

    /********************************************************************************
     * Initializes a card with the data found in the JSON object
     * @param rank The rank value of the card (or order)
     * @param frontText The front text to display
     * @param backText The back text to display
     * @param repetition The repetition frequency assigned to the card (e.g. high, med, low)
     ********************************************************************************/
    public Card(int rank, String frontText, String backText, int repetition)
    {
        this(rank, frontText, backText, repetition, null, null);
    }

    /********************************************************************************
     * Initializes a card with the data found in the JSON object
     * @param rank The rank value of the card (or order)
     * @param frontText The front text to display
     * @param backText The back text to display
     * @param repetition The repetition frequency assigned to the card (e.g. high, med, low)
     * @param frontAudio the front audio fileName to play
     * @param backAudio The back audio fileName to play
     ********************************************************************************/
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

    /********************************************************************************
     * Gets the optional audio file name
     *
     * @param json The JSON object that contains the audio file.
     * @param key The key for the audio file
     * @return The file name for the audio. Null if no filename is found
     ********************************************************************************/
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


    /********************************************************************************
     * Converts the object to a JSON Object
     * @return The JSON object representation of the object
     ********************************************************************************/
    public JSONObject toJSON() throws JSONException
    {
        JSONObject json = new JSONObject();

        json.put(FRONT_TEXT, frontText);
        json.put(BACK_TEXT, backText);
        json.put(REPETITION, Integer.toString(repetition));
        json.put(RANK, Integer.toString(rank));

        if(isFound(frontAudioFile))
            json.put(FRONT_AUDIO, frontAudioFile);

        if(isFound(backAudioFile))
            json.put(BACK_AUDIO, backAudioFile);

        return json;
    }

    /********************************************************************************
     * plays the audio of the front card if an audio is found
     *
     * @param context The context used to play the audio
     * @param player The player used to play the audio
     ********************************************************************************/
    public void frontAudio(Context context, AudioPlayer player)
    {
        if(isFound(frontAudioFile))
            player.play(context, frontAudioFile);
    }

    /********************************************************************************
     * plays the audio of the back card if an audio is found
     *
     * @param context The context used to play the audio
     * @param player The player used to play the audio
     ********************************************************************************/
    public void backAudio(Context context, AudioPlayer player)
    {
        if(isFound(backAudioFile))
            player.play(context, backAudioFile);
    }

    private boolean isFound(String text)
    {
        return (text != null && text.trim().length() > 0);
    }

    /********************************************************************************
     * Sets the repetition frequency to high
     ********************************************************************************/
    public void setHigh()
    {
        this.repetition = HIGH;
    }

    /********************************************************************************
     * Sets the repetition frequency to medium
     ********************************************************************************/
    public void setMedium()
    {
        this.repetition = MEDIUM;
    }

    /********************************************************************************
     * Sets the repetition frequency to low
     ********************************************************************************/
    public void setLow()
    {
        this.repetition = LOW;
    }

    /********************************************************************************
     * Sets the repetition frequency to never
     ********************************************************************************/
    public void setNever()
    {
        this.repetition = NEVER;
    }

    /********************************************************************************
     * @return true if the frequency is high
     ********************************************************************************/
    public boolean isHigh()
    {
        return this.repetition == HIGH;
    }

    /********************************************************************************
     * @return true if the frequency is medium
     ********************************************************************************/
    public boolean isMedium()
    {
        return this.repetition == MEDIUM;
    }

    /********************************************************************************
     * @return true if the frequency is low
     ********************************************************************************/
    public boolean isLow()
    {
        return this.repetition == LOW;
    }

    /********************************************************************************
     * @return true if the frequency is never
     ********************************************************************************/
    public boolean isNever()
    {
        return this.repetition == NEVER;
    }

    /********************************************************************************
     * Places the top text to the view object
     *
     * @param view The view to add the top text to
     ********************************************************************************/
    public void assignTopText(TextView view)
    {
        view.setText(frontText);
    }

    /********************************************************************************
     * Places the bottom text to the view object
     *
     * @param view The view to add the bottom text to
     ********************************************************************************/
    public void assignBottomText(TextView view)
    {
        view.setText(backText);
    }

    /********************************************************************************
     * Places the rank text to the view object
     *
     * @param view The view to add the rank text to
     ********************************************************************************/
    public void assignRankText(TextView view)
    {
        view.setText(Integer.toString(rank));
    }
}
