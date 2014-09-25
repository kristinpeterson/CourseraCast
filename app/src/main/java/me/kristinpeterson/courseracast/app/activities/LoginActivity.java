package me.kristinpeterson.courseracast.app.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import me.kristinpeterson.courseracast.app.CastApplication;
import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.models.courses.CourseDAO;
import me.kristinpeterson.courseracast.app.net.CourseraRestClient;
import me.kristinpeterson.courseracast.app.utils.SessionManager;
import me.kristinpeterson.courseracast.app.utils.Utils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends ActionBarActivity {
	
    private static final String LOGIN_URL = "https://accounts.coursera.org/api/v1/login";
    private static String SIGNEDIN_URL = "https://www.coursera.org/account/signedin";
	private VideoCastManager mCastManager;
	private MiniController mMini;
	private static AsyncHttpResponseHandler mResponseHandler = null;
    private static SessionManager session;
 	private String mEmail;
	private String mPassword;
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;

    /** 
     * Indicates whether a background login has been performed
     * Eliminates infinite loop potential of performing background 
     * login on each 401 error rec'd by CourseDAO.loadCourses()
     */
    public static boolean BACKGROUND_LOGIN_PERFORMED;
	/**
	 * Force logout dialog tag - network error
	 */
	public static String ARG_NETWORK_ERROR = "networkError";
	
	/**
	 * Force logout dialog tag - re-login
	 */
	public static String ARG_RE_LOGIN = "reLogin";
	
	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Utils.CONTEXT = getApplicationContext();
		setContentView(R.layout.activity_login);
		
		// Session Manager
        session = new SessionManager(Utils.CONTEXT);

		Bundle b = getIntent().getExtras();
    	if(null != b && b.getBoolean("forced_logout")) {
    		if(b.getString("forced_logout_reason").equals(ARG_RE_LOGIN)) {
	    		new AlertDialog.Builder(this)
			    .setTitle(getString(R.string.error_re_login))
			    .setIcon(R.drawable.ic_dialog_alert)
			    .show();
    		} else {
    			new AlertDialog.Builder(this)
			    .setTitle(getString(R.string.error_network))
			    .setMessage(getString(R.string.error_processing_request))
			    .setIcon(R.drawable.ic_dialog_alert)
			    .show();
    		}
    	}
		
		// Verifies that the correct version of Google Play services (required by Chromecast)
		// is available on the device
		BaseCastManager.checkGooglePlaySevices(this);
			
		// Initializing the Chromecast VideoCastManager for this Activity
		mCastManager = CastApplication.getCastManager(this);
		
		// Adding Chromecast mini controller
		mMini = (MiniController) findViewById(R.id.miniController1);
		mCastManager.addMiniController(mMini);    

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.loading);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}
	

	@Override
	protected void onResume() {
		mCastManager = CastApplication.getCastManager(this);
		if (null != mCastManager) {
            mCastManager.incrementUiCounter();
        }
        super.onResume();
	}

	@Override
	protected void onPause() {
		mCastManager.decrementUiCounter();
        super.onPause();
	}
	
	@Override
	public void onDestroy() {
        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
        }
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		// Add cast menu option to Action bar
		mCastManager.addMediaRouterButton(menu,
						R.id.media_route_menu_item);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_rate:
	        	Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.rate_link)));
	        	startActivity(rateIntent);
	           return true;
	        case R.id.action_donate:
	        	Intent donateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.paypal_donation_link)));
	        	startActivity(donateIntent);
	           return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {  
        
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			
			
			
			// Getting csrf_token using random course url
			CourseraRestClient.get("https://class.coursera.org/ml-2012-002/class/index", 
					new AsyncHttpResponseHandler() {
				 @Override
			     public void onStart() {
			         // Initiated the request
			     }

			     @Override
			     public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					// Prepere Entity with auth credentials
					List<NameValuePair> auth = new ArrayList<NameValuePair>(2);
			        auth.add(new BasicNameValuePair("email", mEmail));
			        auth.add(new BasicNameValuePair("password", mPassword));
			        auth.add(new BasicNameValuePair("webrequest", "true"));
			        HttpEntity entity = null;
					try {
						entity = new UrlEncodedFormEntity(auth, "utf8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Header[] postHeaders = prepareHeaders();
					String contentType = "application/x-www-form-urlencoded";
					
					// Create response handler
					mResponseHandler = new AsyncHttpResponseHandler() {
					     @Override
					     public void onStart() {
					         // Initiated the request
					     }

					     @Override
					     public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                             getUserId();
					     }

					     @Override
					     public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
					     {
					        // Response failed :(
					        showProgress(false);
					        if(statusCode == 401) {
					        	mPasswordView.setError(getString(R.string.error_invalid_login));
						        mPasswordView.requestFocus();
					        } else if (statusCode == 408 || statusCode == 522) {
					        	showAlertDialog(getString(R.string.error_request_timed_out));
					        } else if (statusCode == 0) {
					        	showAlertDialog(getString(R.string.error_no_network));
					        } else {
					        	showAlertDialog(getString(R.string.error_login_attempt_failed));
					        }
					     }

					     public void onRetry() {
					         // Request was retried
					     }

					     @Override
					     public void onProgress(int bytesWritten, int totalSize) {
					         // Progress notification
					     }

					     @Override
					     public void onFinish() {
					         // Completed the request (either success or failure)
					    	mResponseHandler = null;
					     }
					 };
					 
					// Set basic auth for http client
					CourseraRestClient.setAuth(mEmail, mPassword);
			    	// Send POST request
					CourseraRestClient.post(Utils.CONTEXT, LOGIN_URL, postHeaders, entity, contentType, mResponseHandler);
			     }

			     @Override
			     public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
			     {
			        // Response failed :(
			     }

			     public void onRetry() {
			         // Request was retried
			     }

			     @Override
			     public void onProgress(int bytesWritten, int totalSize) {
			         // Progress notification
			     }

			     @Override
			     public void onFinish() {
			         // Completed the request (either success or failure)
			     }
			});
		}
	}
	
	/**
	 * Performs a background login of the user with the session 
	 * username and password
	 */
	public static void performBackgroundLogin() {  
		
		final SessionManager newSession = new SessionManager(Utils.CONTEXT);
		
		// Store values at the time of the login attempt.
		String email = newSession.getUserDetails().get(SessionManager.KEY_EMAIL);
		String password = newSession.getUserDetails().get(SessionManager.KEY_PASSWORD);
			
		// Prepere Entity with auth credentials
		List<NameValuePair> auth = new ArrayList<NameValuePair>(2);
        auth.add(new BasicNameValuePair("email", email));
        auth.add(new BasicNameValuePair("password", password));
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(auth, "utf8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Header[] headers = prepareHeaders();
		String contentType = "application/x-www-form-urlencoded";
		
		// Create response handler
		mResponseHandler = new AsyncHttpResponseHandler() {
		     @Override
		     public void onStart() {
		         // Initiated the request
		     }

		     @Override
		     public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		    	 BACKGROUND_LOGIN_PERFORMED = true;
		    	 CourseDAO.loadCourseData(Utils.CONTEXT);
		     }

		     @Override
		     public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
		     {
		        // Response failed :(
		        newSession.logoutUser(true, ARG_RE_LOGIN);
		     }

		     public void onRetry() {
		         // Request was retried
		     }

		     @Override
		     public void onProgress(int bytesWritten, int totalSize) {
		         // Progress notification
		     }

		     @Override
		     public void onFinish() {
		         // Completed the request (either success or failure)
		    	mResponseHandler = null;
		     }
		 };
		
		// Set basic auth for http client
		CourseraRestClient.setAuth(email, password);
		// Send POST request
		CourseraRestClient.post(Utils.CONTEXT, LOGIN_URL, headers, entity, contentType, mResponseHandler);
}
	
	/**
	 * Prepares and returns login headers
	 * @return array of login headers
	 */
	public static Header[] prepareHeaders() {
		// Prepare headers
		BasicHeader referer = new BasicHeader("Referer", "https://accounts.coursera.org/signin");
		BasicHeader origin = new BasicHeader("Origin", "https://accounts.coursera.org");
		BasicHeader csrftoken = new BasicHeader("X-CSRFToken", getCsrfToken());
		BasicHeader cookie = new BasicHeader("Cookie", "csrftoken=" + getCsrfToken());
		BasicHeader xrequestedwith = new BasicHeader("X-Requested-With", "XMLHttpRequest");
		BasicHeader contenttype = new BasicHeader("Content-type", "application/x-www-form-urlencoded");
		BasicHeader useragent = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0");
		BasicHeader accept = new BasicHeader("Accept", "*/*");
		BasicHeader acceptencoding = new BasicHeader("Accept-Encoding", "gzip,deflate,sdch");
		Header[] headers = {referer, origin, csrftoken, cookie, xrequestedwith, contenttype, useragent, accept, acceptencoding};
		return headers;
	}
	
	private static String getCsrfToken() {
		String csrfToken = "";
		for(int i = 0; i < CourseraRestClient.cookieStore.getCookies().size(); i++) {
			if(CourseraRestClient.cookieStore.getCookies().get(i).getName().equals("csrf_token")){
				CourseraRestClient.cookieStore.getCookies().get(i).getValue();
			}
		}
		return csrfToken;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	private void showAlertDialog(String title) {
		new AlertDialog.Builder(this)
	    .setTitle(title)
	    .setIcon(R.drawable.ic_dialog_alert)
	    .show();
	}
	
	private void getUserId() {

        // Send GET request
        CourseraRestClient.get(SIGNEDIN_URL, new TextHttpResponseHandler() {
            @Override
		     public void onStart() {
		         // Initiated the request
		     }

            @Override
		     public void onSuccess(int i, Header[] header, String response) {
		         // Successfully got a response
				 String userId = "";
				 Pattern USER_ID_PATTERN = Pattern.compile((String)("id\\\\u0022:\\s*[0-9]+"));
				 Matcher matcher = USER_ID_PATTERN.matcher((CharSequence)response);
				 if (matcher.find() || ((userId = matcher.group()).split(":").length > 0)) {
				 	userId = matcher.group().split(":")[1].trim();
				 }
				// Successfully got a response
				session.createLoginSession(mEmail, mPassword, Utils.getVersion(Utils.CONTEXT), userId);
			    Intent intent = new Intent(LoginActivity.this, CourseListActivity.class);
			    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    startActivity(intent);
			    finish();
		     }

             @Override
		     public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error)
		     {
		        // Response failed :(
				new AlertDialog.Builder(getApplicationContext())
			    .setTitle(getString(R.string.error_network))
			    .setMessage(getString(R.string.error_processing_request))
			    .setIcon(R.drawable.ic_dialog_alert)
			    .show();
		     }

            @Override
		     public void onFinish() {
		         // Completed the request (either success or failure)
		     }
		 });
	}
}