package ie.hocusfocus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.axio.melonplatformkit.DeviceHandle;
import com.axio.melonplatformkit.DeviceManager;
import com.axio.melonplatformkit.listeners.IDeviceManagerListener;
import ie.hocusfocus.utils.MelonUtils;

public class ScannerActivity extends AppCompatActivity implements IDeviceManagerListener {
    private DeviceHandleAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && getSharedPreferences(getApplication().getPackageName(), MODE_PRIVATE).contains(Constants.PREF_MY_MELON_NAME)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        ListView lvDevices = (ListView) findViewById(R.id.lvDevices);
        mAdapter = new DeviceHandleAdapter();
        lvDevices.setAdapter(mAdapter);

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeviceHandle deviceHandle = DeviceManager.getManager().getAvailableDevices().get
                        (position);

                switch (deviceHandle.getState()) {
                    case CONNECTED:
                        deviceHandle.disconnect();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case DISCONNECTED:
                        deviceHandle.connect();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case CONNECTING:
                    default:
                        //do nothing
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //As of Android 6.0 Marshmallow we require location permission in order to get the names of bluetooth LE devices like the Melon.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            DeviceManager.getManager().addListener(this);
            DeviceManager.getManager().startScan();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this).setMessage("This application needs to know your approximate position in order to connect to your Melon.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(ScannerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERMISSIONS);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERMISSIONS);
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (DeviceManager.getManager().isScanning()) {
            DeviceManager.getManager().removeListener(this);
            DeviceManager.getManager().stopScan();
        }

    }

    @Override
    public void onDeviceScanStopped() {
        Toast.makeText(ScannerActivity.this, "Device Scan Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceScanStarted() {
        Toast.makeText(ScannerActivity.this, "Device Scan Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceFound(DeviceHandle deviceHandle) {
        Toast.makeText(ScannerActivity.this, "Device Found", Toast.LENGTH_SHORT).show();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceReady(DeviceHandle deviceHandle) {
        Toast.makeText(ScannerActivity.this, "Device Ready", Toast.LENGTH_SHORT).show();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceDisconnected(DeviceHandle deviceHandle) {
        Toast.makeText(ScannerActivity.this, "Device Disconnected", Toast.LENGTH_SHORT).show();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceConnected(final DeviceHandle deviceHandle) {
        Toast.makeText(ScannerActivity.this, "Device Connected", Toast.LENGTH_SHORT).show();
        mAdapter.notifyDataSetChanged();
        new AlertDialog.Builder(this)
                .setTitle("Is " + MelonUtils.getMelonName(deviceHandle) + " your Melon?")
                .setMessage("Your Melon's light will have changed from flashing to solid if it is" +
                        " connected.")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getSharedPreferences(getApplication()
                                                .getPackageName(),
                                        MODE_PRIVATE)
                                        .edit()
                                        .putString(Constants.PREF_MY_MELON_NAME,
                                                MelonUtils.getMelonName(deviceHandle))
                                        .apply();
                                deviceHandle.disconnect();
                                new AlertDialog.Builder(ScannerActivity.this)
                                        .setMessage("Cool! I'll connect to your " +
                                                "Melon next time I see it!")
                                        .setPositiveButton("THANKS", new
                                                DialogInterface
                                                        .OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        dialog.dismiss();
                                                        startActivity(new Intent(ScannerActivity.this, MainActivity.class));
                                                        finish();
                                                    }
                                                })
                                        .create()
                                        .show();
                            }
                        }
                )
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceHandle.disconnect();
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onDeviceConnecting(DeviceHandle deviceHandle) {

    }

    @Override
    public void onDeviceUnknowStatus(DeviceHandle deviceHandle) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Constants.REQUEST_PERMISSIONS == requestCode) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                DeviceManager.getManager().addListener(this);
                DeviceManager.getManager().startScan();
            }
        }
    }

    class DeviceHandleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return DeviceManager.getManager().getAvailableDevices().size();
        }

        @Override
        public DeviceHandle getItem(int position) {
            return DeviceManager.getManager().getAvailableDevices().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceHandle deviceHandle = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2,
                        parent, false);
                viewHolder = new ViewHolder();
                viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
                viewHolder.text2 = (TextView) convertView.findViewById(android.R.id.text2);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.text1.setText("Name: " + MelonUtils.getMelonName(deviceHandle));
            viewHolder.text2.setText("State: " + deviceHandle.getState().name());


            return convertView;
        }

        class ViewHolder {
            TextView text1;
            TextView text2;
        }
    }
}


