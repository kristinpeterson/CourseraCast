/**
 * 
 */
package me.kristinpeterson.courseracast.app.models.lectures;


/**
 * Lecture objects are individual lectures associated with
 * a Coursera course
 * 
 * @author kristinpeterson
 */
public class Lecture {

	/**
	 * The section number in which the lecture resides (zero indexed)
	 */
	public int section;
	/**
	 * The title of the lecture
	 */
	public String title;
	/**
	 * The URL to the lecture video, as a string
	 */
	public String url;
	/**
	 * True if video has already been viewed
	 */
	public boolean isViewed;
	
	/**
	 * Overloaded constructor for Lecture object.
	 * @param section the section number in which the lecture resides (zero indexed)
	 * @param title the title of the lecture
	 * @param url the URL to the lecture video, as a string
	 * @param isViewed true if video has been viewed
	 */
	public Lecture(int section, String title, String url, boolean isViewed) {
		this.section = section;
		this.title = title;
		this.url = url;
		this.isViewed = isViewed;
	}
	
	/**
	 * Returns the Lecture object's title as a string
	 * @return title the lecture object's title as a string
	 */
	@Override
	public String toString() {
		return title;
	}
}
