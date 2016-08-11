package camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import camera.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("NewApi")
@SuppressWarnings({ "deprecation", "unused" })
public class MCameraManager implements Callback, PreviewCallback {


	private Activity activity;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceholder;
	private Camera camera = null;
	private Boolean cameraCanUse = false;
	private IVideoCapture listener;

	private CameraPostDataThread cameraPostDataThread = null;
	
	private int rotateAngle = 0;

	private int fps = 10;
	private byte[] buffer = null;

	private static int cameraPosition = 0;// 0代表前置摄像头，1代表后置摄像头
	private boolean previewed;//
	
	private int width, height;
	private int screenWidth, screenHeight;

	public MCameraManager(Activity activity, SurfaceView surfaceView, IVideoCapture listener) {
		this.listener = listener;
		this.activity = activity;
		cameraPosition = 0;
		surfaceholder = surfaceView.getHolder();
		surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceholder.addCallback(this);
		screenWidth = screenHeight = -1;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		screenWidth = width;
		screenHeight = height;
		openCamera();
		if (!cameraCanUse) {
			return;
		}
		startPreview();
		
		Size size = camera.getParameters().getPreviewSize();

		Log.e("cody", "camera size, width:"+size.width +",height:"+size.height);
		if (listener != null) {
			listener.onVideoSize(size.height, size.width);
			listener.onVideoFps(fps);
			listener.onVideoReadly();
		}
		if (buffer == null)
			buffer = new byte[size.width * size.height * 3 / 2];
	}

