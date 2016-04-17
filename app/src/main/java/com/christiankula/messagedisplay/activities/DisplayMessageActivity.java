package com.christiankula.messagedisplay.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.christiankula.messagedisplay.R;
import com.christiankula.messagedisplay.utils.ColorUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DisplayMessageActivity extends AppCompatActivity {

    public static final String COLOR_EXTRA = "COLOR";
    public static final String MESSAGE_EXTRA = "MESSAGE";
    public static final String TEXT_SIZE_EXTRA = "TEXT_SIZE";
    public static final String ALL_CAPS_EXTRA = "ALL_CAPS";
    private static final int CODE_RESULT_SHARE = 151;


    private int PX_64DP;
    private int PX_16DP;

    private File lastSharedImage = null;
    private String currentMessage = new String();
    private boolean screenShotIncoming = false;

    private void refreshMemory(File file) {
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);
    }

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private Activity APP_CONTEXT;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


            mContentView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (screenShotIncoming && visibility == 7) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                screenShot();
                                screenShotIncoming = false;
                            }
                        }, UI_ANIMATION_DELAY);

                    }
                }
            });
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                // actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_message);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        APP_CONTEXT = this;
        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);

        Resources r = getResources();

        PX_64DP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, r.getDisplayMetrics());
        PX_16DP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        Intent intent = getIntent();

        String color = intent.getStringExtra(COLOR_EXTRA);
        currentMessage = intent.getStringExtra(MESSAGE_EXTRA);

        if (StringUtils.isEmpty(currentMessage)) {
            currentMessage = getResources().getString(R.string.empty_message);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor(ColorUtils.getDarkerHex(color)));
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.share_button);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(ColorUtils.getDarkerHex(color))));


        findViewById(R.id.display_message_root).setBackgroundColor(Color.parseColor(color));
        TextView tv = ((TextView) findViewById(R.id.fullscreen_content));

        tv.setText(currentMessage);
        tv.setTextSize(intent.getIntExtra(TEXT_SIZE_EXTRA, 0));
        tv.setAllCaps(intent.getBooleanExtra(ALL_CAPS_EXTRA, true));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void refreshFabMargins() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.share_button);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                lp.setMargins(0, 0, PX_64DP, PX_16DP);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                lp.setMargins(0, 0, PX_16DP, PX_64DP);
                break;
        }

        fab.setLayoutParams(lp);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFabMargins();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshFabMargins();
    }

    public static void setMargins(View v, int left, int top, int right, int bottom) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            v.requestLayout();
        }
    }

    private void fadeOutView(final View view) {
        final Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(UI_ANIMATION_DELAY);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.startAnimation(fadeOut);
            }
        }, UI_ANIMATION_DELAY);

    }

    private void fadeInView(final View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(UI_ANIMATION_DELAY);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
        view.startAnimation(fadeIn);
    }


    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;
        fadeOutView(findViewById(R.id.share_button));
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CODE_RESULT_SHARE:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean saveAfterShare = prefs.getBoolean("save_after_share", true);

                if (!saveAfterShare) {
                    if (lastSharedImage.exists()) {
                        lastSharedImage.delete();
                        refreshMemory(lastSharedImage);
                    }
                }
                break;
        }
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        fadeInView(findViewById(R.id.share_button));
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public void prepareForScreenShot(View v) {
        screenShotIncoming = true;
        hide();
    }

    public void screenShot() {
        View rootView = findViewById(R.id.display_message_root).getRootView();
        rootView.setDrawingCacheEnabled(true);
        rootView.destroyDrawingCache();
        Bitmap bitmap = rootView.getDrawingCache();
        createImage(bitmap);
    }

    public void screenShot(View view) {
        Bitmap bitmap = getRootViewBitmap(view);
        createImage(bitmap);
    }

    public Bitmap getRootViewBitmap(View v) {
        View rootview = v.getRootView();
        rootview.setDrawingCacheEnabled(true);
        rootview.destroyDrawingCache();
        return rootview.getDrawingCache();
    }


    public void createImage(Bitmap bmp) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_need_permissions);
                builder.setMessage(R.string.message_need_permissions);
                builder.setPositiveButton("Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(APP_CONTEXT,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_WRITE_EXTERNAL_STORAGE);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                builder.show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");

            String s_now = sdf.format(now);

            File file = new File(Environment.getExternalStorageDirectory() + "/Message Display");
            file.mkdirs();

            file = new File(Environment.getExternalStorageDirectory() + "/Message Display/MD_" + s_now + "_" +
                    currentMessage + ".jpg");

            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(bytes.toByteArray());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (file != null && file.exists()) {
                lastSharedImage = file;

                refreshMemory(file);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                shareIntent.setType("image/jpeg");
                startActivityForResult(Intent.createChooser(shareIntent, getResources().getString(R.string.send_with)
                ), CODE_RESULT_SHARE);
            }
        }
    }
}
