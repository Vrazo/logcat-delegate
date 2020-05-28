package com.vrazo.logcat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("unused")
public abstract class LogCatDelegate {
    /**
     * Type definition for initialization exceptions.
     */
    public final static class InitializationException extends Exception {
        InitializationException(String error) {
            super(error);
        }
    }

    private Date mRegisteredAt;
    private boolean mRunning;
    private Process mProcess;
    private Thread mThread;
    private ArrayList<LogCatMessageFilter> messageFilters = new ArrayList<>();

    @NonNull
    private String commandLineArguments = "-b all";

    @Nullable
    private Runnable deregisteredCallback;

    /**
     * Called when a new LogCat message is received.
     *
     * It is important that you do not log any messages within the context of this delegate. If you
     * do you will end up stuck in a loop.
     *
     * @param message the message
     */
    protected abstract void onNewMessage(LogCatMessage message);

    /**
     * Called when an error is encountered during the operation of the delegate.
     *
     * @param error the error
     */
    protected void onException(Exception error) {
        /* Not implemented by default */
    }

    /**
     * Sets the command line arguments for the Log Cat child-process. The default value is
     * <pre>-b all</pre>.
     *
     * The <pre>-v (--format)</pre> command line argument is not supported.
     *
     * @param commandLineArguments the command line arguments for the Log Cat child-process.
     */
    public void setCommandLineArguments(@NonNull String commandLineArguments) {
        this.commandLineArguments = commandLineArguments;
    }

    /**
     * Adds a message filter to the delegate.
     *
     * @param messageFilter the filter
     */
    public void addMessageFilter(LogCatMessageFilter messageFilter) {
        this.messageFilters.add(messageFilter);
    }

    /**
     * Removes a message filter from the delegate.
     *
     * @param messageFilter the filter
     */
    public void removeMessageFilter(LogCatMessageFilter messageFilter) {
        this.messageFilters.remove(messageFilter);
    }

    /**
     * Registers this delegate so that it will start receiving LogCat messages.
     */
    public final void register() {
        if (isRegistered())
            return;

        this.mRunning = true;
        this.mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isRegistered()) {
                        String cliArgs = commandLineArguments;
                        if (cliArgs.contains("-v") || cliArgs.contains("--format")) {
                            throw new RuntimeException(
                                "LogCatDelegate does not support the -v (--format) " +
                                "command line argument."
                            );
                        }
                        mProcess = Runtime.getRuntime().exec("logcat " + cliArgs + " -v threadtime,epoch");
                        int exitCode = 0;
                        try {
                            exitCode = mProcess.exitValue();
                        } catch (IllegalThreadStateException ignored) {}
                        if(exitCode != 0) {
                            throw new InitializationException(
                                "invalid exit code for logcat invocation: " + exitCode
                            );
                        }

                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(mProcess.getInputStream()));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            LogCatMessage message = LogCatMessage.from(line);
                            if (message != null && (
                                message.getLoggedAt().after(mRegisteredAt) ||
                                message.getLoggedAt().equals(mRegisteredAt)
                            )) {
                                boolean messageAllowed = true;
                                for (LogCatMessageFilter filter : messageFilters) {
                                    messageAllowed = filter.isValid(message);
                                    if (!messageAllowed)
                                        break;
                                }

                                if (messageAllowed) {
                                    onNewMessage(message);
                                }
                            }
                        }
                        bufferedReader.close();
                    }
                }
                catch (Exception error) {
                    // Ignore interrupted exceptions
                    if (!(error instanceof InterruptedIOException)) {
                        onException(error);
                    }
                }
                finally {
                    if (deregisteredCallback != null) {
                        deregisteredCallback.run();
                        deregisteredCallback = null;
                    }
                    mRunning = false;
                }
            }
        });

        this.mRegisteredAt = new Date();
        this.mThread.start();
    }

    /**
     * De-registers this delegate so that it will stop receiving LogCat messages. This method will
     * block the calling thread until the delegate has been fully de-registered.
     */
    public final void deregister() {
        this.deregisterAsync(null);
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * De-registers this delegate so that it will stop receiving LogCat messages. This method will
     * not block the calling thread. When the delegate has been fully de-registered, the completed
     * callback will be invoked.
     *
     * @param completed the callback to invoke once the delegate has been fully de-registered.
     */
    public final void deregisterAsync(@Nullable Runnable completed) {
        if (!isRegistered())
            return;
        deregisteredCallback = completed;
        mRunning = false;
        mProcess.destroy();
    }

    /**
     * De-registers this delegate so that it will stop receiving LogCat messages. This method will
     * not block the calling thread.
     */
    public final void deregisterAsync() {
        this.deregisterAsync(null);
    }

    /**
     * Determine if this delegate is registered.
     *
     * @return true if it's registered, false otherwise
     */
    public final boolean isRegistered() {
        return mRunning;
    }
}
