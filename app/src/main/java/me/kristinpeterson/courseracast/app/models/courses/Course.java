package me.kristinpeterson.courseracast.app.models.courses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.kristinpeterson.courseracast.app.models.lectures.Lecture;
import android.graphics.Bitmap;

/**
 * Course object represents a single Coursera course
 * 
 * @author kristinpeterson
 */
public class Course {
	
	/**
	 * The Course position id
	 */
	public String id;
	/**
	 * The Course name
	 */		
	public String name;
	/**
	 * The course current state (true if current)
	 */
	public Boolean isCurrent;
	/**
	 * The Course smallIcon url
	 */
	public String smallIcon;
	/**
	 * The Course largeIcon url
	 */
	public String largeIcon;
	/**
	 * The Course course short description
	 */
	public String shortDescription;
	/**
	 * The Course root url
	 */
	public String homeLink;
	/**
	 * The Lectures associated with the course
	 */
	public ArrayList<Lecture> lectures;
	/**
	 * The section titles of the lectures
	 */
	public ArrayList<String> sectionTitles;
	/**
	 * A list of to hold lecture section headers, for the Expandable ListView
	 */
    public List<String> listDataHeader;
    
	/**
	 * A map that holds individual lectures separated by section, for the ExpandableListView
	 */
    public HashMap<String, List<Lecture>> listDataChild;
    
	/**
	 * The course large icon as Bitmap
	 */
	public Bitmap largeIconBitmap;
	
	/**
	 * Overloaded Constructor for the Course object
	 * 
	 * @param id the position id of the Course
	 * @param name the Course name
	 * @param smallIcon the Course small icon url
	 * @param largeIcon the Course large icon url 
	 * @param shortDescription the Course short description
	 * @param homeLink the Course root url
	 * @param isCurrent true if this course is current, false if past
	 */
	public Course(String id, String name, String smallIcon, String largeIcon, 
			String shortDescription, String homeLink, Boolean isCurrent) {
		this.id = id;
		this.name = name;
		this.smallIcon = smallIcon;
		this.largeIcon = largeIcon;
		this.shortDescription = shortDescription;
		this.homeLink = homeLink;
		this.isCurrent = isCurrent;
		this.lectures = new ArrayList<Lecture>();
		this.sectionTitles = new ArrayList<String>();
		this.largeIconBitmap = null;
		this.listDataHeader = new ArrayList<String>();
		this.listDataChild = new HashMap<String, List<Lecture>>();
	}

	@Override
	public String toString() {
		return this.name;
	}
}
