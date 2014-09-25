/**
 * 
 */
package me.kristinpeterson.courseracast.app.models.courses;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.loopj.android.http.TextHttpResponseHandler;
import me.kristinpeterson.courseracast.app.activities.CourseListActivity;
import me.kristinpeterson.courseracast.app.activities.LoginActivity;
import me.kristinpeterson.courseracast.app.net.CourseraRestClient;
import me.kristinpeterson.courseracast.app.utils.SessionManager;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kristinpeterson
 *
 */
public class CourseDAO extends Activity {
	
	private static TextHttpResponseHandler textResponseHandler = null;
	private static SessionManager session;
    // The app now fails to download courses as Coursera has adjusted the security settings of
    // the following URL, disallowing access.
	private static String COURSES_URL_ROOT = "https://www.coursera.org/maestro/api/topic/list_my?user_id=";
	
	/**
	 * A list of users past & current Courses
	 */
	public static List<Course> COURSES = new ArrayList<Course>();
	
	/**
	 * Gets course data from Coursera (String representation of JSON)
	 * 
	 * On Success: 
	 * - Initiates CompileCoursesTask() passing the String response
	 * as a JSON Array
	 * 
	 * On Failure:
	 * - If status code 401 & background login has not previously been performed:
	 * performs a background login using saved user session data, ultimately 
	 * bringing user back to course list in a logged in state
	 * - If status code 401 & background login has been previously performed:
	 * asks user to re-login, prevents infinite loop scenario if saved user session
	 * data is bad (ie. password change etc)
	 * - Else, direct user to login page showing network error dialog
	 * 
	 * @param context the context in which this method was called (for getting user session data)
	 * 
	 */
	public static void loadCourseData(Context context) {
		session = new SessionManager(context);
		String userId = session.getUserDetails().get(SessionManager.KEY_USER_ID);
		String coursesUrl = COURSES_URL_ROOT.concat(userId);
        Log.e("userid is", userId);
		// Create GET response handler
		textResponseHandler = new TextHttpResponseHandler() {

			 @Override
		     public void onStart() {
		         // Initiated the request
		     }

		     @Override
		     public void onSuccess(int i, Header[] header, final String response) {
		        // Successfully got a response
		    	CourseListActivity.mContext.runOnUiThread(new Runnable() {
		    	    @Override
		    	    public void run() {
				    	try {
							new CompileCoursesTask().execute(new JSONArray(response));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    	    }
		    	  }
		    	);
		    	// reset background login indicator 
		    	// in the event that loadCourses() was called during background login
		    	LoginActivity.BACKGROUND_LOGIN_PERFORMED = false;
		     }

			@Override
		     public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error)
		     {
                 Log.e("failure in coursesdao", responseBody);
                 Log.e("status code", String.valueOf(statusCode));
                 Log.e("headers", headers.toString());
		        // Response failed :(
				if(statusCode == 401 && !LoginActivity.BACKGROUND_LOGIN_PERFORMED) {
					LoginActivity.performBackgroundLogin();
				} else if(statusCode == 401) {
					forceLogout(LoginActivity.ARG_RE_LOGIN);
				} else {
					forceLogout(LoginActivity.ARG_NETWORK_ERROR);
				}
		     }

		     @Override
		     public void onProgress(int bytesWritten, int totalSize) {
		         // Progress notification
		     }

		     @Override
		     public void onFinish() {
		         // Completed the request (either success or failure)
		    	 textResponseHandler = null;
		     }
		 };
		 
		// Send GET request
		CourseraRestClient.get(coursesUrl, textResponseHandler);
	}
	
    
	/*
	 *	Forces user logout with the given reason
	 *	
	 *	Acceptable reasons:
	 * 	- LoginActivity.ARG_RE_LOGIN: Asks user to re-login
	 * 	- LoginActivity.ARG_NETWORK_ERROR: informs user of network error
	 */
	private static void forceLogout(String reason) {
    	SessionManager session = new SessionManager(CourseListActivity.mContext);
		session.logoutUser(true, reason);
	}
}
