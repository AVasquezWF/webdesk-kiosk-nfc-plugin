package uareu;

import android.util.Log;

public class CapturePollingThread implements Runnable {
    private static final String TAG = "CapturePollingThread";
    private volatile boolean running = false;
    private long pollingIntervalMs = 500;
    public CaptureCallback dataCapture;

    public void setIntervalMs(long newInterval) {
        this.pollingIntervalMs = newInterval;
    }
    
    public void setOnCapturedCallback(CaptureCallback dataCapture){
        this.dataCapture = dataCapture;
    }

    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        Log.d(TAG, "Polling thread started");
        while (running) {
            try {
                dataCapture.capture();
            } catch (Exception e) {
                Log.e(TAG, "Polling thread interrupted");
            }

            try {
                Thread.sleep(pollingIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.w(TAG, "Polling thread interrupted");
                break;
            }
        }
        Log.d(TAG, "Polling thread stopped");
    }
}