package me.kristinpeterson.courseracast.app.models.courses;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.fragments.CurrentCoursesFragment;
import me.kristinpeterson.courseracast.app.fragments.PastCoursesFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.view.View;

/**
 * @author kristinpeterson
 *
 */
public class CompileCoursesTask extends AsyncTask<JSONArray, Void, Boolean> {
	
	/* 
	 * Runs compileCourses() using the given JSON Array of 
	 * Coursera course data
	 * 
	 * @param params JSON Array of Coursera course data
	 * @throws JSONException
	 * 
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected Boolean doInBackground(JSONArray... params) {
		try {
			compileCourses(params[0]);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/* 
	 * After completion of background task (compileCourses()) :
	 * - If CourseDAO.COURSES list is not empty, breaks courses into
	 * two lists CurrentCoursesFragment.CURRENT_COURSES/PastCoursesFragment.PAST_COURSES
	 * then initiates async DownloadCourseImagesTask()
	 * - Else, removes loading view and shows empty list view for 
	 * Past & Current course lists
	 * 
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		if(!CourseDAO.COURSES.isEmpty()) {
    		for(Course course : CourseDAO.COURSES) {
            	if(course.isCurrent) {
            		CurrentCoursesFragment.CURRENT_COURSES.add(course);
            	} else {
            		PastCoursesFragment.PAST_COURSES.add(course);
            	}
    		}
        	new DownloadCourseImagesTask().execute();
		} else {
			CurrentCoursesFragment.mActivity.findViewById(R.id.loading).setVisibility(View.GONE);
    		CurrentCoursesFragment.lv.setEmptyView(CurrentCoursesFragment.mEmptyView);
    		PastCoursesFragment.mActivity.findViewById(R.id.loading).setVisibility(View.GONE);
    		PastCoursesFragment.lv.setEmptyView(PastCoursesFragment.mEmptyView);
		}
		super.onPostExecute(result);
	}

	/*
	 * Parse the given JSON Array of Coursera data and assigns
	 * course data to CourseDAO.COURSES ArrayList of Course objects
	 * 
	 * @param jsonResponse the JSON Array response comprised of Coursera course data
	 */
	private static void compileCourses(JSONArray jsonResponse) throws JSONException {
		List<Course> courseList = new ArrayList<Course>();
		int  courseId = 0;
		for(int i=0; i < jsonResponse.length(); i++) {
			JSONObject course = jsonResponse.getJSONObject(i);
			String smallIcon = course.getString("small_icon");
			String largeIcon = course.getString("large_icon");
			String name = course.getString("name");
			String shortDescription = course.getString("short_description");
			
			// Get data from nested "courses" JSONArray
			JSONObject courseData = course.getJSONArray("courses").getJSONObject(0);
			String homeLink = courseData.getString("home_link");
			String sDurationWeeks = courseData.getString("duration_string");
			int dd = courseData.getInt("start_day");
		    int mm = courseData.getInt("start_month");  // Gregorian Calendar months indexed 0-11
		    int yy = courseData.getInt("start_year");
			
		    Calendar currentDate = new GregorianCalendar();
	    	currentDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH) + 1);
		    
		    Calendar courseStartDate = new GregorianCalendar(yy, mm, dd);
		    int iDurationWeeks = Integer.parseInt(sDurationWeeks.substring(0, sDurationWeeks.indexOf(" ")));
		    Calendar courseEndDate = (GregorianCalendar) courseStartDate.clone();
		    courseEndDate.add(Calendar.WEEK_OF_MONTH, iDurationWeeks);
		    
		    // If course is current, add course with isCurrent set to true
		    // else isCurrent set to false
		    Course aCourse;
		    if(currentDate.compareTo(courseStartDate) >= 0 && currentDate.compareTo(courseEndDate) <= 0) {
		    	aCourse = new Course(Integer.toString(courseId), name, smallIcon, largeIcon, shortDescription, 
	    				homeLink, true);
				courseList.add(aCourse);
				courseId++;
			} else if(currentDate.compareTo(courseEndDate) >= 0) {
				aCourse = new Course(Integer.toString(courseId), name, smallIcon, largeIcon, shortDescription, 
	    				homeLink, false);
				courseList.add(aCourse);
				courseId++;
			}
		}
		CourseDAO.COURSES.addAll(courseList);
	}
}
