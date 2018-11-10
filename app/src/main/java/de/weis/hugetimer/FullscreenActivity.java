package de.weis.hugetimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {
    private TextView timetext;
    private ImageButton btn_reset;
    private Handler sys_handler = new Handler();

    private Date startdate;
    private boolean running = false;
    private boolean startnew = true;
    long carryover_millis = 0;

    SimpleDateFormat hmf = new SimpleDateFormat("HH:mm");
    SimpleDateFormat sf = new SimpleDateFormat("ss");


    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
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
        }
    };


    public long[] diffdates(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        long orig_diff = different;

        different +=  carryover_millis;

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        long[] res = new long[4];
        res[0] = elapsedHours;
        res[1] = elapsedMinutes;
        res[2] = elapsedSeconds;
        res[3] = orig_diff;

        return res;
    }

    /* update time */
    private Runnable update_time = new Runnable() {
        @Override
        public void run() {

                long[] diff = diffdates(startdate, new Date());
                System.out.println("CurrentMillis: " + diff[3] + ", SavedCarryover: " + carryover_millis);

                String bodyData = String.format("%02d", diff[0])+":"+String.format("%02d",diff[1])+"<small><small><font color='#f88'>"+String.format("%02d", diff[2])+"</font></small></small>";
                //String bodyData = "bla";

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    timetext.setText(Html.fromHtml(bodyData,Html.FROM_HTML_MODE_LEGACY));
                } else {
                    timetext.setText(Html.fromHtml(bodyData));
                }
            if(running) {
                sys_handler.postDelayed(update_time, 100);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        btn_reset = (ImageButton) findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                reset();
            }
        });
        timetext = (TextView) findViewById(R.id.fullscreen_content);
        reset();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void toggle() {
        if(!running){ // start/restart
            startdate = new Date();
            running = true;
            sys_handler.postDelayed(update_time, 1);
            btn_reset.setVisibility(View.GONE);
        }else{ // pause
            btn_reset.setVisibility(View.VISIBLE);

            running = false;
            long old_carry = carryover_millis;
            carryover_millis += diffdates(startdate, new Date())[3];
            System.out.println("Carryover: " + carryover_millis);

            startdate = new Date();
            update_time.run();
        }
    }

    private void reset(){
        carryover_millis = 0;
        running = false;

        startdate = new Date();
        update_time.run();

        btn_reset.setVisibility(View.GONE);

        System.out.println("------------- RESET PRESSED");
    }
}
