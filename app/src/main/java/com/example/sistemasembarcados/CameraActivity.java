package com.example.sistemasembarcados;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import androidx.annotation.NonNull;
import android.util.Log;


public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    Mat mRGBA;
    Mat mRGBAT;
    CameraBridgeViewBase cameraBridgeViewBase;

    int counter = 0;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {

            switch(status){
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "onManagerConnect: OpenCV Loaded");
                    cameraBridgeViewBase.enableView();
                }

                default:{
                    super.onManagerConnected(status);
                }

                break;
            }

            super.onManagerConnected(status);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        setContentView(R.layout.camera_activity);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // permissão camera
        switch(requestCode){

            case 1:{

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    cameraBridgeViewBase.setCameraPermissionGranted();

                } else {
                    // permissão negada

                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){

            Log.d(TAG, "onResume: OpenCV iniciado (onResume)");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        } else{

            Log.d(TAG, "onResume: OpenCV não iniciou (onResume)");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);

        }
    }

    @Override
    protected void onPause() { // tocar na tela
        super.onPause();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mRGBAT = new Mat(height, width, CvType.CV_8UC4);
    }


    // processamento frame a frame
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        // frame colorido!
        mRGBA = inputFrame.rgba();
        //mRGBAT = inputFrame.gray();

        // pega o frame no frame 10, 20 e 30, a cada 10 frames!
        if( (counter % 48) == 0){

            //Core.flip(frame, frame, 90);

            int rows = mRGBA.rows();
            int cols = mRGBA.cols();
            // int ch = mRgba.channels();
            double R=0,G=0,B=0;

            for (int i=0; i<rows; i++)
            {
                for (int j=0; j<cols; j++)
                {
                    double[] data = mRGBA.get(i, j); //Stores element in an array

                    // acessando valor de red/green/blue frame a frame
                    // salvar em outra matriz

                    R = data[0];
                    G= data[1];
                    B = data[2];
                }
            }


        }

        counter = counter + 1;

        if(counter == 49){
            counter = 0;
        }

        return mRGBA;
    }


}
