package tk.nathanf.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.Semaphore;

public abstract class LogCatDelegate {
    private Date mRegisteredAt;
    private boolean mRunning;
    private Semaphore mMutex;
    private Process mProcess;
    private Thread mThread;

    /**
     * Creates a new delegate that will receive any messages sent to LogCat.
     */
    public LogCatDelegate() {
        mMutex = new Semaphore(1);
    }

    /**
     * Called when a new LogCat message is received.
     *
     * @param message the message
     */
    public abstract void onNewMessage(LogCatMessage message);

    /**
     * Registers this delegate so that it will start receiving LogCat messages.
     */
    public void register() {
        if (mRunning)
            return;

        this.mRunning = true;
        this.mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMutex.acquire();
                    while (mRunning) {
                        mProcess = Runtime.getRuntime().exec("logcat -b all -v threadtime,epoch");
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(mProcess.getInputStream()));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            LogCatMessage message = LogCatMessage.parse(line);
                            if (message != null && (
                                message.getLoggedAt().after(mRegisteredAt) ||
                                message.getLoggedAt().equals(mRegisteredAt)
                            )) {
                                onNewMessage(message);
                            }
                        }
                        bufferedReader.close();
                    }
                    mMutex.release();
                }
                catch (IOException | InterruptedException ignored) {}
            }
        });

        this.mRegisteredAt = new Date();
        this.mThread.start();
    }

    /**
     * De-registers this delegate so that it will stop receiving LogCat messages. This method will
     * block the calling thread until the delegate has been fully de-registered.
     */
    public void deregister() {
        this.deregisterAsync();
        try {
            mMutex.acquire();
            mMutex.release();
        } catch (InterruptedException ignored) {}
    }

    /**
     * De-registers this delegate so that it will stop receiving LogCat messages. This method will
     * not block the calling thread.
     */
    public void deregisterAsync() {
        if (!mRunning)
            return;
        mRunning = false;
        mProcess.destroy();
    }
}
