package com.example.sistemasembarcados;

import android.Manifest;
import android.content.Context;
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
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import androidx.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    Mat mRGBA;
    Mat mRGBAT;
    CameraBridgeViewBase cameraBridgeViewBase;

    File cascFile;

    CascadeClassifier faceDetector;

    int rows = 600;
    int cols = 800;

    // int ch = mRgba.channels();
    //double R=0,G=0,B=0;

    double[][] matriz_vermelho = new double[rows][cols];
    double[][] matriz_azul = new double[rows][cols];

    double[][] matriz_antiga_vermelho = new double[rows][cols];
    double[][] matriz_antiga_azul = new double[rows][cols];

    double[][] matriz_dif_vermelho = new double[rows][cols];
    double[][] matriz_dif_azul = new double[rows][cols];

    int counter = 0;

    /*for(int i=0; i<rows; i++)
    {
        for (int j=0; j<cols; j++)
        {
            matriz_vermelho[i][j] = 0;
            matriz_azul[i][j] = 0;

            matriz_antiga_vermelho[i][j] = 0;
            matriz_antiga_azul[i][j] = 0;
        }
    }*/


    /*
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
    }; */

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) throws IOException {
            switch(status){
                case LoaderCallbackInterface.SUCCESS:
                {

                    Log.i(TAG, "onManagerConnect: OpenCV Loaded");
                    //cameraBridgeViewBase.enableView();

                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);

                    cascFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");


                    FileOutputStream fos = new FileOutputStream(cascFile);


                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while((bytesRead = is.read(buffer)) != -1){
                        fos.write(buffer, 0, bytesRead);
                    }

                    is.close();
                    fos.close();

                    faceDetector = new CascadeClassifier(cascFile.getAbsolutePath());

                    if(faceDetector.empty()){
                        faceDetector = null;
                    }
                    else{
                        cascadeDir.delete();
                    }

                    cameraBridgeViewBase.enableView();

                }
                break;

                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }

            //super.onManagerConnected(status);

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        setContentView(R.layout.camera_activity);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        //cameraBridgeViewBase.setCvCameraViewListener(this);

        if(!OpenCVLoader.initDebug()){

            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);

        } else{

            try {
                baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        cameraBridgeViewBase.setCvCameraViewListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // permissão camera
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case 1: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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

            try {
                baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        //mRGBAT = new Mat(height, width, CvType.CV_8UC4);
    }


    // processamento frame a frame
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        // frame colorido!
        mRGBA = inputFrame.rgba();
        //mRGBAT = inputFrame.gray();

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(mRGBA, faceDetections);

        for(Rect rect: faceDetections.toArray()){

            Imgproc.rectangle(mRGBA, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y+rect.height),
                    new Scalar(255, 0, 0));

        }

        // pega o frame no frame 10, 20 e 30, a cada 10 frames!
        if( (counter % 48) == 0){


            //cols = 800
            //rows = 600

            //Core.flip(frame, frame, 90);

            for(int i=0; i<rows; i++)
            {
                for (int j=0; j<cols; j++)
                {
                    double[] data = mRGBA.get(i, j); //Stores element in an array

                    // acessando valor de red/green/blue frame a frame
                    // salvar em outra matriz

                    //R = data[0];
                    //G= data[1];
                    //B = data[2];

                    matriz_antiga_vermelho[i][j] = matriz_vermelho[i][j];
                    matriz_antiga_azul[i][j] = matriz_azul[i][j];

                    matriz_vermelho[i][j] = data[0];
                    matriz_azul[i][j] = data[2];

                    // frames estão sendo recebidos, mas esse print faz o programa travar MUITO!

                    //Log.i(TAG, "onCameraFrame -> Matriz de Vermelho no Frame -> " + Int.toString(rows));
                    //Log.i(TAG, "onCameraFrame -> Matriz de Azul no Frame -> " + Int.toString(cols));


                }
            }


        }

        if((counter % 144) == 0){

            for(int i=0; i<rows; i++)
            {
                for (int j=0; j<cols; j++)
                {

                    matriz_dif_vermelho[i][j] = matriz_vermelho[i][j] - matriz_antiga_vermelho[i][j];
                    matriz_dif_azul[i][j] = matriz_azul[i][j] - matriz_antiga_azul[i][j];

                }
            }

        }

        counter = counter + 1;

        if(counter == 145){
            counter = 0;
        }

        return mRGBA;
    }



}
