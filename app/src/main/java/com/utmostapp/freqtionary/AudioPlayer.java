package com.utmostapp.freqtionary;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Plays audio for the user
 *
 * Created by plee on 10/15/14.
 */
public class AudioPlayer
{
    private static final String TAG = "AudioPlayer";

    //utility to play audio
    private MediaPlayer mPlayer;

    /**********************************************************************************
     * Stops the currently playing audio if an audio is playing. Does nothing if no
     * audio is playing.
     ***********************************************************************************/
    public void stop()
    {
        if(mPlayer != null)
        {
            //release instead of stop to let go of this resource
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**********************************************************************************
     * Plays the audio stored int the fileName
     *
     * @param context The context defines where the file is located
     * @param fileName The name of the audio file to play
     ***********************************************************************************/
    public void play(Context context, String fileName)
    {
        stop();

        try
        {
            mPlayer = MediaPlayer.create(context, resourceId(context, fileName));

            //stop when audio is completed.
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                public void onCompletion(MediaPlayer mp)
                {
                    stop();
                }
            });

            //start only when media player is ready
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer)
                {
                    mediaPlayer.start();
                }
            });
        }
        catch(Exception e)
        {
            Log.d(TAG, "Unable to play the file." + e);
        }
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
