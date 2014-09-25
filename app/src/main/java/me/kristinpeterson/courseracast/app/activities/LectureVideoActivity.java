package me.kristinpeterson.courseracast.app.activities;

import java.util.Timer;
import java.util.TimerTask;

import me.kristinpeterson.courseracast.app.CastApplication;
import me.kristinpeterson.courseracast.app.R;
import me.kristinpeterson.courseracast.app.fragments.LectureListFragment;
import me.kristinpeterson.courseracast.app.utils.CastUtils;
import me.kristinpeterson.courseracast.app.utils.SessionManager;
import me.kristinpeterson.courseracast.app.utils.Utils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

/**
 * An activity that displays the loaded lecture video
 * along with any related information. 
 */
public class LectureVideoActivity extends ActionBarActivity {
	
    private SessionManager session;
	private VideoView mVideoView;
    private TextView mTitleView;
    //private TextView mDescriptionView;
    //private TextView mAuthorView;
    private TextView mStartText;
    private TextView mEndText;
    private SeekBar mSeekbar;
    private ImageView mPlayPause;
    private ProgressBar mLoading;
    private View mContainer;
    private ImageView mCoverArt;
    private VideoCastManager mCastManager;
    private Timer mSeekbarTimer;
    private Timer mControlersTimer;
    private PlaybackLocation mLocation;
    private PlaybackState mPlaybackState;
    private final Handler mHandler = new Handler();
    private Point mDisplaySize;
    private final float mAspectRatio = 72f / 128;
    private MediaInfo mSelectedMedia;
    private boolean mControlersVisible;
    private int mDuration;
    private MiniController mMini;
    protected MediaInfo mRemoteMediaInformation;
    private VideoCastConsumerImpl mCastConsumer;
    private boolean mShouldStartPlayback;
    private View mControlers;
    private int mGroupPosition;
    private int mChildPosition;
    
    /**
     * Indicates whether we are doing a local or a remote playback
     */
    public static enum PlaybackLocation {
        /**
         * Playback on the device
         */
        LOCAL,
        /**
         * Playback on Chromecast
         */
        REMOTE;
    }
    
    /**
     * List of various states that we can be in
     */
    public static enum PlaybackState {
        /**
         * Video is playing
         */
        PLAYING, 
        /**
         * Video is paused
         */
        PAUSED, 
        /**
         * Video is buffering
         */
        BUFFERING, 
        /**
         * Video is idle
         */
        IDLE;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Utils.CONTEXT = getApplicationContext();
		setContentView(R.layout.activity_lecture_video);
		
		// Checking if user is logged in, redirecting to LoginActivity if not
        session = new SessionManager(Utils.CONTEXT);
        session.setLogoutReceiver(LectureVideoActivity.this);
        session.checkLogin();
		
		// Initializing the VideoCastManager for this Activity
		mCastManager = CastApplication.getCastManager(this);
		
		loadViews();
		setupActionBar();
		setupControlsCallbacks();
		
		// Adding Chromecast mini controller
		mMini = (MiniController) findViewById(R.id.miniController1);
		mCastManager.addMiniController(mMini);
		setupCastListener();
		
		// Getting & setting video data
	    Bundle b = getIntent().getExtras();
	    if(null != b) {
	    	mGroupPosition = b.getInt("groupPosition");
	    	mChildPosition = b.getInt("childPosition");
	    	mSelectedMedia = com.google.sample.castcompanionlibrary.utils.Utils
                    .toMediaInfo(getIntent().getBundleExtra("media"));
            mShouldStartPlayback = b.getBoolean("shouldStart");
            int startPosition = b.getInt("startPosition", 0);
            Bitmap imgBitmap = (Bitmap) b.get(LectureListFragment.ARG_IMG_BITMAP);
		    mCoverArt.setImageBitmap(imgBitmap);
            mVideoView.setVideoURI(Uri.parse(mSelectedMedia.getContentId()));
            if (mShouldStartPlayback) {
                // this will be the case only if we are coming from the
                // CastControllerActivity by disconnecting from a device
                mPlaybackState = PlaybackState.PLAYING;
                updatePlaybackLocation(PlaybackLocation.LOCAL);
                updatePlayButton(mPlaybackState);
                if (startPosition > 0) {
                    mVideoView.seekTo(startPosition);
                }
                mVideoView.start();
                startControllersTimer();
            } else {
                // we should load the video but pause it
                // and show the album art.
                if (mCastManager.isConnected()) {
                    updatePlaybackLocation(PlaybackLocation.REMOTE);
                } else {
                    updatePlaybackLocation(PlaybackLocation.LOCAL);
                }
                mPlaybackState = PlaybackState.PAUSED;
                updatePlayButton(mPlaybackState);
            }
	    }
        if (null != mTitleView) {
            updateMetadata(true);
        }
	}
	
