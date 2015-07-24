package com.utmostapp.freqtionary;

import android.content.Context;
import android.widget.TextView;

import java.io.Serializable;

/**
 * Created by plee on 6/17/15.
 */
public class Lesson implements Serializable
{
    private String fileName;
    private String name;
    private String description;

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

    public void activateLesson(Context context, WordChooser wordChooser)
    {
        wordChooser.loadLesson(context, this.fileName);
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
