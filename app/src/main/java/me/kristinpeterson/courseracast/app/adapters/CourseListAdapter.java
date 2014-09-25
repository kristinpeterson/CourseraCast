/**
 * 
 */
package me.kristinpeterson.courseracast.app.adapters;

import java.util.List;

import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.models.courses.Course;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author kristinpeterson
 *
 */
public class CourseListAdapter extends BaseAdapter {

	private final Context context;
	private final List<Course> courses;

	/**
	 * A custom adpater for Courses
	 * @param context the context in which the CourseListAdapter is being insantiated
	 * @param courses the courses being added to the adapter
	 */
	public CourseListAdapter(Context context, List<Course> courses) {
		this.context = context;
		this.courses = courses;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.course_list_item, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.courseListItem);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.courseListLogo);
		imageView.setImageBitmap(courses.get(position).largeIconBitmap);
		textView.setText(courses.get(position).name);
		return rowView;
	}
	
		/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return courses.size();
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return courses.get(position);
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	
    /**
     * Updates the CourseListAdapter data
     * 
     * @param courses the section headings
     */
    public void updateCoursesList(List<Course> courses) {
        this.courses.clear();
        this.courses.addAll(courses);
        this.notifyDataSetChanged();
    }
} 