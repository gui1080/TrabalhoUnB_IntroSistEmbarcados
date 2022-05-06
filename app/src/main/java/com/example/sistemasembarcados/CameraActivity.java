package com.example.sistemasembarcados;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;
import android.widget.TextView;
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
// Importing Arrays class from the utility class
import java.text.BreakIterator;
import java.util.Arrays;


public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    Mat mRGBA;
    Mat mRGBAT;
    CameraBridgeViewBase cameraBridgeViewBase;

    File cascFile;

    CascadeClassifier faceDetector;

    public double Sat;

    int rows = 600;
    int cols = 800;

    // int ch = mRgba.channels();
    //double R=0,G=0,B=0;

    //ver_sat.setText("saturação");

    // 360 -> 555-195
    // 367 -> 433-66
    public static int rows_aux = 50; // 142-92
    public static int cols_aux = 50; // 245-195

    int img_zigzag = 2500;// 50x50

    double[] zigzag_vermelho = new double[img_zigzag];
    double[] zigzag_azul = new double[img_zigzag];

    double[] zigzag_vermelho_sorted = new double[img_zigzag];
    double[] zigzag_azul_sorted = new double[img_zigzag];

    double[][] matriz_vermelho = new double[rows_aux][cols_aux];
    double[][] matriz_azul = new double[rows_aux][cols_aux];

    double[][] matriz_antiga_vermelho = new double[rows_aux][cols_aux];
    double[][] matriz_antiga_azul = new double[rows_aux][cols_aux];

    double[][] matriz_dif_vermelho = new double[rows_aux][cols_aux];
    double[][] matriz_dif_azul = new double[rows_aux][cols_aux];

    double alfa = 0.015;

    double media_picos_azul = 0;
    double media_picos_vermelho = 0;

    double total_azul = 0;
    double total_vermelho = 0;

    double[] picos_azul = new double[5];
    double[] picos_vermelho = new double[5];

    int[] posicoes_azul = new int[5];
    int[] posicoes_vermelho = new int[5];
    int counter = 0;
    int i=0;
    int j=0;

    /*
    for(i=0; i<rows_aux; i++){
        for (j=0; j<cols_aux; j++)
        {
            matriz_vermelho[i][j] = 0;
            matriz_azul[i][j] = 0;
            matriz_antiga_vermelho[i][j] = 0;
            matriz_antiga_azul[i][j] = 0;
        }
    }
    */


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

    private void setText(final TextView text, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    TextView ver_sat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        setContentView(R.layout.camera_activity);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        //cameraBridgeViewBase.setCvCameraViewListener(this);

        ver_sat = (TextView) findViewById(R.id.SATURACAO);
        //ver_sat.setText("Saturação: " + Sat);

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

        //TextView ver_sat = (TextView) findViewById(R.id.SATURACAO);
        //ver_sat.setText("Saturação: " + Sat);

        // saiu estranho os tamanhos da imagem, então vou recalcular com os valores da matriz aqui
        int h2 = mRGBA.width();
        int w2 = mRGBA.height();

        // se função acha rosto, aparece a detecção centralizada
        int h_rect = (h2/2) + 100;
        int w_rect = (w2*3) / 5;

        int xComeco = (w2 - w_rect + 150) / 2;
        int yComeco = (h2 - h_rect - 100) / 3;

        int xFim = (w2 + w_rect + 150) / 2;
        int yFim = (h2 + h_rect) / 3;

        int face_reconhecida = 0;

        //Log.d(TAG, "xComeco" + xComeco);
        //Log.d(TAG, "yComeco" + yComeco);
        //Log.d(TAG, "xFim" + xFim);
        //Log.d(TAG, "yFim" + yFim);

        //xComeco171
        //yComeco53
        //xFim459
        //yFim393

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(mRGBA, faceDetections);

        for(Rect rect: faceDetections.toArray()){

            // Draw rectangle
            Imgproc.rectangle(mRGBA, new Point
                    (xComeco, yComeco), new Point(
                    xFim, yFim), new Scalar(128, 0, 128), 4);

            face_reconhecida = 1;

        }

        int x = 0;
        int y = 0;



        if( (counter == 15) || (counter == 16) ){


            //cols = 800
            //rows = 600

            //Core.flip(frame, frame, 90);

            //rows_aux
            for(int i=92; i<142; i++)
            {

                y = 0;

                // cols_aux
                for (int j=195; j<245; j++)
                {
                    double[] data = mRGBA.get(i, j); //Stores element in an array

                    // acessando valor de red/green/blue frame a frame
                    // salvar em outra matriz

                    //R = data[0];
                    //G= data[1];
                    //B = data[2];

                    matriz_antiga_vermelho[x][y] = matriz_vermelho[x][y];
                    matriz_antiga_azul[x][y] = matriz_azul[x][y];

                    matriz_vermelho[x][y] = data[0];
                    matriz_azul[x][y] = data[2];

                    // frames estão sendo recebidos, mas esse print faz o programa travar MUITO!

                    //Log.i(TAG, "onCameraFrame -> Matriz de Vermelho no Frame -> " + Int.toString(rows));
                    //Log.i(TAG, "onCameraFrame -> Matriz de Azul no Frame -> " + Int.toString(cols));

                    y = y+1;

                }

                x = x+1;
            }


        }

        if( ((counter % 64) == 0) && (counter > 0)){

            for(int i=0; i<rows_aux; i++)
            {
                for (int j=0; j<cols_aux; j++)
                {

                    matriz_dif_vermelho[i][j] = matriz_vermelho[i][j] - matriz_antiga_vermelho[i][j];
                    matriz_dif_azul[i][j] = matriz_azul[i][j] - matriz_antiga_azul[i][j];

                }
            }

            matriz_dif_vermelho = dctTransform(matriz_dif_vermelho);
            matriz_dif_azul = dctTransform(matriz_dif_azul);

            // zigzag pela foto

            zigzag_vermelho = zigZagMatrix(matriz_dif_vermelho,  rows_aux, cols_aux);
            zigzag_azul     = zigZagMatrix(matriz_dif_azul,      rows_aux, cols_aux);

            // zera mais ou menos 20% da ponta
            double tirar_final = img_zigzag - (img_zigzag * 0.2);

            /*for(int i = (int) tirar_final ; i<img_zigzag; i++){

                zigzag_vermelho[i] = 0;
                zigzag_azul[i] = 0;

            }*/

            // R = (valor do primeiro pico / média dos picos) vermelho / (valor do primeiro pico / média dos picos) azul

            // vou pegar os 10 maiores valores do array
            // DC vai ser o primeiro maior

            zigzag_vermelho_sorted = zigzag_vermelho;
            zigzag_azul_sorted = zigzag_azul;
            Arrays.sort(zigzag_vermelho_sorted);
            Arrays.sort(zigzag_azul_sorted);

            int j = 0;

            for(int i = img_zigzag-1 ; i>(img_zigzag-5); i--){

                picos_azul[j] = zigzag_azul_sorted[i];
                picos_vermelho[j] = zigzag_vermelho_sorted[i];

                j = j+1;
            }

            int DC_azul = 0;
            int DC_vermelho = 0;

            for(int i = 0 ; i<5; i++){

                for(j = 0 ; j<img_zigzag; j++){

                    if(picos_azul[i] == zigzag_azul[j]){
                        posicoes_azul[i] = j;
                    }
                    if(picos_vermelho[i] == zigzag_vermelho[j]){
                        posicoes_vermelho[i] = j;
                    }

                }

            }


            for(int i = 0 ; i<5; i++){

                if(i == 0){
                    DC_azul = posicoes_azul[i];
                }
                else{
                    if(posicoes_azul[i] < DC_azul){
                        DC_azul = posicoes_azul[i];
                    }
                }
                // primeiro pico azul ocorre nessa posição

                if(i == 0){
                    DC_vermelho = posicoes_vermelho[i];
                }
                else{
                    if(posicoes_vermelho[i] < DC_vermelho){
                        DC_vermelho = posicoes_vermelho[i];
                    }
                }
                // primeiro vermelho ocorre nessa posição

            }


            for(int i = 0 ; i<5; i++){
                total_azul = total_azul + picos_azul[i];
                total_vermelho = total_vermelho + picos_vermelho[i];
            }

            media_picos_azul = total_azul/5;
            media_picos_vermelho = total_vermelho/5;

            // fa
            // alfa = 0.01SatO2 = 100 - R * al5

            double R = 0;

            R = ((zigzag_vermelho[DC_vermelho]/media_picos_vermelho) / (zigzag_azul[DC_azul]/media_picos_azul));


            Sat = 100 - (R * alfa);

            Log.d(TAG, "DC Vermelho -> " + DC_vermelho);
            Log.d(TAG, "DC Azul -> " + DC_azul);
            Log.d(TAG, "Media picos Vermelho -> " + media_picos_vermelho);
            Log.d(TAG, "Media picos Azul -> " + media_picos_azul);
            Log.d(TAG, "zigzag_vermelho[DC_vermelho] -> " + zigzag_vermelho[DC_vermelho]);
            Log.d(TAG, "zigzag_azul[DC_azul] -> " + zigzag_azul[DC_azul]);
            Log.d(TAG, "R -> " + R);
            Log.d(TAG, "SatO2 -> " + Sat);

            //ver_sat.setText("Saturação: " + Sat);
            setText( ver_sat, Double.toString(Sat) );

            //setText(ver_sat, Double.toString(Sat));

        }

        counter = counter + 1;

        if(counter == 65){
            counter = 0;
        }

        Log.d(TAG, "!!!!!!!!!!!!! Counter -> " + counter);

        return mRGBA;
    }

    // Função de transformada de matriz para "zigzag" adaptada do site geeksforgeeks
    // Shubham Bansal

    static double[] zigZagMatrix(double arr[][], int n, int m) {
        int row = 0, col = 0;
        int size = n*m;

        double result[] = new double[size];

        boolean row_inc = false;

        int count = 0;

        // Print matrix of lower half zig-zag pattern
        int mn = Math.min(m, n);
        for (int len = 1; len <= mn; ++len) {
            for (int i = 0; i < len; ++i) {
                //System.out.print(arr[row][col] + " ");
                result[count] = arr[row][col];
                count = count + 1;

                if (i + 1 == len)
                    break;

                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    --row;
                    ++col;
                }
            }

            if (len == mn)
                break;


            if (row_inc) {
                ++row;
                row_inc = false;
            } else {
                ++col;
                row_inc = true;
            }
        }

        if (row == 0) {
            if (col == m - 1)
                ++row;
            else
                ++col;
            row_inc = true;
        } else {
            if (row == n - 1)
                ++col;
            else
                ++row;
            row_inc = false;
        }

        int MAX = Math.max(m, n) - 1;
        for (int len, diag = MAX; diag > 0; --diag) {

            if (diag > mn)
                len = mn;
            else
                len = diag;

            for (int i = 0; i < len; ++i) {

                result[count] = arr[row][col];
                count = count + 1;

                if (i + 1 == len)
                    break;

                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    ++col;
                    --row;
                }
            }

            if (row == 0 || col == m - 1) {
                if (col == m - 1)
                    ++row;
                else
                    ++col;

                row_inc = true;
            }

            else if (col == 0 || row == n - 1) {
                if (row == n - 1)
                    ++col;
                else
                    ++row;

                row_inc = false;
            }
        }

        return result;
    }

    // Função DCT adaptada do site geeksforgeeks
    // Aditya Kumar

    //public static int n = 8,m = 8;
    public static double pi = 3.142857;

    static double[][] dctTransform(double matrix[][])
    {
        int i, j, k, l;

        // dct will store the discrete cosine transform
        double[][] dct = new double[rows_aux][cols_aux];

        double ci, cj, dct1, sum;

        for (i = 0; i < rows_aux; i++)
        {
            for (j = 0; j < cols_aux; j++)
            {
                // ci and cj depends on frequency as well as
                // number of row and columns of specified matrix
                if (i == 0)
                    ci = 1 / Math.sqrt(rows_aux);
                else
                    ci = Math.sqrt(2) / Math.sqrt(rows_aux);

                if (j == 0)
                    cj = 1 / Math.sqrt(cols_aux);
                else
                    cj = Math.sqrt(2) / Math.sqrt(cols_aux);

                // sum will temporarily store the sum of
                // cosine signals
                sum = 0;
                for (k = 0; k < rows_aux; k++)
                {
                    for (l = 0; l < cols_aux; l++)
                    {
                        dct1 = matrix[k][l] *
                            Math.cos((2 * k + 1) * i * pi / (2 * rows_aux)) *
                            Math.cos((2 * l + 1) * j * pi / (2 * cols_aux));
                            sum = sum + dct1;
                    }
                }
                dct[i][j] = ci * cj * sum;
            }

            Log.d(TAG, "!!!!!!!!!!!!! IIIII DCT Counter -> " + i);
        }

        return dct;

    }




    }
