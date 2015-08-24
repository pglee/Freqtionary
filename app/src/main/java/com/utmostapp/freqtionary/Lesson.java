package com.utmostapp.freqtionary;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by plee on 6/17/15.
 */
public class Lesson implements Serializable
{
    private static final String TAG = "Lesson";
    private static final String LESSON_NAME = "name";
    private static final String LESSON_DESC = "desc";

    private String fileName;
    private String name;
    private String description;


    public Lesson(String fileName, JSONObject json) throws JSONException
    {
        this.fileName = fileName;
        this.name        = getValue(json, LESSON_NAME, fileName);
        this.description = getValue(json, LESSON_DESC, fileName);
    }

    public Lesson(String fileName)
    {
        this.fileName    = fileName;
        this.name        = fileName;
        this.description = fileName;
    }

    public Lesson(String fileName, String name, String description)
    {
        this.fileName    = fileName;
        this.name        = name;
        this.description = description;
    }

    public String toString()
    {
        return "fileName: " + fileName + "\nname: " + name +"\ndescription: " + description;
    }

    private static String getValue(JSONObject json, String key, String defaultValue) throws JSONException
    {
        String value;

        try
        {
            if (!json.isNull(key))
                value = json.getString(key);
            else
                value = defaultValue;
        }
        catch(JSONException e)
        {
            value = defaultValue;
        }

        return value;
    }

    public void addLesson(JSONArray array) throws JSONException
    {
        JSONObject json = new JSONObject();

        Log.d(TAG, "Adding lesson to save name:" + name + " desc:" + description);

        json.put(LESSON_NAME, name);
        json.put(LESSON_DESC, description);

        array.put(json);
    }

    public void activateLesson(Context context, CardChooser cardChooser)
    {
        //Activate the current lesson, but don't save it (i.e. pass null) Otherwise the newly selected lesson will be saved when you want the previous lesson.
        cardChooser.loadLesson(context, null, this.fileName);
    }

    /*****************************************************************************
     * Assigns values in this object to views
     *
     * @param view assigns the name value to the passed view
     ****************************************************************************/
    public void assignName(TextView view)
    {
        view.setText(name);
    }

    /*****************************************************************************
     * Assigns values in this object to views
     *
     * @param view assigns the description value to the passed view
     ****************************************************************************/
    public void assignDescription(TextView view)
    {
        view.setText(description);
    }
}
