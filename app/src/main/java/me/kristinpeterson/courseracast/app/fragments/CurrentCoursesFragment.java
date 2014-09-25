package me.kristinpeterson.courseracast.app.fragments;

import java.util.ArrayList;
import java.util.List;

import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.activities.CourseDetailActivity;
import me.kristinpeterson.courseracast.app.adapters.CourseListAdapter;
import me.kristinpeterson.courseracast.app.models.courses.Course;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author kristinpeterson
 *
 */
public class CurrentCoursesFragment extends Fragment {
	
	/**
	 * The adapter for the course list view
	 */
	public static CourseListAdapter mAdapter;
	/**
	 * The course list view
	 */
	public static ListView lv;
	/**
	 * List of past courses
	 */
	public static final List<Course> CURRENT_COURSES = new ArrayList<Course>();
	/**
	 * The activity calling this fragment
	 */
	public static Activity mActivity;
	/**
	 * Empty Text View
	 */
	public static TextView mEmptyView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_current_course_list, container, false);
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mActivity = getActivity();
		
		mEmptyView = (TextView) mActivity.findViewById(R.id.empty_current_course_list);
		mEmptyView.setText(R.string.empty_current_course_list);
		
		lv = (ListView) getView().findViewById(R.id.current_course_list);

		// Create a progress bar to display while the list loads
        lv.setEmptyView(getActivity().findViewById(R.id.loading));

		mAdapter = new CourseListAdapter(getActivity(), CURRENT_COURSES);

		lv.setAdapter(mAdapter);

		lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				String courseId = CURRENT_COURSES.get(position).id;
				String lecturesUrl = CURRENT_COURSES.get(position).homeLink.concat("lecture");
				Intent detailIntent = new Intent(getActivity(), CourseDetailActivity.class);
				detailIntent.putExtra(CourseDetailActivity.ARG_COURSE_ID, courseId);
				detailIntent.putExtra(LectureListFragment.ARG_LECTURES_URL, lecturesUrl);
				startActivity(detailIntent);
			}
		});
	}

}
