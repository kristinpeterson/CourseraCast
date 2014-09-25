package me.kristinpeterson.courseracast.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import me.kristinpeterson.courseracast.app.CastApplication;
import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.adapters.TabsPagerAdapter;
import me.kristinpeterson.courseracast.app.fragments.CurrentCoursesFragment;
import me.kristinpeterson.courseracast.app.fragments.PastCoursesFragment;
import me.kristinpeterson.courseracast.app.models.courses.CourseDAO;
import me.kristinpeterson.courseracast.app.utils.SessionManager;
import me.kristinpeterson.courseracast.app.utils.Utils;

/**
 * An activity representing a list of Courses. The activity
 * presents a list of items, which when touched, lead to a
 * {@link CourseDetailActivity} representing course details.
 */
public class CourseListActivity extends ActionBarActivity implements
	ActionBar.TabListener {
	
	private SessionManager session;
	private VideoCastManager mCastManager;
	private MiniController mMini;
	private ViewPager viewPager;
    private TabsPagerAdapter mTabAdapter;
    private ActionBar actionBar;
    private String[] tabs = { "Current", "Past" };
    private static Menu optionsMenu;
    private static MenuItem refreshButton;
    
    /**
     * Indicates whether course list is loading
     */
    public static boolean LOADING;
    
	/**
	 * The Activity context
	 */
	public static CourseListActivity mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Utils.CONTEXT = getApplicationContext();
		setContentView(R.layout.activity_course_list);
		
		mContext = CourseListActivity.this;

		// Checking if user is logged in, redirecting to LoginActivity if not
        session = new SessionManager(Utils.CONTEXT);
		session.setLogoutReceiver(CourseListActivity.this);
        session.checkLogin();
        
		if(CourseDAO.COURSES.isEmpty()) {
			LOADING = true;
			CourseDAO.loadCourseData(Utils.CONTEXT);
		}

		// Initializing the Chromecast VideoCastManager for this Activity
		mCastManager = CastApplication.getCastManager(this);

		// Adding Chromecast mini controller
		mMini = (MiniController) findViewById(R.id.miniController1);
		mCastManager.addMiniController(mMini);

		// Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
        mTabAdapter = new TabsPagerAdapter(getSupportFragmentManager());
 
        viewPager.setAdapter(mTabAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
 
        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }
        
        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
         
            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }
         
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
         
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
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
	    	case R.id.action_refresh:
	    		setRefreshActionButtonState(true);
	    		// hide list view
	    		CurrentCoursesFragment.lv.setVisibility(View.GONE);
	    		PastCoursesFragment.lv.setVisibility(View.GONE);
	    		// hide empty list text view
	    		CurrentCoursesFragment.mEmptyView.setVisibility(View.GONE);
	    		PastCoursesFragment.mEmptyView.setVisibility(View.GONE);
	    		// set empty view to loading
	    		findViewById(R.id.loading).setVisibility(View.VISIBLE);
	    		// clear course list objects
	    		CourseDAO.COURSES.clear();
	    		CurrentCoursesFragment.CURRENT_COURSES.clear();
	    		PastCoursesFragment.PAST_COURSES.clear();
	    		// load course data
	    		CourseDAO.loadCourseData(Utils.CONTEXT);
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
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	     if (keyCode == KeyEvent.KEYCODE_BACK) {
		     //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
	    	 moveTaskToBack(true);
		     return true;
	     }
	     return super.onKeyDown(keyCode, event);    
	}
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
		super.onBackPressed();
	}

	/* (non-Javadoc)
	 * @see android.support.v7.app.ActionBar.TabListener#onTabReselected(android.support.v7.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.support.v7.app.ActionBar.TabListener#onTabSelected(android.support.v7.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
        // show respective fragment view
        viewPager.setCurrentItem(tab.getPosition());
	}

	/* (non-Javadoc)
	 * @see android.support.v7.app.ActionBar.TabListener#onTabUnselected(android.support.v7.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
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
