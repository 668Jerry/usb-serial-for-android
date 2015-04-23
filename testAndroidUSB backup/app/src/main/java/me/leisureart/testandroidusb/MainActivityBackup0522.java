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


public class MainActivityBackup0522 extends ActionBarActivity {

    /**
     * Socket novell
     */
    public String sInput = "";
    public UsbSerialPort usbSerialPort = null;
    public int numBytesRead = 0;
    public byte buffer[];
    boolean bReadTheadEnable = false;
    /**
     * UI novell
     */
    public EditText etInputField;
    public TextView tvOutputDisplay;
    public TextView tvUsbStatus;
    
    private void setUSBSerialPortListen () {
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(this, "Test", 1000).show();
            return;
        }
        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        usbSerialPort = driver.getPorts().get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            tvUsbStatus.setText("Opening device failed");
            return;
        }
        try {
            usbSerialPort.open(connection);
            usbSerialPort.setParameters(115200, 8, 1, 0);


            buffer = new byte[16];
            int numBytesRead = usbSerialPort.read(buffer, 1000);
            Toast.makeText(this, "Read " + numBytesRead + " bytes.", 200).show();
        } catch (IOException e) {
            Log.e("", "Error setting up device: " + e.getMessage(), e);
            tvUsbStatus.setText("Error opening device: " + e.getMessage());
            try {
                usbSerialPort.close();
            } catch (IOException e2) {
                // Ignore.
            }
            usbSerialPort = null;
            return;
        } finally {
            tvUsbStatus.setText("Serial device: " + usbSerialPort.getClass().getSimpleName());
//            try {
//                usbSerialPort.close();
//            } catch (IOException e) {
//                Toast.makeText(this, "Close Failed.", 200).show();
//            }
        }
    }

    private void startUIThread() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            private long startTime = System.currentTimeMillis();
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
//                        numBytesRead = usbSerialPort.read(buffer, 1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable(){
                        public void run() {
                            tvOutputDisplay.setText(sInput);
                            for (int i = 0; i < numBytesRead; i++) {
                                sInput = sInput + Byte.toString(buffer[i]);
                            }
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInputField = (EditText)findViewById(R.id.inputField);
        tvOutputDisplay = (TextView)findViewById(R.id.usbInputInfo);
        tvUsbStatus = (TextView)findViewById(R.id.usbStatus);
        
        setUSBSerialPortListen();
        ReadThread readThread = new ReadThread(handler);
        readThread.start();
//        startUIThread();
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

    class ReadThread extends Thread
    {
        final int USB_DATA_BUFFER = 8192;

        Handler mHandler;
        ReadThread(Handler h)
        {
            mHandler = h;
            this.setPriority(MAX_PRIORITY);
        }

        public void run()
        {
            Context context = getApplicationContext();
            bReadTheadEnable = true;
            int i = 0;
            while (true == bReadTheadEnable)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    i ++;
                    sInput = new Integer(i).toString(i);
//                    int numBytesRead = usbSerialPort.read(buffer, 1000);

                    Log.d("abc",sInput);
//                    Toast.makeText(context,"go",10).show();
                }
            }
        }
    }

    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {

        }
    };

//    private void startTimerThread() {
//        Runnable runnable = new Runnable() {
//            private long startTime = System.currentTimeMillis();
//            public void run() {
//                int i = 1;
//                while (true) {
//                    try {
//                        Thread.sleep(1000);
//                    }
//                    catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    handler.post(new Runnable(){
//                        public void run() {
////                            i ++;
////                            tvOutputDisplay.setText("i");
//                        }
//                    });
//                }
//            }
//        };
//        new Thread(runnable).start();
//    }

//    // add data to UI(@+id/ReadValues)
//    void appendData(String data)
//    {
//        if(true == bContentFormatHex)
//        {
//            if(iTimesMessageHexFormatWriteData < 3)
//            {
//                iTimesMessageHexFormatWriteData++;
//                midToast("The writing data won��t be showed on data area while content format is hexadecimal format.",Toast.LENGTH_LONG);
//            }
//            return;
//        }
//
//        if(true == bSendHexData)
//        {
//            SpannableString text = new SpannableString(data);
//            text.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, data.length(), 0);
//            tvReadText.append(text);
//            bSendHexData = false;
//        }
//        else
//        {
//            tvReadText.append(data);
//        }
//
//        int overLine = tvReadText.getLineCount() - fiTEXT_MAX_LINE;
//
//        if (overLine > 0)
//        {
//            int IndexEndOfLine = 0;
//            CharSequence charSequence = tvReadText.getText();
//
//            for (int i = 0; i < overLine; i++)
//            {
//                do
//                {
//                    IndexEndOfLine++;
//                }
//                while (IndexEndOfLine < charSequence.length() && charSequence.charAt(IndexEndOfLine) != '\n');
//            }
//
//            if (IndexEndOfLine < charSequence.length())
//            {
//                tvReadText.getEditableText().delete(0, IndexEndOfLine + 1);
//            }
//            else
//            {
//                tvReadText.setText("");
//            }
//        }
//
//        tvScrollView.smoothScrollTo(0, tvReadText.getHeight() + 30);
//    }

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
        new SerialInputOutputManager.Listener() {

            @Override
            public void onRunError(Exception e) {
                Log.d("", "Runner stopped.");
            }

            @Override
            public void onNewData(final byte[] data) {
                MainActivityBackup0522.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivityBackup0522.this.sendMessage(data);
                    }
                });
            }
        };


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

    public void sendMessage(View view) {
        tvOutputDisplay.setText(etInputField.getText().toString() + tvOutputDisplay.getText().toString());
    }

    private void sendMessage(byte[] data) {
        tvOutputDisplay.setText("Read " + data.length + " bytes: \n" + HexDump.dumpHexString(data) + "\n\n" + tvOutputDisplay.getText().toString());
    }


}
