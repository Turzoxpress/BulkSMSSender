package com.brl.bulksmssender;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity {


    private String TAG = "MainActivity";
    private Button choose_file,send_button;
    private TextView file_path,number_txt;
    private EditText main_message;

    private LinearLayout button_panel,loading_panel,panel3_msg;
    private TextView progress_text;
    private Button cancel;




    List<String> nArray = new ArrayList<String>();


    private Handler handler = new Handler();
    private Runnable runner;
    private boolean startHandler = false;

    private int iterator = 0;
    private boolean keepSending = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SmsManager.getDefault().sendTextMessage("01834261758", null, "Test SMS", null, null);
        choose_file = (Button)findViewById(R.id.choose_file);
        file_path = (TextView)findViewById(R.id.file_path_txt);
        number_txt = (TextView)findViewById(R.id.number_txt);
        main_message = (EditText)findViewById(R.id.main_message);
        send_button = (Button)findViewById(R.id.send_button);
        panel3_msg = (LinearLayout)findViewById(R.id.panel3_msg);


        button_panel = (LinearLayout)findViewById(R.id.button_panel);
        loading_panel = (LinearLayout)findViewById(R.id.loadin_oanel);
        progress_text = (TextView)findViewById(R.id.progress_txt);
        cancel = (Button) findViewById(R.id.cancel_button);

        //--

        send_button.setVisibility(View.INVISIBLE);
        main_message.setVisibility(View.INVISIBLE);
        panel3_msg.setVisibility(View.INVISIBLE);
        showButtonPanel();

        //--

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //--------



        //--
        choose_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                isReadStoragePermissionGranted();


            }
        });

        number_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showNumbers();
            }
        });

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startSendingSMS();
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                keepSending = false;
                handler.removeCallbacks(runner);

                Intent ni = new Intent(MainActivity.this,MainActivity.class);
                startActivity(ni);
                finish();

                //showButtonPanel();

            }
        });






        //-------------------




    }


    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                //--
                processFile();
                //-------
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            //--

            processFile();
            //---------
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d(TAG, "External storage2");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                   //-- task
                    processFile();
                    //--------
                }else{

                }
                break;


        }
    }

    private void processFile(){
        FileChooser fileChooser = new FileChooser(MainActivity.this);

        fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                // ....do something with the file
                String filename = file.getAbsolutePath();
                Log.d(TAG, filename);
                file_path.setText(filename);
                try {
                    readFile(filename);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // then actually do something in another module

            }
        });
// Set up and filter my extension I am looking for
        //fileChooser.setExtension("pdf");
        fileChooser.showDialog();
    }

    private void readFile(String path) throws IOException {

        FileInputStream is;
        BufferedReader reader;
        final File file = new File(path);

        nArray.clear();
        if (file.exists()) {
            is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while(line != null){

                nArray.add(line);

                line = reader.readLine();
            }
        }

        number_txt.setText("");
        String tempS = "";
        for (int i=0; i<nArray.size(); i++){

            Log.d("Numbers", nArray.get(i));
            tempS = tempS + nArray.get(i)+"\n";
        }

        number_txt.setText(tempS);
        send_button.setVisibility(View.VISIBLE);
        main_message.setVisibility(View.VISIBLE);
        panel3_msg.setVisibility(View.VISIBLE);


    }

    public void showNumbers() {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.number_show, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText input_number = (EditText) promptsView
                .findViewById(R.id.number_holder);



        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {


                                input_number.setText(number_txt.getText());
                            }
                        })
        ;

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void startSendingSMS(){

        hideButtonPanel();


        runner = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // Repetitive task


                //--------
                if(keepSending){

                    if(iterator >= nArray.size())
                    {


                        showButtonPanel();
                        allSMSSent();
                        Log.d(TAG,"Runner is running----------");
                        handler.removeCallbacks(runner);






                    }else{

                        //--

                        //SmsManager.getDefault().sendTextMessage(nArray.get(iterator), null, main_message.getText().toString(), null, null);

                        SmsManager sms = SmsManager.getDefault();
                        ArrayList<String> parts = sms.divideMessage(main_message.getText().toString());
                        sms.sendMultipartTextMessage(nArray.get(iterator), null, parts, null, null);


                        //-----------


                        progress_text.setText("Sending SMS to "+nArray.get(iterator)+"  , Total sent("+String.valueOf(iterator)+"/"+nArray.size()+")");
                        iterator++;

                        handler.postDelayed(this, 3000);

                    }



                }else {

                    handler.removeCallbacks(runner);
                    Log.d(TAG,"Cancel button was clicked!");
                }



                // task here


                //-- recursing the handler

            }


        };

        //-- start for first time

        handler.postDelayed(runner, 1000);
    }


    private void showButtonPanel(){

        button_panel.setVisibility(View.VISIBLE);
        loading_panel.setVisibility(View.GONE);
    }

    private void hideButtonPanel(){

        button_panel.setVisibility(View.GONE);
        loading_panel.setVisibility(View.VISIBLE);
    }


    @Override
    public void onBackPressed() {


       // handler.removeCallbacks(runner);

       // finish();

    }

    private void allSMSSent(){

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Congratulation!");
        alertDialog.setMessage("Alert message has been delivered to all of your numbers");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent ni = new Intent(MainActivity.this,MainActivity.class);
                        startActivity(ni);
                        finish();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }





}
