package com.example.sistemasembarcados;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    static{
        if(OpenCVLoader.initDebug()){

            Log.d(TAG, "opencv instalado corretamente!");

        }
        else{

            Log.d(TAG, "opencv com erro!");

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void abrindo_camera(View view){
        Intent intent = new Intent(this, com.example.sistemasembarcados.CameraActivity.class);
        startActivity(intent);
    }

}