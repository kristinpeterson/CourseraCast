package me.kristinpeterson.courseracast.app;

import android.app.Application;
import android.content.Context;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;

/**
 * 
 */

/**
 * @author kristinpeterson
 *
 */
public class CastApplication extends Application {

	private static String APPLICATION_ID;
    private static VideoCastManager mCastMgr = null;

    /**
     * The increment by which the volume will be increased or decreased
     */
    public static final double VOLUME_INCREMENT = 0.05;
    
    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        APPLICATION_ID = getString(R.string.app_id);
        Utils.saveFloatToPreference(getApplicationContext(),
                VideoCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);

    }

    /**
     * Either initializes the VideoCastManager for the application or
     * sets the context for the existing VideoCastManager as given
     * 
     * @param context the context in which the VideoCastManager is being called
     * 
     * @return the Video Cast Manager set to the given context
     */
    public static VideoCastManager getCastManager(Context context) {
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(context, APPLICATION_ID,
                    null, null);
            mCastMgr.enableFeatures(
                    VideoCastManager.FEATURE_NOTIFICATION |
                            VideoCastManager.FEATURE_LOCKSCREEN |
                            VideoCastManager.FEATURE_DEBUGGING);
            mCastMgr.setStopOnDisconnect(true);

        }
        mCastMgr.setContext(context);
        /*String destroyOnExitStr = Utils.getStringFromPreference(context,
                CastPreference.TERMINATION_POLICY_KEY);
        mCastMgr.setStopOnDisconnect(null != destroyOnExitStr
                && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));*/
        return mCastMgr;
    }
	
}
