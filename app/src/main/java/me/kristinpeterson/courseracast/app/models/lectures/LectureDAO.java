/**
 * 
 */
package me.kristinpeterson.courseracast.app.models.lectures;

import android.app.Activity;
import android.view.View;
import com.loopj.android.http.TextHttpResponseHandler;
import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.activities.CourseDetailActivity;
import me.kristinpeterson.courseracast.app.activities.LoginActivity;
import me.kristinpeterson.courseracast.app.fragments.LectureListFragment;
import me.kristinpeterson.courseracast.app.models.courses.CourseDAO;
import me.kristinpeterson.courseracast.app.net.CourseraRestClient;
import me.kristinpeterson.courseracast.app.utils.SessionManager;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author kristinpeterson
 *
 */
public class LectureDAO {
	
	private static TextHttpResponseHandler textResponseHandler = null;
	
	/**
	 * Gets lecture data from a given Coursera course and assembles
	 * data into Lecture objects which are stored in the courses lectures
	 * list.
	 * 
	 * @param courseId the id of the course in which lectures are being loaded
	 * @param lectureURL the URL for a given Course's lectures
	 * @param context the activity context
	 */
	public static void getLectures(final String courseId, String lectureURL, final Activity context) {
		// Create GET response handler
		textResponseHandler = new TextHttpResponseHandler() {
		     @Override
		     public void onStart() {
		         // Initiated the request
		     }

		     @Override
		     public void onSuccess(int i, Header[] header, String responseBody) {
		        // Successfully got a response
			    parseLectures(Integer.parseInt(courseId), responseBody);
			    if(LectureListFragment.mCourse.lectures.isEmpty()) {
			    	context.findViewById(R.id.loading).setVisibility(View.GONE);
			    	LectureListFragment.expListView.setEmptyView(context.findViewById(R.id.empty_lecture_list));
			    } else {
			    	context.findViewById(R.id.loading).setVisibility(View.GONE);
			    	LectureListFragment.expListView.setVisibility(View.VISIBLE);
			    }
			    CourseDetailActivity.LOADING = false;
			    CourseDetailActivity.enableRefreshButton(true);
			    CourseDetailActivity.setRefreshActionButtonState(false);
		     }

			@Override
		     public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error)
		     {
		        // Response failed :(
				if(statusCode == 0) {
					context.findViewById(R.id.loading).setVisibility(View.GONE);
			    	LectureListFragment.expListView.setEmptyView(context.findViewById(R.id.empty_lecture_list));
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
		CourseraRestClient.get(lectureURL, textResponseHandler);
	}
	
	/*
	 * - Parses lecture data from given string response
	 * - Adds lectures & section titles to given course 
	 * - Calls prepareListData to add lecture data to listDataHeader 
	 * 		& listDataChild (used by lecture list's ExpandableListAdapter)
	 * - Updates the LectureListFragment.ExpandableListAdapter
	 */
	private static void parseLectures(int courseId, String responseBody) {
		ArrayList<Lecture> lectures = new ArrayList<Lecture>();
		ArrayList<String> sectionTitles = new ArrayList<String>();
		
		// variables used for parsing Coursera /lectures DOM
    	int anchorIndex = 0;
    	Document doc = Jsoup.parse(responseBody);
	    Elements sectionTitleElements = doc.getElementsByTag("h3");
	    
	    /* 
	     * lecture-link anchors hold the url to a frame that contains the 
	     * actual lecture video url, which can be derived with parseLectureUrlResponse()
	     */
	    Elements anchors = doc.getElementsByClass("lecture-link");
	    Elements sections = doc.getElementsByClass("course-item-list-section-list");
	    
	    // Create ArrayList of lecture section titles
	    for(Element sectionTitle : sectionTitleElements) {
	    	sectionTitles.add(sectionTitle.text());
	    }
	    
	    // Add Lectures to lectures ArrayList
	    for(int i = 0; i < sections.size(); i++) {
		    int lecturesInSection = sections.get(i).getElementsByClass("lecture-link").size();
	    	for(int j = 0; j < lecturesInSection; j++) {
	    		Element anchor = anchors.get(anchorIndex);
	    		String lectureTitle = anchor.ownText();
	    		String lectureUrl = anchor.attr("data-modal-iframe");
	    		boolean isViewed = false;
	    		if(anchor.parent().className().equals("viewed")) {
	    			isViewed = true;
	    		}
		    	Lecture lecture = new Lecture(i, lectureTitle, lectureUrl, isViewed);	
		    	lectures.add(lecture);
		    	anchorIndex++;
	    	}
	    }
	    CourseDAO.COURSES.get(courseId).sectionTitles = sectionTitles;
	    CourseDAO.COURSES.get(courseId).lectures = lectures;
	    prepareListData(courseId);
	}
    
	/*
     * Prepares the lecture list data for ExpandableListAdapter
     */
    private static void prepareListData(int courseId) {
        List<String> listDataHeader = new ArrayList<String>();
        HashMap<String, List<Lecture>> listDataChild = new HashMap<String, List<Lecture>>();
        
        /* 
         * instantiate lectures outside of loop
         * as the lectures list will be reduced within the loop
         * to enhance performance (ie prevent unnecessary trips through loop)
         */
        List<Lecture> lectures = CourseDAO.COURSES.get(courseId).lectures;
        List<String> sectionTitles = CourseDAO.COURSES.get(courseId).sectionTitles;
        for(int i = 0; i < sectionTitles.size(); i++) {
        	String sectionTitle = sectionTitles.get(i);
        	listDataHeader.add(sectionTitle);
        	List<Lecture> sectionList = new ArrayList<Lecture>();
        	
        	for(int j = 0; j < lectures.size(); j++) {
        		if(lectures.get(j).section == i) {
        			sectionList.add(lectures.get(j));
        			// put final section list
            		if(lectures.size() == sectionList.size())
            			listDataChild.put(sectionTitle, sectionList);
        		}
        		else {
        			listDataChild.put(sectionTitle, sectionList);
        			lectures = lectures.subList(j, lectures.size());
        			break;
        		}
        	}
        }
        // Add lecture list headers & children to Course object
        LectureListFragment.mCourse.listDataHeader = listDataHeader;
        LectureListFragment.mCourse.listDataChild = listDataChild;
        // Update list adapter
        LectureListFragment.mAdapter.updateLecturesList(listDataHeader, listDataChild);
    }
    
	/*
	 *	Forces user logout with the given reason
	 *	
	 *	Acceptable reasons:
	 * 	- LoginActivity.ARG_RE_LOGIN: Asks user to re-login
	 * 	- LoginActivity.ARG_NETWORK_ERROR: informs user of network error
	 */
	private static void forceLogout(String reason) {
    	SessionManager session = new SessionManager(CourseDetailActivity.mContext);
		session.logoutUser(true, reason);
	}
}
