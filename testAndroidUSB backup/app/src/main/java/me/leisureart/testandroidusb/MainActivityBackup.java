package me.leisureart.testandroidusb;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Message;
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
import java.util.List;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


public class MainActivityBackup extends ActionBarActivity {
    public String sInput;
    public TextView textView;
    public byte buffer[];
    public UsbSerialPort port;
    public int numBytesRead = 0;
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
        port = driver.getPorts().get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }
        try {
            port.open(connection);
            port.setParameters(115200, 8, 1, 0);
            buffer = new byte[16];
            int numBytesRead = port.read(buffer, 1000);
            Toast.makeText(this, "Read " + numBytesRead + " bytes.", 200).show();
        } catch (IOException e) {

        } finally {
            try {
                port.close();
            } catch (IOException e) {
                Toast.makeText(this, "Close Failed.", 200).show();
            }
        }
    }

    private void startUIThread() {
        final Handler handler = new Handler();
        textView = (TextView)findViewById(R.id.usbInputInfo);
        Runnable runnable = new Runnable() {
            private long startTime = System.currentTimeMillis();
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
                            textView.setText(sInput);
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
        setUSBSerialPortListen();
        ReadThread readThread = new ReadThread(handler);
        readThread.start();
        startUIThread();
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

    public void sendMessage(View view) {
        ((TextView)findViewById(R.id.usbInputInfo)).setText(((EditText) findViewById(R.id.inputField)).getText().toString() + ((TextView) ((TextView) findViewById(R.id.usbInputInfo))).getText().toString());
    }

    boolean bReadTheadEnable = false;

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
//                    sInput = new Integer(i).toString(i);
//                    int numBytesRead = port.read(buffer, 1000);

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
//        TextView textView = (TextView)findViewById(R.id.usbInputInfo);
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
////                            textView.setText("i");
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

}
