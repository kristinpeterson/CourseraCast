/**
 * 
 */
package me.kristinpeterson.courseracast.app.net;

import java.security.KeyStore;

import me.kristinpeterson.courseracast.app.utils.Utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.params.ClientPNames;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.ResponseHandlerInterface;


/**
 * @author kristinpeterson
 *
 */
public class CourseraRestClient {
	  
	private static AsyncHttpClient client;
	/**
	 * Cookiestore
	 */
	public static CookieStore cookieStore;
	
	static {
		client = new AsyncHttpClient();
		client.setMaxRetriesAndTimeout(3, 30000);
		client.setMaxConnections(12);
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			client.setSSLSocketFactory(sf);   
		}
		catch (Exception e) {   
		}
		cookieStore = new PersistentCookieStore(Utils.CONTEXT);
		client.setCookieStore(cookieStore);
		client.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
	}

	/**
	 * Handles an asynchronous post request to the given url
	 * 
	 * @param context the context in which the post request was called
	 * @param url the url for the post request
	 * @param headers the HTTP headers
	 * @param entity the HTTP entity
	 * @param contentType the HTTP content type
	 * @param responseHandler the response handler for the request
	 * 
	 * @see com.loopj.android.http.AsyncHttpResponseHandler
	 */
	public static void post(Context context, String url, 
			  Header[] headers, HttpEntity entity, 
			  String contentType, ResponseHandlerInterface responseHandler) {
		client.post(context, url, headers, entity, contentType, responseHandler);
	  }
	
	/**
	 * Handles an asynchronous get request to the given url
	 * 
	 * @param url the url for the get request
	 * @param responseHandler the asynchronous response handler
	 * 
	 * @see com.loopj.android.http.AsyncHttpResponseHandler
	 */
	public static void get(String url, AsyncHttpResponseHandler responseHandler) {
	    client.get(url, responseHandler);
	  }
	  
	/**
	 * Sets basic authentication for the request. 
	 * 
	 * @param email the login email
	 * @param password the login password
	 * 
	 * @see com.loopj.android.http.AsyncHttpClient
	 */
	public static void setAuth(String email, String password) {
		  client.setBasicAuth(email, password);
	  }
}