	@SuppressLint("NewApi")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
            updateMetadata(false);
            mContainer.setBackgroundColor(getResources().getColor(R.color.black));
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            updateMetadata(true);
            mContainer.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }
	
	private void updateMetadata(boolean visible) {
        if (!visible) {
            //mDescriptionView.setVisibility(View.GONE);
            mTitleView.setVisibility(View.GONE);
            //mAuthorView.setVisibility(View.GONE);
            mDisplaySize = CastUtils.getDisplaySize(this);
            RelativeLayout.LayoutParams lp = new
                    RelativeLayout.LayoutParams(mDisplaySize.x,
                            mDisplaySize.y + getSupportActionBar().getHeight());
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            mVideoView.setLayoutParams(lp);
            mVideoView.invalidate();
        } else {
            MediaMetadata mm = mSelectedMedia.getMetadata();
            //mDescriptionView.setText(mm.getString(MediaMetadata.KEY_STUDIO));
            mTitleView.setText(mm.getString(MediaMetadata.KEY_TITLE));
            //mAuthorView.setText(mm.getString(MediaMetadata.KEY_SUBTITLE));
            //mDescriptionView.setVisibility(View.VISIBLE);
            mTitleView.setVisibility(View.VISIBLE);
            //mAuthorView.setVisibility(View.VISIBLE);
            mDisplaySize = CastUtils.getDisplaySize(this);
            RelativeLayout.LayoutParams lp = new
                    RelativeLayout.LayoutParams(mDisplaySize.x,
                            (int) (mDisplaySize.x * mAspectRatio));
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mVideoView.setLayoutParams(lp);
            mVideoView.invalidate();
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	  super.onCreateOptionsMenu(menu);
	  getMenuInflater().inflate(R.menu.video_activity_options, menu);

		// Add cast menu option to Action bar
		mCastManager.addMediaRouterButton(menu,
				R.id.media_route_menu_item);

	  return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
        case R.id.action_logout:
        	session.logoutUser(false, null);
            return true;
        case R.id.action_rate:
        	Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.rate_link)));
        	startActivity(rateIntent);
           return true;
        case R.id.action_donate:
        	Intent donateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.paypal_donation_link)));
        	startActivity(donateIntent);
           return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    protected void onStart() {
        super.onStart();
    }

	@Override
	protected void onResume() {
		mCastManager = CastApplication.getCastManager(this);
		if (null != mCastManager) {
	        mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }
        super.onResume();
	}
	
	@Override
	protected void onPause() {
        super.onPause();
        if (mLocation == PlaybackLocation.LOCAL) {
            if (null != mSeekbarTimer) {
                mSeekbarTimer.cancel();
                mSeekbarTimer = null;
            }
            if (null != mControlersTimer) {
                mControlersTimer.cancel();
            }
            // since we are playing locally, we need to stop the playback of
            // video (if user is not watching, pause it!)
            mVideoView.pause();
            mPlaybackState = PlaybackState.PAUSED;
            updatePlayButton(PlaybackState.PAUSED);
        }
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        mMini.removeOnMiniControllerChangedListener(mCastManager);
        mCastManager.decrementUiCounter();
	}
	
	@Override
    protected void onStop() {
        super.onStop();
    }
	
	@Override
	protected void onDestroy() {
        unregisterReceiver(session.getLogoutReceiver());
        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastConsumer = null;
        }
        stopControllersTimer();
        stopTrickplayTimer();
        super.onDestroy();
	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setBackgroundDrawable(
        //		getResources().getDrawable(R.drawable.ab_transparent_democastoverlay));
	}
	
	private void setupCastListener() {
        mCastConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata,
                String sessionId, boolean wasLaunched) {
                if (null != mSelectedMedia) {

                    if (mPlaybackState == PlaybackState.PLAYING) {
                        mVideoView.pause();
                        try {
                            loadRemoteMedia(mSeekbar.getProgress(), true);
                            finish();
                        } catch (Exception e) {
                            CastUtils.handleException(LectureVideoActivity.this, e);
                        }
                        return;
                    } else {
                        updatePlaybackLocation(PlaybackLocation.REMOTE);
                    }
                }
            }

            @Override
            public void onApplicationDisconnected(int errorCode) {
                updatePlaybackLocation(PlaybackLocation.LOCAL);
            }

            @Override
            public void onDisconnected() {
                mPlaybackState = PlaybackState.PAUSED;
                mLocation = PlaybackLocation.LOCAL;
            }

            @Override
            public void onRemoteMediaPlayerMetadataUpdated() {
                try {
                    mRemoteMediaInformation = mCastManager.getRemoteMediaInformation();
                } catch (Exception e) {
                    // silent
                }
            }

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                CastUtils.showToast(LectureVideoActivity.this,
                        R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                CastUtils.showToast(LectureVideoActivity.this,
                        R.string.connection_recovered);
            }

        };
    }
	
	private void updatePlaybackLocation(PlaybackLocation location) {
        this.mLocation = location;
        if (location == PlaybackLocation.LOCAL) {
            if (mPlaybackState == PlaybackState.PLAYING ||
                    mPlaybackState == PlaybackState.BUFFERING) {
                setCoverArtStatus(null);
                startControllersTimer();
            } else {
                stopControllersTimer();
                setCoverArtStatus(com.google.sample.castcompanionlibrary.utils.Utils.
                        getImageUrl(mSelectedMedia, 0));
            }

            getSupportActionBar().setTitle("");
        } else {
            stopControllersTimer();
            setCoverArtStatus(com.google.sample.castcompanionlibrary.utils.Utils.
                    getImageUrl(mSelectedMedia, 0));
            updateControlersVisibility(true);
        }
    }

    private void play(int position) {
    	setVideoViewed();
        startControllersTimer();
        switch (mLocation) {
            case LOCAL:
                mVideoView.seekTo(position);
                mVideoView.start();
                break;
            case REMOTE:
                mPlaybackState = PlaybackState.BUFFERING;
                updatePlayButton(mPlaybackState);
                try {
                    mCastManager.play(position);
                } catch (Exception e) {
                    CastUtils.handleException(this, e);
                }
                break;
            default:
                break;
        }
        restartTrickplayTimer();
    }

    private void togglePlayback() {
    	setVideoViewed();
        stopControllersTimer();
        switch (mPlaybackState) {
            case PAUSED:
                switch (mLocation) {
                    case LOCAL:
                        mVideoView.start();
                        mPlaybackState = PlaybackState.PLAYING;
                        startControllersTimer();
                        restartTrickplayTimer();
                        updatePlaybackLocation(PlaybackLocation.LOCAL);
                        break;
                    case REMOTE:
                        try {
                            mCastManager.checkConnectivity();
                            loadRemoteMedia(0, true);
                            finish();
                        } catch (Exception e) {
                            CastUtils.handleException(LectureVideoActivity.this, e);
                            return;
                        }
                        break;
                    default:
                        break;
                }
                break;

            case PLAYING:
                mPlaybackState = PlaybackState.PAUSED;
                mVideoView.pause();
                break;

            case IDLE:
                mVideoView.seekTo(0);
                mVideoView.start();
                mPlaybackState = PlaybackState.PLAYING;
                restartTrickplayTimer();
                break;

            default:
                break;
        }
        updatePlayButton(mPlaybackState);
    }

    private void loadRemoteMedia(int position, boolean autoPlay) {
        mCastManager.startCastControllerActivity(this, mSelectedMedia, position, autoPlay);
    }

    private void setCoverArtStatus(String url) {
        if (null != url) {
            mCoverArt.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.INVISIBLE);
        } else {
            mCoverArt.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
        }
    }
 
    private void stopTrickplayTimer() {
        if (null != mSeekbarTimer) {
            mSeekbarTimer.cancel();
        }
    }

    private void restartTrickplayTimer() {
        stopTrickplayTimer();
        mSeekbarTimer = new Timer();
        mSeekbarTimer.scheduleAtFixedRate(new UpdateSeekbarTask(), 100, 1000);
    }

    private void stopControllersTimer() {
        if (null != mControlersTimer) {
            mControlersTimer.cancel();
        }
    }

    private void startControllersTimer() {
        if (null != mControlersTimer) {
            mControlersTimer.cancel();
        }
        if (mLocation == PlaybackLocation.REMOTE) {
            return;
        }
        mControlersTimer = new Timer();
        mControlersTimer.schedule(new HideControllersTask(), 5000);
    }
    
    // should be called from the main thread
    private void updateControlersVisibility(boolean show) {
        if (show) {
            getSupportActionBar().show();
            mControlers.setVisibility(View.VISIBLE);
        } else {
            getSupportActionBar().hide();
            mControlers.setVisibility(View.INVISIBLE);
        }
    }
    
  private class HideControllersTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateControlersVisibility(false);
                    mControlersVisible = false;
                }
            });

        }
    }

    private class UpdateSeekbarTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    int currentPos = 0;
                    if (mLocation == PlaybackLocation.LOCAL) {
                        currentPos = mVideoView.getCurrentPosition();
                        updateSeekbar(currentPos, mDuration);
                    }
                }
            });
        }
    }
    
   
    private void setupControlsCallbacks() {
        mVideoView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String msg = "";
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_unaccessible);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
                CastUtils.showErrorDialog(LectureVideoActivity.this, msg);
                mVideoView.stopPlayback();
                mPlaybackState = PlaybackState.IDLE;
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mDuration = mp.getDuration();
                mEndText.setText(com.google.sample.castcompanionlibrary.utils.Utils
                       .formatMillis(mDuration));
                mSeekbar.setMax(mDuration);
                restartTrickplayTimer();
            }
        });

        mVideoView.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopTrickplayTimer();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(PlaybackState.IDLE);
            }
        });

        mVideoView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mControlersVisible) {
                    updateControlersVisibility(true);
                }
                startControllersTimer();
                return false;
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlaybackState == PlaybackState.PLAYING) {
                    play(seekBar.getProgress());
                } else {
                    mVideoView.seekTo(seekBar.getProgress());
                }
                startControllersTimer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopTrickplayTimer();
                mVideoView.pause();
                stopControllersTimer();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                mStartText.setText(com.google.sample.castcompanionlibrary.utils.Utils
                        .formatMillis(progress));
            }
        });

        mPlayPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mLocation == PlaybackLocation.LOCAL) {
            return super.onKeyDown(keyCode, event);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            onVolumeChange(CastApplication.VOLUME_INCREMENT);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            onVolumeChange(-CastApplication.VOLUME_INCREMENT);
        } else {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    private void onVolumeChange(double volumeIncrement) {
        if (mCastManager == null) {
            return;
        }
        try {
            mCastManager.incrementVolume(volumeIncrement);
        } catch (Exception e) {
        }
    }
    
    private void updateSeekbar(int position, int duration) {
        mSeekbar.setProgress(position);
        mSeekbar.setMax(duration);
        mStartText.setText(com.google.sample.castcompanionlibrary.utils.Utils
                .formatMillis(position));
        mEndText.setText(com.google.sample.castcompanionlibrary.utils.Utils.formatMillis(duration));
    }

    private void updatePlayButton(PlaybackState state) {
        switch (state) {
            case PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_av_pause_dark));
                break;
            case PAUSED:
            case IDLE:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_av_play_dark));
                break;
            case BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
    
    private void loadViews() {
    	mVideoView = (VideoView) findViewById(R.id.lecture_video);
        mTitleView = (TextView) findViewById(R.id.textView1);
        //mDescriptionView = (TextView) findViewById(R.id.textView2);
        //mDescriptionView.setMovementMethod(new ScrollingMovementMethod());
        //mAuthorView = (TextView) findViewById(R.id.textView3);
        mStartText = (TextView) findViewById(R.id.startText);
        mEndText = (TextView) findViewById(R.id.endText);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        // mVolBar = (SeekBar) findViewById(R.id.seekBar2);
        mPlayPause = (ImageView) findViewById(R.id.imageView2);
        mLoading = (ProgressBar) findViewById(R.id.progressBar);
        // mVolumeMute = (ImageView) findViewById(R.id.imageView2);
        mControlers = findViewById(R.id.controllers);
        mContainer = findViewById(R.id.container);
        mCoverArt = (ImageView) findViewById(R.id.coverArtView);
    }
    
    private void setVideoViewed() {
    	if(LectureListFragment.mCourse.listDataChild.get(LectureListFragment.mCourse.listDataHeader.get(mGroupPosition))
			.get(mChildPosition).isViewed == false) {
    		LectureListFragment.mCourse.listDataChild.get(LectureListFragment.mCourse.listDataHeader.get(mGroupPosition))
				.get(mChildPosition).isViewed = true;
    		LectureListFragment.UPDATE_LIST = true;
    	}
    }
}
