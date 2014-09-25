/**
 * 
 */
package me.kristinpeterson.courseracast.app.models.courses;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.activities.CourseListActivity;
import me.kristinpeterson.courseracast.app.activities.LoginActivity;
import me.kristinpeterson.courseracast.app.fragments.CurrentCoursesFragment;
import me.kristinpeterson.courseracast.app.fragments.PastCoursesFragment;
import me.kristinpeterson.courseracast.app.utils.SessionManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;

/**
 * Asynchronously downloads each course image and saves Bitmap to Course object,
 * when finished updates Course List Adapter
 * 
 * @author kristinpeterson
 *
 */
public class DownloadCourseImagesTask extends AsyncTask<Void, Void, Void> {
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		for(int i = 0; i < CourseDAO.COURSES.size(); i++) {
			CourseDAO.COURSES.get(i).largeIconBitmap = 
					downloadImage(CourseDAO.COURSES.get(i).largeIcon);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Void result) {
		if(CurrentCoursesFragment.CURRENT_COURSES.isEmpty()) {
			CurrentCoursesFragment.mActivity.findViewById(R.id.loading).setVisibility(View.GONE);
			CurrentCoursesFragment.lv.setEmptyView(CurrentCoursesFragment.mEmptyView);
		} else {
			CurrentCoursesFragment.mAdapter.notifyDataSetChanged();
			CurrentCoursesFragment.lv.setVisibility(View.VISIBLE);
		}
		if(PastCoursesFragment.PAST_COURSES.isEmpty()) {
			PastCoursesFragment.mActivity.findViewById(R.id.loading).setVisibility(View.GONE);
			PastCoursesFragment.lv.setEmptyView(PastCoursesFragment.mEmptyView);
		} else {
			PastCoursesFragment.mAdapter.notifyDataSetChanged();
			PastCoursesFragment.lv.setVisibility(View.VISIBLE);
		}
		CourseListActivity.LOADING = false;
		CourseListActivity.enableRefreshButton(true);
		CourseListActivity.setRefreshActionButtonState(false);
		super.onPostExecute(result);
	}
	  
	private Bitmap downloadImage(String url) {
		Bitmap bmp =null;
		try{
			URL ulrn = new URL(url);
			HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
			InputStream is = con.getInputStream();
			bmp = BitmapFactory.decodeStream(is);
			if (null != bmp)
				return bmp;
		}catch(Exception e){
			forceLogout(LoginActivity.ARG_NETWORK_ERROR);
		}
			return bmp;
	}
	
	/*
	 *	Forces logout 
	 */
	private static void forceLogout(String reason) {
    	SessionManager session = new SessionManager(CourseListActivity.mContext);
		session.logoutUser(true, reason);
	}
}
