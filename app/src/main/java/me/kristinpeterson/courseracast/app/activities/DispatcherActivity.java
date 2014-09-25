/**
 * 
 */
package me.kristinpeterson.courseracast.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import me.kristinpeterson.courseracast.app.CastApplication;
import me.kristinpeterson.courseracast.app.utils.SessionManager;
import me.kristinpeterson.courseracast.app.utils.Utils;

/**
 * @author kristinpeterson
 *
 */
public class DispatcherActivity extends Activity {

	private VideoCastManager mCastManager;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.CONTEXT = getApplicationContext();
        
        Class<?> activityClass;
        SessionManager session = new SessionManager(getApplicationContext());;
        Bundle b = getIntent().getExtras();
        
        // Initializing the Chromecast VideoCastManager for this Activity
     	mCastManager = CastApplication.getCastManager(this);
     	
		// Chromecast Session Recovery
		boolean showDialog = false;
		int timeoutInSeconds = 10;
		mCastManager.reconnectSessionIfPossible(this, showDialog,
				timeoutInSeconds);
        
		if(session.isLoggedIn()) {
			activityClass = CourseListActivity.class;
		} else {
			activityClass = LoginActivity.class;
		}
		
		Intent i = new Intent(this, activityClass);
		if(null != b) {
			i.putExtras(b);
		}
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
	
	@Override
	protected void onResume() {
		mCastManager = CastApplication.getCastManager(this);
		if (null != mCastManager) {
            mCastManager.incrementUiCounter();
        }
        super.onResume();
	}

	@Override
	protected void onPause() {
		mCastManager.decrementUiCounter();
        super.onPause();
	}
}