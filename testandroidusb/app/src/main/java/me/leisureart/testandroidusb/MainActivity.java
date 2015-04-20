package me.leisureart.testandroidusb;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import java.io.IOException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import java.util.List;
import android.util.Log;



public class MainActivity extends ActionBarActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void testUSBSerialPort () {
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }
// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbSerialPort port = driver.getPorts().get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }
        try {
            port.open(connection);
            port.setParameters(115200, 8, 1, 0);
            byte buffer[] = new byte[16];
            int numBytesRead = port.read(buffer, 1000);
            Log.d(TAG, "Read " + numBytesRead + " bytes.");
        } catch (IOException e) {

        } finally {
            try {
                port.close();
            } catch (IOException e) {

            } finally {

            }
        }
//        port.open(connection);
//        try {
////            port.setBaudRate(115200);
//            port.setParameters(115200, 8, 1, 0);
//            byte buffer[] = new byte[16];
//            int numBytesRead = port.read(buffer, 1000);
//            Log.d(TAG, "Read " + numBytesRead + " bytes.");
//        } catch (IOException e) {
//            // Deal with error.
//        } finally {
//            port.close();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
