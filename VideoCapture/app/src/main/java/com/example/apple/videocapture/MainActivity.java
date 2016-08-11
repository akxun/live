package com.example.apple.videocapture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import camera.IVideoCapture;
import camera.MCameraManager;

public class MainActivity extends AppCompatActivity implements IVideoCapture, View.OnClickListener {
    private String TAG = MainActivity.class.getName();

    private SurfaceView mSurfaceView;
    private Button startBtn;
    private Button switchBtn;

    private MCameraManager mCameraManager;
    private Boolean isStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(this);
        switchBtn = (Button)findViewById(R.id.switchBtn);
        switchBtn.setOnClickListener(this);
        mCameraManager = new MCameraManager(this, mSurfaceView, this);
        isStart = false;
    }

    @Override
    public void onVideoSize(int width, int height) {
        Log.e(TAG, "onVideoSize() called with: " + "width = [" + width + "], height = [" + height + "]");
    }

    @Override
    public void onVideoFps(int fps) {
        Log.e(TAG, "onVideoFps() called with: " + "fps = [" + fps + "]");
    }

    @Override
    public void onVideoReadly() {
        Log.e(TAG, "onVideoReadly() called with: " + "");
    }

    @Override
    public void onRawVideo(byte[] nv21, int len, int width, int height, int angle, int format) {
        Log.e(TAG, "onRawVideo() called with: " + "nv21 = [" + "], len = [" + len + "], width = [" + width + "], height = [" + height + "], angle = [" + angle + "], format = [" + format + "]");
    }

    @Override
    public void onCameraCanUse(Boolean canUse) {
        Log.e(TAG, "onCameraCanUse() called with: " + "canUse = [" + canUse + "]");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startBtn:
                if (isStart == true) {
                    mCameraManager.setPreviewed(!isStart);
                    isStart = false;
                    startBtn.setText("开始");
                }else {
                    mCameraManager.setPreviewed(!isStart);
                    isStart = true;
                    startBtn.setText("停止");
                }
                break;
            case R.id.switchBtn:
                if (mCameraManager != null) {
                    mCameraManager.cameraSwitch();
                }
                break;
            default:
                break;
        }
    }
}
