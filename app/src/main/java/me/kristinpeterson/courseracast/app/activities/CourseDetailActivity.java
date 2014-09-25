package me.kristinpeterson.courseracast.app.activities;

import me.kristinpeterson.courseracast.app.CastApplication;
import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.fragments.LectureListFragment;
import me.kristinpeterson.courseracast.app.models.courses.Course;
import me.kristinpeterson.courseracast.app.models.courses.CourseDAO;
import me.kristinpeterson.courseracast.app.models.lectures.LectureDAO;
import me.kristinpeterson.courseracast.app.utils.SessionManager;
import me.kristinpeterson.courseracast.app.utils.Utils;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

/**
 * An activity representing a single Course detail screen,
 * containing the Course logo, title & lecture list.
 */
public class CourseDetailActivity extends ActionBarActivity {

	private VideoCastManager mCastManager;
	private MiniController mMini;
    private IVideoCastConsumer mCastConsumer;
    private SessionManager session;
    private Course mCourse;
    private static Menu optionsMenu;
    private static MenuItem refreshButton;
    
    /**
	 * The Activity context
	 */
	public static CourseDetailActivity mContext;
    /**
     * Course Id tag for Bundle
     */
    public static String ARG_COURSE_ID = "course_id";
    /**
     * Indicates whether lecture list is loading
     */
    public static boolean LOADING;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Utils.CONTEXT = getApplicationContext();
		setContentView(R.layout.activity_course_detail);
		
		mContext = CourseDetailActivity.this;
		
		// Checking if user is logged in, redirecting to LoginActivity if not
        session = new SessionManager(Utils.CONTEXT);
		session.setLogoutReceiver(mContext);
        session.checkLogin();
        
		// Initializing the VideoCastManager for this Activity
		mCastManager = CastApplication.getCastManager(this);
		
		// Adding Chromecast mini controller
		mMini = (MiniController) findViewById(R.id.miniController1);
		mCastManager.addMiniController(mMini);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle b = getIntent().getExtras();
		
		if(null != b && null != b.getString(ARG_COURSE_ID)) 
				mCourse = CourseDAO.COURSES.get(Integer.parseInt(b.getString(ARG_COURSE_ID)));
		
		if (mCourse != null) {
			
			// Set image view for course logo
			ImageView courseLogo = (ImageView) findViewById(R.id.course_logo);
			courseLogo.setImageBitmap(mCourse.largeIconBitmap);		
			
			// Set text view for course title
			((TextView) findViewById(R.id.course_detail)).setText(mCourse.name);
			
			findViewById(R.id.loading).setVisibility(View.GONE);
		}
		
		mCastConsumer = new VideoCastConsumerImpl() {

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                Utils.showToast(CourseDetailActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                Utils.showToast(CourseDetailActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final RouteInfo info) {
                /*if (!CastPreference.isFtuShown(CourseDetailActivity.this)) {
                    CastPreference.setFtuShown(CourseDetailActivity.this);

                    Log.d(TAG, "Route is visible: " + info);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                Log.d(TAG, "Cast Icon is visible: " + info.getName());
                                showFtu();
                            }
                        }
                    }, 1000); 
                } */
            }
        };
        
		// Chromecast Session Recovery
		boolean showDialog = false;
		int timeoutInSeconds = 10;
		mCastManager.reconnectSessionIfPossible(this, showDialog,
				timeoutInSeconds);
	}
	
	@Override
	public void onResume() {
		mCastManager = CastApplication.getCastManager(this);
		if (null != mCastManager) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }
        super.onResume();
	}
	
	@Override
	public void onPause() {
		mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(session.getLogoutReceiver());
        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
        }
        super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		optionsMenu = menu;
		getMenuInflater().inflate(R.menu.options, menu);
		refreshButton = menu.findItem(R.id.action_refresh);
		if(LOADING) {
			enableRefreshButton(false);
		} else {
			enableRefreshButton(true);
		}
		// Add cast menu option to Action bar
		mCastManager.addMediaRouterButton(menu,
				R.id.media_route_menu_item);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					CourseListActivity.class));
			return true;
    	case R.id.action_refresh:
    		setRefreshActionButtonState(true);
    		LectureListFragment.expListView.setVisibility(View.GONE);
    		LectureListFragment.mEmptyView.setVisibility(View.GONE);
    		findViewById(R.id.loading).setVisibility(View.VISIBLE);
    		mCourse.lectures.clear();
    		LectureDAO.getLectures(mCourse.id, LectureListFragment.mLecturesUrl, CourseDetailActivity.this);
	        return true;
	    case R.id.action_logout:
	    	session.logoutUser(false, null);
	    	return true;
        case R.id.action_rate:
        	Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.rate_link)));
        	startActivity(rateIntent);
           return true;
        case R.id.action_donate:
        	Intent donateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.paypal_donation_link)));
        	startActivity(donateIntent);
           return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Sets action bar refresh button state
	 * either inactive or loading
	 * 
	 * @param refreshing true of loading
	 */
	public static void setRefreshActionButtonState(final boolean refreshing) {
	    if (optionsMenu != null) {
	        final MenuItem refreshItem = optionsMenu
	            .findItem(R.id.action_refresh);
	        if (refreshItem != null) {
	            if (refreshing) {
	                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
	            } else {
	                refreshItem.setActionView(null);
	            }
	        }
	    }
	}
	
	/**
	 * Toggles refresh button enable mode
	 * 
	 * @param enable true if refresh button should be enabled
	 */
	public static void enableRefreshButton(boolean enable) {
	    refreshButton.setEnabled(enable);
	}
}
