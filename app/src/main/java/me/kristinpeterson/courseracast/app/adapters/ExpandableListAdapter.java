/**
 * 
 */
package me.kristinpeterson.courseracast.app.adapters;

import java.util.HashMap;
import java.util.List;

import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.models.lectures.Lecture;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author kristinpeterson
 *
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
	 	
		private Context _context;
	    private List<String> _listDataHeader;
	    private HashMap<String, List<Lecture>> _listDataChild;
	 
	    /**
	     * Overloaded constructor for Lectures ExpandableListAdapter
	     * 
	     * @param context the context where the list is being populated
	     * @param listDataHeader the list item headers
	     * @param listChildData the child items for each header
	     */
	    public ExpandableListAdapter(Context context, List<String> listDataHeader,
	            HashMap<String, List<Lecture>> listChildData) {
	        this._context = context;
	        this._listDataHeader = listDataHeader;
	        this._listDataChild = listChildData;
	    }
	 
	    @Override
	    public Object getChild(int groupPosition, int childPosititon) {
	        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
	                .get(childPosititon);
	    }
	 
	    @Override
	    public long getChildId(int groupPosition, int childPosition) {
	        return childPosition;
	    }
	 
	    @Override
	    public View getChildView(int groupPosition, final int childPosition,
	            boolean isLastChild, View convertView, ViewGroup parent) {
	 
	        final Lecture childObject = (Lecture) getChild(groupPosition, childPosition);
	        final String childText = childObject.title;
	 
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) this._context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.lecture_list_item, null);
	        }
	 
	        TextView txtListChild = (TextView) convertView
	                .findViewById(R.id.lblListItem);
	        
	        ImageView listChildIcon = (ImageView) convertView
	        		.findViewById(R.id.listItemCheck);
	        
	        if(childObject.isViewed) {
	        	listChildIcon.setImageResource(R.drawable.ic_green_checkmark);
	        } else {
	        	listChildIcon.setImageResource(android.R.color.transparent);
	        }
		    
	        txtListChild.setText(childText);
	        
	        return convertView;
	    }
	 
	    @Override
	    public int getChildrenCount(int groupPosition) {
	    	return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
	    }
	 
	    @Override
	    public Object getGroup(int groupPosition) {
	        return this._listDataHeader.get(groupPosition);
	    }
	 
	    @Override
	    public int getGroupCount() {
	        return this._listDataHeader.size();
	    }
	 
	    @Override
	    public long getGroupId(int groupPosition) {
	        return groupPosition;
	    }
	 
	    @Override
	    public View getGroupView(int groupPosition, boolean isExpanded,
	            View convertView, ViewGroup parent) {
	        String headerTitle = (String) getGroup(groupPosition);
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) this._context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.lecture_list_group, null);
	        }
	 
	        TextView lblListHeader = (TextView) convertView
	                .findViewById(R.id.lblListHeader);
	        lblListHeader.setTypeface(null, Typeface.BOLD);
	        lblListHeader.setText(headerTitle);
	 
	        return convertView;
	    }
	 
	    @Override
	    public boolean hasStableIds() {
	        return false;
	    }
	 
	    @Override
	    public boolean isChildSelectable(int groupPosition, int childPosition) {
	        return true;
	    }
	    
	    /**
	     * Updates the ExpandableList data
	     * 
	     * @param listDataHeader the section headings
	     * @param listChildData a map of each sections child data by section
	     */
	    public void updateLecturesList(List<String> listDataHeader,
	            HashMap<String, List<Lecture>> listChildData) {
	        this._listDataHeader.clear();
	        this._listDataHeader.addAll(listDataHeader);
	        this._listDataChild.clear();
	        this._listDataChild.putAll(listChildData);
	        this.notifyDataSetChanged();
	    }
	}
