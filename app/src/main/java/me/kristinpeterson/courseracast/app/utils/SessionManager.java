/**
 * 
 */
package me.kristinpeterson.courseracast.app.utils;

import java.util.HashMap;

import me.kristinpeterson.courseracast.app.activities.LoginActivity;
import me.kristinpeterson.courseracast.app.fragments.CurrentCoursesFragment;
import me.kristinpeterson.courseracast.app.fragments.PastCoursesFragment;
import me.kristinpeterson.courseracast.app.models.courses.CourseDAO;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.loopj.android.http.PersistentCookieStore;

/**
 * @author kristinpeterson
 *
 */
public class SessionManager {
	
    private SharedPreferences pref;
    private Editor editor;
    private Context mContext;
    private BroadcastReceiver mLogoutReceiver;
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static int PRIVATE_MODE = 0;
	private static String SESSION_PREF_FILENAME = "session_pref";
    /**
     * Key for session email
     */
    public static final String KEY_EMAIL = "email";
    /**
     * Key for session user password
     */
    public static final String KEY_PASSWORD = "password";
    /**
     * Key for session app version id
     */
    public static final String KEY_VERSION = "version";
    /**
     * Key for session user id
     */
    public static final String KEY_USER_ID = "userId";
    
    /**
     * Overloaded constructor for SessionManager, gets shared preference
     * file and prepares Editor to edit prefs
     * 
     * @param context the context in which the SessionManager is created
     */
    @SuppressLint("CommitPrefEdits")
	public SessionManager(Context context){
        this.mContext = context;
        pref = mContext.getSharedPreferences(SESSION_PREF_FILENAME, PRIVATE_MODE);
        editor = pref.edit();
    }
     
    /**
     * Create login session
     * 
     * @param email the email for the current user
     * @param version the application version
     * @param password user password
     * @param userId user id
     */
    public void createLoginSession(String email, String password, String version, String userId){
        // Storing login value as TRUE
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
         
        // Storing email in pref
        editor.putString(KEY_EMAIL, email);
        
        // Storing password in pref
        editor.putString(KEY_PASSWORD, password);
        
        // Storing userID in pref
        editor.putString(KEY_USER_ID, userId);
        
        // Storing app version in pref
        editor.putString(KEY_VERSION, version);
         
        // commit changes
        editor.commit();
    }   
     
    /**
     * Checks user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(mContext, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             
            // Staring Login Activity
            mContext.startActivity(i);
        }
         
    }
     
       
    /**
     * Get stored session data
     * 
     * @return a hash of the user details, in this case email only
     */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
         
        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        
        // user password
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        
        // user id
        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));
        
        // app version
        user.put(KEY_VERSION, pref.getString(KEY_VERSION, null));
         
        // return user
        return user;
    }
     
    /**
     * Clears session & Course list object, and logs out user
     * 
     * @param forcedLogout true of logout was forced by error
     * @param reason reason for forced logout - null if focredLogout is false
     */
    public void logoutUser(boolean forcedLogout, String reason){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
        CourseDAO.COURSES.clear();
        CurrentCoursesFragment.CURRENT_COURSES.clear();
        PastCoursesFragment.PAST_COURSES.clear();
        Intent broadcastIntent = new Intent();
    	broadcastIntent.setAction("me.kristinpeterson.courseracast.app.activities.ACTION_LOGOUT");
    	broadcastIntent.putExtra("forced_logout", forcedLogout);
    	broadcastIntent.putExtra("forced_logout_reason", reason);
    	mContext.sendBroadcast(broadcastIntent);
    }
     
    /**
     * Checks if user is logged in
     * 
     * @return true if user is logged in
     */
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    

	/**
	 * Sets and registers the logout receiver for this activity
	 * 
	 * @param activity the activity being finished
	 */
	public void setLogoutReceiver(final Activity activity) {
		 mLogoutReceiver = new BroadcastReceiver() {
		        @Override
		        public void onReceive(Context context, Intent intent) {
		            (new PersistentCookieStore(context)).clear();
		            Intent i = new Intent(context, LoginActivity.class);
		            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            i.putExtra("forced_logout", intent.getBooleanExtra("forced_logout", false));
		            i.putExtra("forced_logout_reason", intent.getStringExtra("forced_logout_reason"));
		            context.startActivity(i);
		            activity.finish();
		        }
		    }; 
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("me.kristinpeterson.courseracast.app.activities.ACTION_LOGOUT");
	    activity.registerReceiver(mLogoutReceiver, intentFilter);
	}
	
	/**
	 * Returns the BroadcastReceiver for ACTION_LOGOUT the activity
	 * 
	 * @return the logout receiver
	 */
	public BroadcastReceiver getLogoutReceiver() {
		return mLogoutReceiver;
	}
}