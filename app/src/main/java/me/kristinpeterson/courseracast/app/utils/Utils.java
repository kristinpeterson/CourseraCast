/**
 * 
 */
package me.kristinpeterson.courseracast.app.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

/**
 * @author kristinpeterson
 *
 */
public class Utils {
	
	/**
	 * The application context
	 * Set in onCreate() method of each Activity
	 */
	public static Context CONTEXT;
	
	/**
	 * Shows a (long) toast
	 * 
	 * @param context the context in which to display the toast
	 * @param msg the message to display via toast, as a string
	 */
	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	 /**
     * Shows a (long) toast.
     *
     * @param context the context in which to display the toast
     * @param resourceId the message to display via toast, as a resource ID
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }
    
	/**
	 * Gets the application version name
	 * 
	 * @param context the application context
	 * 
	 * @return the application version name, or null if an error is thrown
	 */
	public static String getVersion(Context context) {
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
