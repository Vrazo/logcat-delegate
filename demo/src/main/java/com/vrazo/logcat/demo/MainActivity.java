package com.vrazo.logcat.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import com.vrazo.logcat.LogCatDelegate;
import com.vrazo.logcat.LogCatMessage;
import com.vrazo.logcat.LogCatMessageFilter;
import com.vrazo.logcat.LogCatPriority;
import com.vrazo.logcat.LogCatPriorityFilter;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ScrollView scroller;
    TextView logView;
    CheckBox autoScrollCheckBox;
    Button controlButton;

    LogCatDelegate logCatDelegate;
    LogCatMessageFilter logCatMessageFilter;
    LogCatPriorityFilter logCatPriorityFilter;

    /**
     * Initialize the view components and the delegate.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Initialize the view components.
         */
        scroller = findViewById(R.id.scroller);
        logView = findViewById(R.id.logView);
        autoScrollCheckBox = findViewById(R.id.autoScrollCheckBox);
        findViewById(R.id.clearButton).setOnClickListener(this);
        findViewById(R.id.sendRandomMessageButton).setOnClickListener(this);
        controlButton = findViewById(R.id.controlButton);
        controlButton.setOnClickListener(this);
        logView.setHorizontallyScrolling(true);
        logView.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Initialize the Log Cat Delegate.
         */
        logCatDelegate = new LogCatDelegate() {
            /**
             * Called each time a new message is available.
             *
             * @param message the message
             */
            @Override
            protected void onNewMessage(final LogCatMessage message) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        // Append the message to the log view and if autoScroll is enabled, scroll
                        // the log view to the bottom.
                        logView.append(message.getFormatted("%d %v %t %m") + "\n");
                        if (autoScrollCheckBox.isChecked()) {
                            scroller.post(new Runnable() {
                                @Override
                                public void run() {
                                    scroller.smoothScrollTo(logView.getLeft(), logView.getBottom());
                                }
                            });
                        }
                    }
                });
            }
        };

        // Test out the filter functionality.
        // If you want  all log cat messages to display, comment this section out.
        //
        // This filter will only display messages with the tag `LogCatDelegate-Demo`.
        logCatMessageFilter = new LogCatMessageFilter("LogCatDelegate\\-Demo");
        logCatMessageFilter.setMessageSpan(LogCatMessageFilter.MessageSpan.Tag);
        logCatDelegate.addMessageFilter(logCatMessageFilter);
        //
        // This filter will only display message with the INFO priority.
        logCatPriorityFilter = new LogCatPriorityFilter(new ArrayList<LogCatPriority>() {{
            add(new LogCatPriority(Log.INFO));
        }});
        logCatDelegate.addMessageFilter(logCatPriorityFilter);
    }

    /**
     * Toggles the delegate between Registered and De-Registered
     */
    public void toggleDelegate() {
        if (this.logCatDelegate.isRegistered()) {
            controlButton.setText("Stopping...");
            controlButton.setEnabled(false);
            this.logCatDelegate.deregisterAsync(new Runnable() {
                @Override
                public void run() {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            controlButton.setText("Start");
                            controlButton.setEnabled(true);
                        }
                    });
                }
            });
        } else {
            this.logCatDelegate.register();
            controlButton.setText("Stop");
            controlButton.setEnabled(true);
        }
    }

    /**
     * Sends a random message to log cat.
     */
    public void sendRandomMessage() {
        // Store a list of messages that can be sent along with accompanying priorities
        final ArrayList<Pair<Integer, String>> messages
                = new ArrayList<Pair<Integer, String>>() {{
            add(new Pair<>(Log.VERBOSE, "Hello, world! I am a log message!"));
            add(new Pair<>(Log.ERROR, "Oh no, I've messed up!"));
            add(new Pair<>(Log.WARN, "I'm warning you!"));
            add(new Pair<>(Log.INFO, "Well, this has all been super informative."));
            add(new Pair<>(Log.DEBUG, "No bugs here :)"));
        }};

        final Random random = new Random();

        // Retrieve a random message from the list
        int idx = random.nextInt(messages.size());
        Pair<Integer, String> message = messages.get(idx);

        // Log the message
        switch(message.first) {
            case Log.VERBOSE:
                Log.v("LogCatDelegate-Demo", message.second);
                break;
            case Log.DEBUG:
                Log.d("LogCatDelegate-Demo", message.second);
                break;
            case Log.INFO:
                Log.i("LogCatDelegate-Demo", message.second);
                break;
            case Log.WARN:
                Log.w("LogCatDelegate-Demo", message.second);
                break;
            case Log.ERROR:
                Log.e("LogCatDelegate-Demo", message.second);
                break;
        }
    }

    /**
     * Handles the click events for buttons.
     *
     * @param view the view that was clicked
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.clearButton) {
            logView.setText("");
        } else if (view.getId() == R.id.sendRandomMessageButton) {
            sendRandomMessage();
        } else if (view.getId() == R.id.controlButton) {
            toggleDelegate();
        }
    }

    /**
     * Utility function for running code on the main thread.
     *
     * @param runnable the runnable to post
     */
    public void runOnMainThread(Runnable runnable) {
        new Handler(getMainLooper()).post(runnable);
    }
}
