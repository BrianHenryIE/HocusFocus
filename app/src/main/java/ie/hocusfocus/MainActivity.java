package ie.hocusfocus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.axio.melonplatformkit.AnalysisResult;
import com.axio.melonplatformkit.DeviceHandle;
import com.axio.melonplatformkit.DeviceManager;
import com.axio.melonplatformkit.SignalAnalyzer;
import com.axio.melonplatformkit.listeners.IDeviceManagerListener;
import com.axio.melonplatformkit.listeners.ISignalAnalyzerListener;

import java.util.Arrays;

import ie.hocusfocus.utils.MelonUtils;
import ie.hocusfocus.utils.RollingMovingAverage;
import ie.hocusfocus.utils.RollingMovingMinimum;

public class MainActivity extends AppCompatActivity implements IDeviceManagerListener {

    private String mMelonName;
    private TextView tvFocus;
    private ImageView mainImage;

    // seconds * samples per second... 2500 is a mad guess
    private int watchFocusLength = 2500;

    private float isFocusedThreshold = 0.6f;

    Float[] initialValues = new Float[watchFocusLength];


    RollingMovingMinimum rma;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvFocus = (TextView) findViewById(R.id.tvFocus);
        mainImage = (ImageView) findViewById(R.id.mainImage);

        mainImage.setImageResource(R.drawable.serene);
        // the rolling moving average setup
        Arrays.fill(initialValues, 0.0f);
        rma = new RollingMovingMinimum(initialValues);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMelonName = getSharedPreferences(getApplication().getPackageName(), MODE_PRIVATE).getString(Constants.PREF_MY_MELON_NAME, null);

        if (mMelonName == null) {
            startActivity(new Intent(this, ScannerActivity.class));
            finish();
            return;
        }

        getSupportActionBar().setSubtitle("Searching for " + mMelonName);
        DeviceManager.getManager().addListener(this);
        DeviceManager.getManager().startScan();

        boolean connected = false;
        for (DeviceHandle deviceHandle : DeviceManager.getManager().getConnectedDevices()) {
            if (MelonUtils.getMelonName(deviceHandle).equals(mMelonName)) {

                setAnalyzer(deviceHandle);

                connected = true;
            }
        }

        if (!connected) {
            for (DeviceHandle deviceHandle : DeviceManager.getManager().getAvailableDevices()) {
                if (MelonUtils.getMelonName(deviceHandle).equals(mMelonName)) {
                    deviceHandle.connect();
                }
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                getSharedPreferences(getApplication().getPackageName(), MODE_PRIVATE).edit().remove(Constants.PREF_MY_MELON_NAME).apply();
                startActivity(new Intent(this, ScannerActivity.class));
                finish();
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDeviceScanStopped() {

    }

    @Override
    public void onDeviceScanStarted() {

    }

    @Override
    public void onDeviceFound(DeviceHandle deviceHandle) {
        if (MelonUtils.getMelonName(deviceHandle).equals(mMelonName)) {
            deviceHandle.connect();
        }
    }

    @Override
    public void onDeviceReady(DeviceHandle deviceHandle) {
        setAnalyzer(deviceHandle);
    }

    @Override
    public void onDeviceDisconnected(DeviceHandle deviceHandle) {

    }

    @Override
    public void onDeviceConnected(DeviceHandle deviceHandle) {
        getSupportActionBar().setSubtitle("Connected to " + MelonUtils.getMelonName(deviceHandle));
    }

    @Override
    public void onDeviceConnecting(DeviceHandle deviceHandle) {
        getSupportActionBar().setSubtitle("Connecting to " + MelonUtils.getMelonName(deviceHandle));
    }

    @Override
    public void onDeviceUnknowStatus(DeviceHandle deviceHandle) {

    }

    private void setAnalyzer(DeviceHandle deviceHandle) {
        SignalAnalyzer signalAnalyzer = new SignalAnalyzer();

        signalAnalyzer.addListener(new ISignalAnalyzerListener() {
            @Override
            public void onAnalyzedSamples(SignalAnalyzer signalAnalyzer, AnalysisResult leftChannelAnalysis, AnalysisResult rightChannelAnalysis) {
                //The focus score is only stored from the left channel results.

                // 512 different [floats]
                // probably 1-125 Hz

//                leftChannelAnalysis.getFilteredSignal();

                // What filter range ... beta!

                tvFocus.setText("Current Focus: " + leftChannelAnalysis.getFocusScore());

                if(isSereneScene)
                    watchFocus(leftChannelAnalysis.getFocusScore());

            }
        });

        deviceHandle.addAnalyzer(signalAnalyzer);
        deviceHandle.startStreaming();
        DeviceManager.getManager().stopScan();
    }

    private boolean isSereneScene = true;


    private void watchFocus(float focusScore) {

        rma.add(focusScore);

        if(rma.getValue()>isFocusedThreshold){
            setNextPhoto();
            isSereneScene = false;
            Arrays.fill(initialValues, 0.0f);
        }

    }


    private void setNextPhoto() {
        Log.i("ie.hocusfocus", "setNextPhoto()");

    }

    // watch user's focus, when focus is above threshold for 15 seconds, move on to next image

    // show image for five seconds, record EEG for this period, go back to calming image



}
