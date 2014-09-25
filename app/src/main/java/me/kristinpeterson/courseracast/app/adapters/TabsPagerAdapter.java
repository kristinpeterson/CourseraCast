package me.kristinpeterson.courseracast.app.adapters;

import me.kristinpeterson.courseracast.app.fragments.CurrentCoursesFragment;
import me.kristinpeterson.courseracast.app.fragments.PastCoursesFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * TabsPagerAdapter provides views to tab fragments
 * @author kristinpeterson
 *
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {
	 
    /**
     * TabsPagerAdapter constructor
     * 
     * @param fm the fragment manager for the activity
     */
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // Top Rated fragment activity
            return new CurrentCoursesFragment();
        case 1:
            // Games fragment activity
            return new PastCoursesFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }
 
}
