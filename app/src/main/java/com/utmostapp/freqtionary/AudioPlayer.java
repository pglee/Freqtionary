package com.utmostapp.freqtionary;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by plee on 10/15/14.
 */
public class AudioPlayer
{
    private static final String TAG = "AudioPlayer";
    private MediaPlayer mPlayer;

    public void stop()
    {
        if(mPlayer != null)
        {
            //release instead of stop to let go of this resource
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void play(Context context, String fileName)
    {
        stop();
        mPlayer = MediaPlayer.create(context, resourceId(context, fileName));
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mp)
            {
                stop();
            }
        });

        mPlayer.start();
    }

    //gets the resourceId based on the fileName passed of the actual file
    private int resourceId(Context context, String fileName)
    {
        int resourceId =  context.getResources().getIdentifier(resourceName(fileName), "raw", context.getPackageName());

        Log.d(TAG, "resourceId: " + resourceId);

        return resourceId;
    }

    //simply removes the fileName extension
    private String resourceName(String fileName)
    {
        String resourceName = fileName.substring(0, fileName.lastIndexOf('.'));

        Log.d(TAG, "resourceName: " + resourceName);
        return resourceName;
    }
}