	private void openCamera() {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				int cameraNumbers = Camera.getNumberOfCameras();

				CameraInfo info = new CameraInfo();
				for (int i = 0; i < cameraNumbers; i++) {
					Camera.getCameraInfo(i, info);
					if (cameraNumbers >= 2) {//有前置摄像头
						if (info.facing == CameraInfo.CAMERA_FACING_FRONT && cameraPosition == 0) {//这就是前置摄像头，亲。
							camera = Camera.open(i);
//							cameraPosition = 0;
						} else if (info.facing == CameraInfo.CAMERA_FACING_BACK && cameraPosition == 1) {
							camera = Camera.open(i);
						}
					} else if (cameraNumbers == 1) {//只有一个摄像头
						camera = Camera.open(i);
//						cameraPosition = 1;
					} else {//没有摄像头
						Toast.makeText(activity, "您的手机没有摄像头!!!", Toast.LENGTH_LONG).show();
						return;
					}
				}

				cameraCanUse = true;
				if (listener != null) {
					listener.onCameraCanUse(cameraCanUse);
				}
			}
		}catch (Exception e){
			cameraCanUse = false;
			if (listener != null) {
				listener.onCameraCanUse(cameraCanUse);
			}
		}
	}


	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}



	public void cameraSwitch() {
		if (!cameraCanUse) {
			return;
		}
		int cameraNumbers= Camera.getNumberOfCameras();
		if(cameraNumbers<2){//只有一个摄像头
			Toast.makeText(activity, "您的手机只有一个摄像头或者没有摄像头!!!", Toast.LENGTH_LONG).show();
			return;
		}
		
		surfaceDestroyed(null);// 释放摄像头信息

		//切换前后摄像头
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i,cameraInfo);// 得到每一个摄像头的信息
			if (cameraPosition == 1) {// 现在是后置，变更为前置
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
					camera = Camera.open(i);// 打开当前选中的摄像头
					startPreview();
					cameraPosition = 0;
					break;
				}
			} else {// 现在是前置,变更为后置
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_BACK后置
					camera = Camera.open(i);// 打开当前选中的摄像头
					startPreview();
					cameraPosition = 1;
					break;
				}
			}
		}
	}

	private void startPreview() {
		if(cameraPostDataThread != null) {
			cameraPostDataThread.detachThread();
			cameraPostDataThread = null;
		}
//		cameraPostDataThread = new CameraPostDataThread();
//		cameraPostDataThread.start();

		try {
			// 设置预览监听
			camera.setPreviewDisplay(surfaceholder);
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPictureFormat(ImageFormat.JPEG);
			parameters.set("jpeg-quality",100);//照片质量
			int defaultPreviewFormat = parameters.getPreviewFormat();
			if ((defaultPreviewFormat != ImageFormat.YV12)&& (defaultPreviewFormat != ImageFormat.NV21)) {
				parameters.setPreviewFormat(ImageFormat.NV21);
			}

//			if (isSupportFlash(parameters, Camera.Parameters.FLASH_MODE_OFF)) {
//				parameters.setFocusMode(Camera.Parameters.FLASH_MODE_OFF);
//			}

			if (isSupportSceneMode(parameters, Camera.Parameters.SCENE_MODE_AUTO)) {
				parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
			}

			if (isSupportWhiteBalance(parameters, Camera.Parameters.WHITE_BALANCE_AUTO)) {
				parameters.setSceneMode(Camera.Parameters.WHITE_BALANCE_AUTO);
			}

			if (activity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				parameters.set("orientation", "portrait");
				camera.setDisplayOrientation(90);
				parameters.setRotation(90);
			} else {
				parameters.set("orientation", "landscape");
				camera.setDisplayOrientation(0);
				parameters.setRotation(0);
			}
			/* 获取摄像头支持的PreviewSize列表 */
			List<Size> previewSizeList = parameters.getSupportedPreviewSizes();
			
//			DisplayMetrics dm = new DisplayMetrics();
//			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
//			int screenWidth = dm.widthPixels;
//			int screenHeight = dm.heightPixels;
			
			System.out.println("screen width:"+screenWidth+",height:"+screenHeight);
			Size preSize = Utils.getProperSize(previewSizeList,((float) screenHeight) / screenWidth);

			if (null != preSize) {
				System.out.println("设置摄像头宽度:"+preSize.width+",设置摄像头高度:"+preSize.height);
				parameters.setPreviewSize(preSize.width, preSize.height);
			}
			width = preSize.width;
			height = preSize.height;

//			if(buffer == null ) {		
				buffer = new byte[preSize.width*preSize.height*3/2]; 
//			}

			int[] range = new int[2];
			parameters.getPreviewFpsRange(range);
			System.out.println("range[0]:" + range[0] + "  range[1]:" + range[1]);
//			if (range[0] == range[1]) {
//				fps = range[1] / 1000;
//			} else if (range[1] > 24) {
//				fps = 24;
//			} else {
//				fps = range[1] / 1000;
//			}
			fps = 24;
			int[] range1 = findClosestFpsRange(24, parameters.getSupportedPreviewFpsRange());
			parameters.setPreviewFpsRange(range1[0], range1[1]);
//			parameters.setPreviewFrameRate(fps);
			initFocusMode(parameters);

			camera.setParameters(parameters);
			
			Size size = camera.getParameters().getPreviewSize();
			System.out.println("camera: width:"+size.width+",height:"+size.height);
			
			//camera.addCallbackBuffer(buffer);
			//camera.setPreviewCallbackWithBuffer(this);
			camera.setPreviewCallback(this);
			// 启动摄像头预览
			camera.startPreview();
			System.out.println("camera.startpreview");
		} catch (IOException e) {
			e.printStackTrace();
			camera.release();
			System.out.println("camera.release");
		}
	}


	public void initFocusMode(Camera.Parameters parameters) {
		List<String> supportedFocusModes = parameters.getSupportedFocusModes();
		String focusMode = findSettableValue(supportedFocusModes, Camera.Parameters.FOCUS_MODE_AUTO);
		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}
