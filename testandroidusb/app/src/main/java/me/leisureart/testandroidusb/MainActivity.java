package me.leisureart.testandroidusb;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import org.w3c.dom.Text;


public class MainActivity extends ActionBarActivity {

    /**
     * Device novel
     */
    private UsbManager mUsbManager;
    private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MESSAGE_REFRESH:
//                    refreshDeviceList();
//                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
//                    break;
//                default:
//                    super.handleMessage(msg);
//                    break;
//            }
//        }
//
//    };
    private List<UsbSerialPort> mEntries = new ArrayList<UsbSerialPort>();
    private ArrayAdapter<UsbSerialPort> mAdapter;
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


    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialPort)}.
     * <p/>
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */

    public static String asciiBytesToString( byte[] bytes )
    {
        if ( (bytes == null) || (bytes.length == 0 ) )
        {
            return "";
        }

        char[] result = new char[bytes.length];

        for ( int i = 0; i < bytes.length; i++ )
        {
            result[i] = (char)bytes[i];
        }

        return new String( result );
    }

    // This is Listener
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d("", "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sInput = sInput + asciiBytesToString(data);
//                            for (byte bSingle : data) {
//                                sInput = sInput + Byte.toString(bSingle);
//                            }
//                            Toast.makeText(getApplicationContext(),Byte.toString(data[0]),100).show();
//                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    // Dont't bother onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etInputField = (EditText)findViewById(R.id.inputField);
        tvOutputDisplay = (TextView)findViewById(R.id.usbInputInfo);
        tvUsbStatus = (TextView)findViewById(R.id.usbStatus);

        /**
         * device novel
         */
        setDeviceOnCreate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * device novel
         */
//        mHandler.removeMessages(MESSAGE_REFRESH);
        /**
         * port novel
         */
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
        /**
         * device novel
         */
//        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
//        usbSerialPort = mEntries.get(0);
        // Open a connection to the first available driver.
//        setDeviceOnCreate();
//        refreshDeviceList();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(this, "Test", 1000).show();
            return;
        }
        UsbSerialDriver driver = availableDrivers.get(0);
        usbSerialPort = driver.getPorts().get(0);

        /**
         * port novel
         */
        Log.d("", "Resumed, port=" + usbSerialPort);
        Toast.makeText(this, "protected void onResume() {", 1000).show();
        if (usbSerialPort == null) {
            tvUsbStatus.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(usbSerialPort.getDriver().getDevice());
            if (connection == null) {
                tvUsbStatus.setText("Opening device failed");
                Toast.makeText(this, "Opening device failed", 1000).show();
                return;
            }

            try {
                usbSerialPort.open(connection);
//                usbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                usbSerialPort.setParameters(9600, 8, 1, 0);
            } catch (IOException e) {
                Log.e("", "Error setting up device: " + e.getMessage(), e);
                Toast.makeText(this, "Error setting up device: ", 1000).show();
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
            Toast.makeText(this, "Serial device: " + usbSerialPort.getClass().getSimpleName(), 1000).show();
            startUIThread();
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i("", "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (usbSerialPort != null) {
            Log.i("", "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(usbSerialPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    public char[] convertByteToCharArray(byte [] data, int size, int start ){
        char[] charArr = (new String(data)).substring(start,size).toCharArray();
        return charArr;
    }

    private void updateReceivedData(byte[] data) {
//        sInput = sInput + "<" + data.toString() + ">" + "[" + data.length + "]";
//        for (byte single : data) {
//            sInput = sInput + (char)single;
//        }
//        sInput = sInput + (char)data[0];

//        final String message = "Read " + data.length + " bytes: \n"
//                + HexDump.dumpHexString(data) + "\n\n";
//        tvOutputDisplay.append(message);
    }

//    private void updateReceivedData(byte[] data) {
////        byte[] data = new byte[] {97, 98, 99};
////        String str = new String(data);
////        sInput = sInput + str;
////        sInput = sInput + data.length + ",";
////        String str = IOUtils.toString(inputStream, "UTF-8");
//        sInput = sInput + IOUtils.toString(data, "US-ASCII");;
//    }

    static void show(Context context, UsbSerialPort port) {
        usbSerialPort = port;
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

    public void sendMessage(View view) {
        final String message = tvOutputDisplay.getText().toString() + etInputField.getText().toString();
        tvOutputDisplay.setText(message);
    }

    /**
     * device novel
     */
    private void setDeviceOnCreate() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(this, "Test", 1000).show();
            return;
        }
        UsbSerialDriver driver = availableDrivers.get(0);
        usbSerialPort = driver.getPorts().get(0);
        UsbDeviceConnection connection = mUsbManager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

    }

    /**
     * device novel
     */
    private void refreshDeviceListOrigin() {

        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params) {
                Log.d("", "Refreshing device list ...");
                SystemClock.sleep(1000);

                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

                final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
                for (final UsbSerialDriver driver : drivers) {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    Log.d("", String.format("+ %s: %s port%s",
                            driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
                    result.addAll(ports);
                }

                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result) {
                mEntries.clear();
                mEntries.addAll(result);
                mAdapter.notifyDataSetChanged();
                tvUsbStatus.setText(
                        String.format("%s device(s) found", Integer.valueOf(mEntries.size())));
                Log.d("", "Done refreshing, " + mEntries.size() + " entries found.");
                if (mEntries.size() > 0) {
//                    Toast.makeText(this, "if (mEntries.size() > 0) {", 1000).show();
                    usbSerialPort = mEntries.get(0);
                }
            }

        }.execute((Void) null);
    }


    private void startUIThread() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
//                        numBytesRead = port.read(buffer, 1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable(){
                        public void run() {
                            tvOutputDisplay.setText(sInput);
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
    }

    String hexToAscii(String s) throws IllegalArgumentException
    {
        int n = s.length();
        StringBuilder sb = new StringBuilder(n / 2);
        for (int i = 0; i < n; i += 2)
        {
            char a = s.charAt(i);
            char b = s.charAt(i + 1);
            sb.append((char) ((hexToInt(a) << 4) | hexToInt(b)));
        }
        return sb.toString();
    }

    static int hexToInt(char ch)
    {
        if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
        if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
        if ('0' <= ch && ch <= '9') { return ch - '0'; }
        throw new IllegalArgumentException(String.valueOf(ch));
    }
}
