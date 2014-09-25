/**
 * 
 */
package me.kristinpeterson.courseracast.app.fragments;

import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.activities.CourseDetailActivity;
import me.kristinpeterson.courseracast.app.activities.LectureVideoActivity;
import me.kristinpeterson.courseracast.app.activities.LoginActivity;
import me.kristinpeterson.courseracast.app.adapters.ExpandableListAdapter;
import me.kristinpeterson.courseracast.app.models.courses.Course;
import me.kristinpeterson.courseracast.app.models.courses.CourseDAO;
import me.kristinpeterson.courseracast.app.models.lectures.LectureDAO;
import me.kristinpeterson.courseracast.app.net.CourseraRestClient;
import me.kristinpeterson.courseracast.app.utils.CastUtils;
import me.kristinpeterson.courseracast.app.utils.SessionManager;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.loopj.android.http.TextHttpResponseHandler;


/**
 * @author kristinpeterson
 *
 */
public class LectureListFragment extends ListFragment {

	private static TextHttpResponseHandler textResponseHandler;
    
	/**
	 * The lecture list adapter for ExpandableListView
	 */
	public static ExpandableListAdapter mAdapter;
	/**
	 * The ExpandableListView
	 */
	public static ExpandableListView expListView;
	/**
	 * List empty text view
	 */
	public static TextView mEmptyView;
	/**
	 * The currently selected Course
	 */
	public static Course mCourse = null;
	
	/**
	 * The url for course lectures
	 */
	public static String mLecturesUrl;
	
	/**
	 * The bundle argument representing the url for the lectures that this fragment represents
	 */
    public static final String ARG_LECTURES_URL = "lectures_url";
    
    /**
     * The bundle argument representing the lecture image as a bitmap
     */
    public static final String ARG_IMG_BITMAP = "img_bitmap";
    
    /**
     * The bundle argument representing the url of the lecture video being selected
     */
    public static final String ARG_VIDEO_URL = "video_url";
    
    /**
     * Indicates whether list should be updated
     */
    public static boolean UPDATE_LIST;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }
 
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_lecture_list, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	expListView = (ExpandableListView) getView().findViewById(android.R.id.list);
    	mEmptyView = (TextView) getActivity().findViewById(R.id.empty_lecture_list);
    	
    	Bundle b = getActivity().getIntent().getExtras();
    	
    	if(savedInstanceState == null) {
			if (b.containsKey(CourseDetailActivity.ARG_COURSE_ID) && b.containsKey(ARG_LECTURES_URL)) {
				int courseId = Integer.parseInt(getActivity().getIntent().getStringExtra(
						CourseDetailActivity.ARG_COURSE_ID));
				mCourse = CourseDAO.COURSES.get(courseId);
				mLecturesUrl = b.getString(ARG_LECTURES_URL);
				// Display progress bar while the list loads
			    expListView.setEmptyView(getActivity().findViewById(R.id.loading));
				LectureDAO.getLectures(mCourse.id, mLecturesUrl, getActivity());
				mAdapter = new ExpandableListAdapter(getActivity(), mCourse.listDataHeader, mCourse.listDataChild);
		    	expListView.setAdapter(mAdapter);
				expListView.setFooterDividersEnabled(true);
				expListView.addFooterView(new View(expListView.getContext()));
			}
    	} else {
    		if(mCourse.lectures.isEmpty()) {
    			getActivity().findViewById(R.id.loading).setVisibility(View.GONE);
		    	expListView.setEmptyView(mEmptyView);
    		}
    	}
		
		// Listview Group click listener
	        expListView.setOnGroupClickListener(new OnGroupClickListener() {
	 
	            @Override
	            public boolean onGroupClick(ExpandableListView parent, View v,
	                    int groupPosition, long id) {
	                // Toast.makeText(getApplicationContext(),
	            	// "Group Clicked " + listDataHeader.get(groupPosition),
	            	// Toast.LENGTH_SHORT).show();
	                return false;
	            }
	        });
	 
	        // Listview Group expanded listener
	        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {
	 
	            @Override
	            public void onGroupExpand(int groupPosition) {
	                //Toast.makeText(getActivity(),
	                //        listDataHeader.get(groupPosition) + " Expanded",
	                //        Toast.LENGTH_SHORT).show();
	            }
	        });
	 
	        // Listview Group collasped listener
	        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
	 
	            @Override
	            public void onGroupCollapse(int groupPosition) {
	                //Toast.makeText(getActivity(),
	                //        listDataHeader.get(groupPosition) + " Collapsed",
	                //        Toast.LENGTH_SHORT).show();
	 
	            }
	        });
	 
	        // Listview on child click listener
	        expListView.setOnChildClickListener(new OnChildClickListener() {
	 
	            @Override
	            public boolean onChildClick(ExpandableListView parent, View v,
	                    int groupPosition, int childPosition, long id) {
	            //Toast.makeText(
	            //        getActivity(),
	            //        mCourse.listDataHeader.get(groupPosition)
	            //                + " : "
	            //                + mCourse.listDataChild.get(
	            //                        mCourse.listDataHeader.get(groupPosition)).get(
	            //                        childPosition).url, Toast.LENGTH_SHORT)
	            //        .show();
	            
	            String lectureUrl = mCourse.listDataChild.get(mCourse.listDataHeader.get(groupPosition))
	            		.get(childPosition).url;
	            String lectureTitle = mCourse.listDataChild.get(mCourse.listDataHeader.get(groupPosition))
	            		.get(childPosition).title;
	            Bitmap imgBitmap = mCourse.largeIconBitmap;
	            String smallImgUrl = mCourse.smallIcon;
	            String bigImgUrl = mCourse.largeIcon;
	            
	            startLectureVideo(lectureTitle, lectureUrl, imgBitmap, smallImgUrl, bigImgUrl, groupPosition, childPosition);
	            
	            return false;
	        }
	    });
    }
    
    public void onStart() {
    	super.onStart();
    }
    
    public void onPause() {
    	super.onPause();
    }
    
    public void onResume() {
    	if(UPDATE_LIST) {
    		mAdapter.updateLecturesList(mCourse.listDataHeader, mCourse.listDataChild);
    		UPDATE_LIST = false;
    	}
    	super.onResume();
    }
	
    /*
	 * Starts the given lecture video
	 */
	private void startLectureVideo(final String lectureTitle, String lectureUrl, 
			final Bitmap imgBitmap, final String smallImgUrl, final String bigImgUrl,
			final int groupPosition, final int childPosition) {
		
		// Create GET response handler
		textResponseHandler = new TextHttpResponseHandler() {
		     @Override
		     public void onStart() {
		         // Initiated the request
		     }
	
		     @Override
		     public void onSuccess(int i, Header[] header, String responseBody) {
		        // Successfully got a response
			    String videoUrl = parseLectureUrlResponse(responseBody);
			    if(null != videoUrl) {
				    Intent videoIntent = new Intent(getActivity(), LectureVideoActivity.class);
		            videoIntent.putExtra(ARG_IMG_BITMAP, imgBitmap);
		            MediaInfo mSelectedMedia = CastUtils.buildMediaInfo(lectureTitle, "", "", videoUrl, smallImgUrl, bigImgUrl);
		            videoIntent.putExtra("media", com.google.sample.castcompanionlibrary.utils.Utils
		                    .fromMediaInfo(mSelectedMedia));
		            videoIntent.putExtra("shouldStart", false);
		            videoIntent.putExtra("groupPosition", groupPosition);
		            videoIntent.putExtra("childPosition", childPosition);
		            startActivity(videoIntent);
			    } else {
			    	new AlertDialog.Builder(CourseDetailActivity.mContext)
				    .setTitle(getString(R.string.error_loading_video))
				    .setIcon(R.drawable.ic_dialog_alert)
				    .show();
			    }
		     }
	
			@Override
		     public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error)
		     {
		        // Response failed :(
				if(statusCode == 401) {
					forceLogout(null);
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
		CourseraRestClient.get(lectureUrl, textResponseHandler);
	}
	
	/*
	 * Parse the lecture video base url to get the video source url
	 * in the first available format (webm or mp4)
	 * If no video url is found, returns null.
	 */
	private static String parseLectureUrlResponse(String responseBody) {
		
		Document doc = Jsoup.parse(responseBody);
		
	    Elements webmVideoElements = doc.getElementsByAttributeValue("type", "video/webm");
	    String webmUrl = webmVideoElements.first().attr("src");
	    		
	    Elements mp4VideoElements = doc.getElementsByAttributeValue("type", "video/mp4");
	    String mp4Url = mp4VideoElements.first().attr("src");
	    
	    if(null != mp4Url) {
	    	return mp4Url;
	    } else if(null != webmUrl) {
	    	return webmUrl;
	    } else {
	    	return null;
	    }
	}
	
	/*
	 *	Forces logout 
	 */
	private void forceLogout(String reason) {
		SessionManager session = new SessionManager(getActivity());
    	session.logoutUser(true, reason);
	}
}
