package br.com.helpdev.facedetect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Runnable {

    private static final String TAG = "OCVSampleFaceDetect";
    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                cameraBridgeViewBase.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };
    private volatile boolean running = false;
    private volatile int qtdFaces;
    private volatile Mat matTmpProcessingFace;
    private CascadeClassifier cascadeClassifier;
    private File mCascadeFile;
    private TextView infoFaces;

    FaceRectView faceView;

    private RelativeLayout rlMain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        infoFaces = findViewById(R.id.tv);
        cameraBridgeViewBase = findViewById(R.id.main_surface);
        faceView=new FaceRectView(this);
        rlMain = findViewById(R.id.rl_main);
        faceView.setFaceRectList(mFaceList);
        rlMain.addView(faceView);
        loadHaarCascadeFile();
        checkPermissions();
    }
    int CAMERA_ID=CameraBridgeViewBase.CAMERA_ID_BACK;
    private void checkPermissions() {
        if (isPermissionGranted()) {
            loadCameraBridge(CAMERA_ID);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissions();
    }

    private void loadCameraBridge(int CAMERA_ID_) {
        cameraBridgeViewBase.setCameraIndex(CAMERA_ID_);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    private void loadHaarCascadeFile() {
        try {
            File cascadeDir = getDir("haarcascade_frontalface_alt", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");

            if (!mCascadeFile.exists()) {
                FileOutputStream os = new FileOutputStream(mCascadeFile);
                InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to load Haar Cascade file");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (!isPermissionGranted()) return;
        resumeOCV();
    }
    /**
     * 静默安装
     */
    private void resumeOCV() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        cascadeClassifier.load(mCascadeFile.getAbsolutePath());
        startFaceDetect();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (matTmpProcessingFace == null) {
            matTmpProcessingFace = inputFrame.gray();
        }
        return inputFrame.rgba();
    }


    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    public void startFaceDetect() {
        if (running) return;
        new Thread(this).start();
    }
    List<Rect> mFaceList=new ArrayList<>();
    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                if (matTmpProcessingFace != null) {
                    MatOfRect matOfRect = new MatOfRect();
                    cascadeClassifier.detectMultiScale(matTmpProcessingFace, matOfRect);

                    int newQtdFaces = matOfRect.toList().size();
                    List<Rect> faceList=matOfRect.toList();
                    faceView.clearFaceInfo();

                    if(faceList.size()>0)
                    {
                        faceView.addFaceInfo(faceList);
                        Log.i("textLog", " 发现 脸数 ：  " +faceList.size() );

                        for (int i = 0; i < faceList.size(); i++) {
                            Rect rect=faceList.get(i);
                            Log.i("textLog","height : " + rect.height);
                            Log.i("textLog","width : " + rect.width);
                            Log.i("textLog","x :  " + rect.x);
                            Log.i("textLog","y :  " + rect.y);
                        }
                    }
                    if (qtdFaces != newQtdFaces) {
                        qtdFaces = newQtdFaces;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                infoFaces.setText(String.format(getString(R.string.faces_detects), qtdFaces));
                            }
                        });
                    }
                    Thread.sleep(50);//if you want an interval
                    matTmpProcessingFace = null;
                }
                Thread.sleep(50);
            } catch (Throwable t) {
                try {
                    Thread.sleep(10_000);
                } catch (Throwable tt) {
                }
            }
        }
    }
    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }
    private void disableCamera() {
        running = false;
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();

    }

}

