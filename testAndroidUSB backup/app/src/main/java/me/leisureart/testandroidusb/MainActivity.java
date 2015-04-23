package me.leisureart.testandroidusb;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;


import java.io.IOException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


public class MainActivity extends ActionBarActivity {

    /**
     * Socket novell
     */
    public String sInput = "";
    private static UsbSerialPort usbSerialPort = null;
    public int numBytesRead = 0;
    public byte buffer[];
    boolean bReadTheadEnable = false;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;
    /**
     * UI novell
     */
    public EditText etInputField;
    public TextView tvOutputDisplay;
    public TextView tvUsbStatus;
    /**
     * Other novell
     */
    private final String TAG = SerialConsoleActivity.class.getSimpleName();


    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialPort)}.
     *
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */


    // This is Listener
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    // Dont't bother onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_console);
        etInputField = (EditText)findViewById(R.id.inputField);
        tvOutputDisplay = (TextView)findViewById(R.id.usbInputInfo);
        tvUsbStatus = (TextView)findViewById(R.id.usbStatus);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (usbSerialPort != null) {
            try {
                usbSerialPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            usbSerialPort = null;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resumed, port=" + usbSerialPort);
        if (usbSerialPort == null) {
            tvUsbStatus.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(usbSerialPort.getDriver().getDevice());
            if (connection == null) {
                tvUsbStatus.setText("Opening device failed");
                return;
            }

            try {
                usbSerialPort.open(connection);
                usbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                tvUsbStatus.setText("Error opening device: " + e.getMessage());
                try {
                    usbSerialPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                usbSerialPort = null;
                return;
            }
            tvUsbStatus.setText("Serial device: " + usbSerialPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (usbSerialPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(usbSerialPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n" + tvOutputDisplay.getText().toString();
        tvOutputDisplay.append(message);
    }

    /**
     * Starts the activity, using the supplied driver instance.
     *
     * @param context
     * @param driver
     */
    static void show(Context context, UsbSerialPort port) {
        usbSerialPort = port;
        final Intent intent = new Intent(context, SerialConsoleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }
}
