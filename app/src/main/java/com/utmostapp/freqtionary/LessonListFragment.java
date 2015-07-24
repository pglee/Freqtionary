package com.utmostapp.freqtionary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


import com.utmostapp.freqtionary.dummy.DummyContent;

import java.util.ArrayList;

/**
 * A fragment representing a list of of Lessons
 */
public class LessonListFragment extends ListFragment
{
    private static final String TAG = "LessonListFragment";
    private static final String LESSON_TAG = "lesson";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LessonListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //the adapter loads the values for the view
        LessonArrayAdapter adapter = new LessonArrayAdapter(loadLessons());
        setListAdapter(adapter);
    }

    //builds the list of lessons
    //TO DO we need to put this outside of the code
    private ArrayList<Lesson> loadLessons()
    {
        ArrayList<Lesson> list = new ArrayList<Lesson>();

        Lesson lesson1 = new Lesson("lesson1.json", "Capitols", "US State Capitols");
        Lesson lesson2 = new Lesson("lesson2.json:", "English Lesson1", "10 Most Frequently used English Words");

        list.add(lesson1);
        list.add(lesson2);

        return list;
    }

    @Override
    /***********************************************************
     * Event listener for the list item click
     *
     * @param listView The list view object contains the items
     * @param view The view object
     * @param position The selected list item position in the list
     * @param id The id of the list item
     ************************************************************/
    public void onListItemClick(ListView listView, View view, int position, long id)
    {
        Lesson lesson = (Lesson)listView.getItemAtPosition(position);
        Intent intent = new Intent(getActivity(), FlashActivity.class);

        //pass the intent back to the FlashActivity
        intent.putExtra(LESSON_TAG, lesson);
        startActivity(intent);

        Log.d(TAG, lesson.toString());
    }

    /******************************************************************
     * Grabs the lesson selected and passed in the intent
     *
     * @param intent Contains the lesson passed by this class
     * @return Lesson object stored in the intent. null if none passed
     ******************************************************************/
    public static Lesson selectedLesson(Intent intent)
    {
        return (Lesson)intent.getSerializableExtra(LESSON_TAG);
    }

    //Builds the custom view for the fragment by populating individual items
    private class LessonArrayAdapter extends ArrayAdapter<Lesson>
    {
        public LessonArrayAdapter(ArrayList<Lesson> lessons)
        {
            super(getActivity(), 0, lessons);
        }

        @Override
        //populates the view for each list item
        public View getView(int position, View view, ViewGroup parent)
        {
            //if no view given, inflate one
            if(view == null)
            {
                view = getActivity().getLayoutInflater().inflate(R.layout.fragment_lesson_list, null);
            }

            //get selected item
            Lesson lesson = getItem(position);

            TextView nameView = (TextView)view.findViewById(R.id.lesson_name);
            TextView descView = (TextView)view.findViewById(R.id.lesson_description);

            //assign lesson values to the views
            lesson.assignName(nameView);
            lesson.assignDescription(descView);

            return view;
        }
    }
}
