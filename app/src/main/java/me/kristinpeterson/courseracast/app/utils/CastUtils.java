/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.kristinpeterson.courseracast.app.utils;

import java.io.IOException;

import me.kristinpeterson.courseracast.app.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

/**
 * A collection of utility methods, all static.
 */
public class CastUtils {

    /*
     * Making sure public utility methods remain static
     */
    private CastUtils() {
    }


    /**
     * Returns the screen/display size based on given context
     *
     * @param context the context used to obtain display size
     * 
     * @return the width and height of display
     */
    @SuppressWarnings("deprecation")
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        return new Point(width, height);
    }

    /**
     * Shows an error dialog with a given text message.
     *
     * @param context the context in which to show the given message
     * @param errorString the error message to show
     */
    public static final void showErrorDialog(Context context, String errorString) {
        new AlertDialog.Builder(context).setTitle(R.string.error)
                .setMessage(errorString)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /**
     * Shows an error dialog with a text provided by a resource ID
     *
     * @param context the context in which to show the given message
     * @param resourceId the string resource id for the error message to be displayed
     */
    public static final void showErrorDialog(Context context, int resourceId) {
        new AlertDialog.Builder(context).setTitle(R.string.error)
                .setMessage(context.getString(resourceId))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /**
     * Shows an "Oops" error dialog with a text provided by a resource ID
     *
     * @param context the context in which to show the dialogue
     * @param resourceId the string resource id for the error message to be displayed
     */
    public static final void showOopsDialog(Context context, int resourceId) {
        new AlertDialog.Builder(context).setTitle(R.string.oops)
                .setMessage(context.getString(resourceId))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(R.drawable.ic_action_alerts_and_states_warning)
                .create()
                .show();
    }

    /**
     * A utility method to handle a few types of exceptions that are commonly thrown by the cast
     * APIs in this library. It has special treatments for
     * {@link TransientNetworkDisconnectionException}, {@link NoConnectionException} and shows an
     * "Oops" dialog conveying certain messages to the user. The following resource IDs can be used
     * to control the messages that are shown:
     * <p>
     * <ul>
     * <li><code>R.string.connection_lost_retry</code></li>
     * <li><code>R.string.connection_lost</code></li>
     * <li><code>R.string.failed_to_perfrom_action</code></li>
     * </ul>
     *
     * @param context the context in which the dialogue is to be displayed
     * @param e the exception being handled
     */
    public static void handleException(Context context, Exception e) {
        int resourceId = 0;
        if (e instanceof TransientNetworkDisconnectionException) {
            // temporary loss of connectivity
            resourceId = R.string.connection_lost_retry;

        } else if (e instanceof NoConnectionException) {
            // connection gone
            resourceId = R.string.connection_lost;
        } else if (e instanceof RuntimeException ||
                e instanceof IOException ||
                e instanceof CastException) {
            // something more serious happened
            resourceId = R.string.failed_to_perfrom_action;
        } else {
            // well, who knows!
            resourceId = R.string.failed_to_perfrom_action;
        }
        if (resourceId > 0) {
            me.kristinpeterson.courseracast.app.utils.CastUtils.showOopsDialog(context, resourceId);
        }
    }

    /**
     * Gets the version of app.
     *
     * @param context the context being evaluated
     * @return the version of the application as a string
     */
    public static String getAppVersionName(Context context) {
        String versionString = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0 /* basic info */);
            versionString = info.versionName;
        } catch (Exception e) {
            // do nothing
        }
        return versionString;
    }

    /**
     * Shows a (long) toast
     *
     * @param context the context in which to show the toast message
     * @param msg the message to be displayed
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a (long) toast.
     *
     * @param context the context in which to show the toast message
     * @param resourceId the string resource id of the message to be displayed
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }
    
    /**
     * Builds MediaInfo object based on given parameters
     * 
     * @param title the title of the media
     * @param subTitle the subtitle of the media
     * @param studio the studio for the media
     * @param url the url for the media
     * @param imgUrl the image url for the media
     * @param bigImageUrl the big image url for the media
     * 
     * @return MediaInfo object based on given parameters
     */
    public static MediaInfo buildMediaInfo(String title,
            String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));
        
        String contentType = getVideoContentType(url);

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(contentType)
                .setMetadata(movieMetadata)
                .build();
    }
    
    /*
     * Takes given url and extracts file type extension
     * in order to construct and return the appropriate 
     * content type string
     * 
     * @param url the video url
     * @return the content type of the video
     */
    private static String getVideoContentType(String url) {
    	
    	int dot = url.lastIndexOf(".");
    	String fileType = url.substring(dot+1);
    	
    	if(fileType.equals("webm")) {
    		return "video/webm";
    	} else {
    		return "video/mp4";
    	}
    }

}