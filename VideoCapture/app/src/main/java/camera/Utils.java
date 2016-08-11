package camera;

import android.hardware.Camera.Size;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {
	public static Size getProperSize(List<Size> sizeList, float displayRatio) {
		// 先对传进来的size列表进行排序
		Collections.sort(sizeList,new SizeComparator());
		displayRatio =  (float)(Math.round(displayRatio*100))/100;
		
		float differ=0;
		Size resultSize=null;
		for(int i=sizeList.size()-1;i>=0;i--){
			Size size=sizeList.get(i);
			if(size.width<640&&size.height<368){
				sizeList.remove(i);
			}else{
				float newDiffer=getRatio(size.width,size.height) - displayRatio;//当前比例-屏幕显示比例
				if (newDiffer == 0){
					return size;
				}else{
					System.out.println("绝对值:"+ Math.abs(newDiffer)+" 宽度:"+size.width+" 高度:"+size.height);
					if((newDiffer!=differ)&&(resultSize==null|| Math.abs(newDiffer)< Math.abs(differ))){//第一次进入
						resultSize=size;
						differ=newDiffer;
					}
				}
			}
		}
		return resultSize;
	}
	
	public static float getRatio(int width,int height){
		float curRatio = ((float) width) / height;
		return (float)(Math.round(curRatio*100))/100;
	}

	static class SizeComparator implements Comparator<Size> {
		@Override
		public int compare(Size lhs, Size rhs) {
			// TODO Auto-generated method stub
			Size size1 = lhs;
			Size size2 = rhs;
			if (size1.width < size2.width || size1.width == size2.width && size1.height < size2.height) {
				return 1;
			} else if (!(size1.width == size2.width && size1.height == size2.height)) {
				return -1;
			}
			return 0;
		}
	}
}
