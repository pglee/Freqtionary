package com.utmostapp.freqtionary;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Loads and saves words from properly formatted JSON files
 * Created by plee on 10/24/14.
 */
public class LessonJSONSerializer implements Serializable
{
    private static final String TAG = "JSONSerializer";
    private static final String LESSON_NAME = "name";
    private static final String LESSON_DESC = "desc";

    /************************************************************************
     * Retrieves a lesson contained in the given filename. The content of the
     * filename is assumed to be in the proper JSON format
     *
     * @param context Used to get the file that contains the JSON data
     * @param fileName The file name stored in the app sandbox
     ************************************************************************/
    public static Lesson getLesson(Context context, String fileName) throws IOException, JSONException
    {
        BufferedReader reader = null;
        Lesson lesson         = null;

        try
        {
            //Open and read the file into a StringBuilder
            int resourceId                 = resourceId(context, fileName);
            InputStream inputStream        = getFileStream(context, fileName, resourceId);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            reader                         = new BufferedReader(streamReader);

            StringBuilder builder = new StringBuilder();
            String line;

            //line breaks are omitted and irrelevant
            while((line = reader.readLine()) != null)
            {
                builder.append(line);
                break;
            }

            if(line != null)
            {
                JSONTokener tokener = new JSONTokener(builder.toString());
                JSONArray array     = (JSONArray)tokener.nextValue();
                JSONObject json     = array.getJSONObject(0);

                lesson = new Lesson(fileName, json);

                Log.d(TAG, "first line: " + json);
            }
        }
        catch(Resources.NotFoundException e)
        {
            //Ignore this one; it happens when starting fresh
        }
        finally
        {
            if(reader != null)
                reader.close();
        }

        return lesson;
    }

    //helper to get the resource id with the given fileName
    private static int resourceId(Context context, String fileName)
    {
        return context.getResources().getIdentifier(resourceName(fileName), "raw", context.getPackageName());
    }

    //helper to get the resource name with the given fileName
    private static String resourceName(String fileName)
    {
        String resourceName = fileName.substring(0, fileName.lastIndexOf('.'));

        Log.d(TAG, "resourceName: "  + resourceName);
        return resourceName;
    }

    /************************************************************************
     *
     * @param context Used to get the file that contains the JSON data
     * @param fileName The file name stored in the app sandbox
     * @param resourceId The resourceId of the file that contains the initial
     *                   data to be loaded if this the first time loading data.
     ************************************************************************/
    public static ArrayList<Card> loadWords(Context context, String fileName, int resourceId) throws IOException, JSONException
    {
        ArrayList<Card> cardList = new ArrayList<Card>();
        BufferedReader reader    = null;

        try
        {
            //Open and read the file into a StringBuilder
            InputStream inputStream        = getFileStream(context, fileName, resourceId);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            reader                         = new BufferedReader(streamReader);

            StringBuilder builder = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null)
            {
                //line breaks are omitted and irrelevant
                builder.append(line);
            }

            //Parse the JSON using JSONTokener
            JSONTokener tokener = new JSONTokener(builder.toString());
            JSONArray array     = (JSONArray)tokener.nextValue();

            //Build the array of crimes from JSONObjects
            for(int index = 0; index < array.length(); index++)
            {
                JSONObject json = array.getJSONObject(index);

                if(json.length() > 2)
                {
                    Log.d(TAG, "Build JSON " + json);

                    cardList.add(new Card(json));
                }
                else
                    Log.d(TAG, "JSON is not valid " + json.toString());
            }
        }
        catch(Resources.NotFoundException e)
        {
            //Ignore this one; it happens when starting fresh
        }
        finally
        {
            if(reader != null)
                reader.close();
        }

        return cardList;
    }

    /************************************************************************
     * Retrieves the filestream used to read the given file
     *
     * @param context Used to get the file that contains the JSON data
     * @param fileName The file name stored in the app sandbox
     * @param resourceId The resourceId of the file that contains the initial
     *                   data to be loaded if this the first time loading data.
     ************************************************************************/
    private static InputStream getFileStream(Context context, String fileName, int resourceId) throws Resources.NotFoundException
    {
        InputStream inputStream;
        Log.d(TAG, "getFileStream fileName: " + fileName + " resourceId " + resourceId);

        try
        {
            inputStream = context.openFileInput(fileName);
            Log.d(TAG, "JSON data loaded from sandbox. " + fileName);
        }
        catch(FileNotFoundException e)
        {
            Log.d(TAG, "Sandbox file not found. Assuming initial load. Getting data from res directory.");
            inputStream = context.getResources().openRawResource(resourceId);
            Log.d(TAG, "JSON data loaded from resource." + resourceId);
        }

        return inputStream;
    }

    /************************************************************************
     * Saves the lesson as a JSON file with the data stored in the
     * passed cards
     *
     * @param context Used to get the file that contains the JSON data
     * @param lesson The lesson that the cards will be stored under
     * @param fileName The file name stored in the app sandbox
     * @param cards Contains the lesson data that needs to be saved
     ************************************************************************/

    public static void saveLesson(Context context, Lesson lesson, String fileName, ArrayList<Card> cards) throws JSONException
    {
        //Build an array in JSON
        JSONArray array = new JSONArray();

        lesson.addLesson(array);

        for(Card card : cards)
        {
            array.put(card.toJSON());
        }

        //Write the file to disk
        Writer writer = null;

        try
        {
            //automatically adds the path to the app's sandbox, creates the file, and opens it for writing
            OutputStream out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writer           = new OutputStreamWriter(out);
            writer.write(array.toString());
        }
        catch(Throwable t)
        {
            Log.e(TAG, "Failed to open/create JSON file.", t);
        }
        finally
        {
            try
            {
                if (writer != null)
                    writer.close();
            }
            catch(Throwable t)
            {
                Log.e(TAG, "writer.close() failed. ", t);
            }
        }
    }
}

