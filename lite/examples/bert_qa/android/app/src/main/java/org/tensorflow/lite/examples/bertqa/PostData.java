package org.tensorflow.lite.examples.bertqa;

//package com.example.androidjavaapp;
//
//        import android.content.Context;
//        import android.media.AudioManager;
//        import android.media.Ringtone;
//        import android.media.RingtoneManager;
//        import android.net.Uri;
//        import android.os.AsyncTask;
//        import android.os.Bundle;
//        import android.os.Handler;
//        import android.util.Log;
//        import android.view.View;
//        import android.widget.Button;
//        import android.widget.EditText;
//        import android.widget.Toast;
//        import androidx.appcompat.app.AppCompatActivity;
//        import com.android.volley.toolbox.HttpClientStack;
//        import org.json.JSONObject;
//        import java.io.BufferedReader;
//        import java.io.IOException;
//        import java.io.InputStreamReader;
//        import java.io.OutputStream;
//        import java.net.HttpURLConnection;
//        import java.net.URL;
//
//public class MainActivity extends AppCompatActivity {
//
//    // on below line creating a variable for button and edit text.
//    private Button postDataBtn;
//    private EditText nameEdt, jobEdt;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        // on below line we are initializing variables.
//        postDataBtn = findViewById(R.id.idBtnPostData);
//        nameEdt = findViewById(R.id.idEdtName);
//        jobEdt = findViewById(R.id.idEdtJob);
//
//        postDataBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (nameEdt.getText().toString().isEmpty() && jobEdt.getText().toString().isEmpty()) {
//                    Toast.makeText(MainActivity.this, "Please enter name and job", Toast.LENGTH_SHORT).show();
//                } else {
//                    try {
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("name", nameEdt.getText().toString());
//                        jsonObject.put("job", jobEdt.getText().toString());
//                        String jsonString = jsonObject.toString();
//                        new PostData().execute(jsonString);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        });
//    }

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import org.tensorflow.lite.task.text.qa.QaAnswer;

// on below line creating a class to post the data.
    class PostData {

    // on below line creating a url to post the data.
    URL url;
    {
        try {
            url = new URL(String.format("http://%s:5000/jobsubmit/","172.20.10.14"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    // on below line opening the connection.
    HttpURLConnection client;
    {
        try {
            client = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init() {
        try {
            client.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
            client.setRequestProperty("Content-Type", "application/json");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
        }
        public QaAnswer send_request(String... strings) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                try (OutputStream os = client.getOutputStream()) {
                    byte[] input = strings[0].getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                // on below line creating and initializing buffer reader.
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject result = new JSONObject(String.valueOf(response));
                    String answer = result.getString("text");
                    int start = result.getInt("start");
                    int end = result.getInt("end");
                    float logit = result.getLong("logit");
                    QaAnswer rais_answer = new QaAnswer(answer,start,end,logit);
                    return rais_answer;
                }

            } catch (Exception e) {
                Log.d("exception", String.valueOf(e));
                // on below line handling the exception.
                e.printStackTrace();
                //Toast.makeText(MainActivity.this, "Fail to post the data : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }