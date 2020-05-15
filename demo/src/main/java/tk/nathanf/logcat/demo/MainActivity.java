package tk.nathanf.logcat.demo;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.widget.ScrollView;
import android.widget.TextView;

import tk.nathanf.logcat.LogCatDelegate;
import tk.nathanf.logcat.LogCatMessage;

public class MainActivity extends AppCompatActivity {
    ScrollView scroller;
    TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scroller = findViewById(R.id.scroller);
        logView = findViewById(R.id.logView);

        logView.setHorizontallyScrolling(true);
        logView.setMovementMethod(new ScrollingMovementMethod());

        LogCatDelegate logCatDelegate = new LogCatDelegate() {
            @Override
            public void onNewMessage(final LogCatMessage message) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        logView.append(message.getFormatted("%d %v %t %m") + "\n");
                    }
                });
            }
        };
        logCatDelegate.register();
    }

    public void runOnMainThread(Runnable runnable) {
        new Handler(getMainLooper()).post(runnable);
    }
}