//		String focusMode = findSettableValue(supportedFocusModes,Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//		if(focusMode==null)
//			focusMode = findSettableValue(supportedFocusModes,Camera.Parameters.FOCUS_MODE_AUTO);
//		if(focusMode==null)
//			findSettableValue(supportedFocusModes,Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//		if (null == focusMode) {
//			focusMode = findSettableValue(parameters.getSupportedFocusModes(),Camera.Parameters.FOCUS_MODE_MACRO,
//					Camera.Parameters.FOCUS_MODE_EDOF);
//		}
//		if (null != focusMode) {
//			parameters.setFocusMode(focusMode);
//		} else {
//			parameters.setFocusMode(supportedFocusModes.get(0));
//		}
	}
	
	private String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.e("camera", "surfaceDestroyed");
		if (camera != null) {
			camera.setPreviewCallback(null);
//			camera.setPreviewCallbackWithBuffer(null);
			camera.stopPreview();

			camera.release();
			camera = null;
		}
		if(cameraPostDataThread != null) {
			cameraPostDataThread.detachThread();
			cameraPostDataThread = null;
		}

	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera){
		Log.e("xx", "onPreviewFrame");
		if(previewed){
			if (cameraPosition == 0) {
				rotateAngle = 270;
			} else if (cameraPosition == 1) {
				rotateAngle = 90;
			}
//			if(cameraPostDataThread != null) {
//				cameraPostDataThread.addCameraData(data);
//			}
			listener.onRawVideo(data, data.length, width, height, rotateAngle, 0);
		}
		//camera.addCallbackBuffer(data);
	}
	
	public void setPreviewed(boolean previewed) {
		this.previewed = previewed;
	}

	private static int[] findClosestFpsRange(int expectedFps, List<int[]> fpsRanges) {
		expectedFps *= 1000;
		int[] closestRange = fpsRanges.get(0);
		int measure = Math.abs(closestRange[0] - expectedFps) + Math.abs(closestRange[1] - expectedFps);
		for (int[] range : fpsRanges) {
			if (range[0] <= expectedFps && range[1] >= expectedFps) {
				int curMeasure = Math.abs(range[0] - expectedFps) + Math.abs(range[1] - expectedFps);
				if (curMeasure < measure) {
					closestRange = range;
					measure = curMeasure;
				}
			}
		}
		return closestRange;
	}


	//	params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//	params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
//	params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

	private Boolean isSupportFlash(Camera.Parameters parameters, String type) {
		List<String> flash = parameters.getSupportedFlashModes();
		String off = findSettableValue(flash, type);
		if (off != null) {
			return true;
		} else {
			return false;
		}
	}

	private Boolean isSupportWhiteBalance(Camera.Parameters parameters, String type) {
		List<String> whiteBalance = parameters.getSupportedWhiteBalance();
		String auto = findSettableValue(whiteBalance, type);
		if (auto != null) {
			return  true;
		} else {
			return false;
		}
	}

	private Boolean isSupportSceneMode(Camera.Parameters parameters, String type) {
		List<String> scene = parameters.getSupportedSceneModes();
		String t = findSettableValue(scene, type);
		if (t != null) {
			return true;
		} else {
			return false;
		}
	}





	private class CameraPostDataThread extends Thread {
		private List<ByteBuffer> cacheList ;
		private Boolean threadQuit = false;
		private Boolean hasData = false;

		public CameraPostDataThread() {
			cacheList = new LinkedList<>();
			threadQuit = false;
			hasData = false;
		}

		public void addCameraData(ByteBuffer buffer) {
			if (cacheList != null) {
				synchronized (cacheList) {
					cacheList.add(buffer);
					hasData = true;
				}
			}
		}

		public void addCameraData(byte[] data) {
			if(cacheList != null) {
				synchronized (cacheList) {
					cacheList.add(ByteBuffer.allocate(data.length).put(data));
					hasData = true;
				}
			}
		}

		public void detachThread() {
			threadQuit = true;
		}




		@Override
		public void run() {
			while(true) {
				Thread.currentThread().setName("CameraPostDataThread");
				if (threadQuit) {
					break;
				}
				if (hasData) {
					synchronized (cacheList) {
						if (cacheList.size() > 0) {
							ByteBuffer b = cacheList.remove(0);
							if (listener != null) {
//
								if (cameraPosition == 0) {
									rotateAngle = 270;
								} else if (cameraPosition == 1) {
									rotateAngle = 90;
								}
								listener.onRawVideo(b.array(), b.array().length, width, height, rotateAngle, 0);
								b = null;
								hasData = false;
							}
						}
					}

				}
			}

		}
	}

}
