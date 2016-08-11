package camera;



public interface IVideoCapture {
	public void onVideoSize(int width, int height);
	
	public void onVideoFps(int fps);
	
	public void onVideoReadly();
	
	public void onRawVideo(byte[] nv21, int len, int width, int height, int angle, int format);

	public void onCameraCanUse(Boolean canUse);
}
