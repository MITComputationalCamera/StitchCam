// (CompCam): HW ASSIGNMENT : Design a function to control the capture of 2 images to stit SEE "Features2D + Homography to find a known object" tutorial of openCV as reference 
// (  http://docs.opencv.org/doc/tutorials/features2d/feature_homography/feature_homography.html#feature-homography  )
//You should only need to modify the code/methods under the blocks shown with "ADD CODE HERE" (specifically "YOUR CODE")


package com.example.stitchCamera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import com.example.cameraexample.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CameraActivity extends Activity implements OnClickListener, PictureCallback {
	

	private static final String  TAG = "OpenCV_img_blur_ex";

	private CameraSurfaceView cameraSurfaceView;
	private Button shutterButton;
	
	private Mat imgOne; 
	private Mat imgTwo;
	
	private Bitmap imgBitmap;
	
	private int state = 0;
	
	private CameraActivity  act = this;
	
	
	
	//(CompCam): matching and homography vals
	 private FeatureDetector detector;
	 private MatOfKeyPoint oneKeypoints;
	 private DescriptorExtractor descriptor;
	 private Mat oneDescriptors;
	 
	 //frames
	 private MatOfKeyPoint twoKeypoints;
	 private Mat twoDescriptors;
	 
	 //mathcer
	 private DescriptorMatcher matcher;	 
	 private MatOfDMatch imgMatches;
	 
	 //matches points;
	 private ArrayList<Point> onePointList;
	 private ArrayList<Point> twoPointList;
	 
	 
	 private Mat oneCorners;
	 private Mat twoCorners;
	 
	 private Mat H; 
	
	private Point corner1, corner2;
	private Point pt;
	
	private MatOfPoint2f pointsOne;
	private MatOfPoint2f pointsTwo;
	
	private Mat finalImg;
	
	private Mat grayOne;
	private Mat grayTwo;
	
	//extras
	private Mat matchingImg;
	
	
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	
		public void onManagerConnected(int status) {
          switch (status) {
              case LoaderCallbackInterface.SUCCESS:
              {
                  Log.i(TAG, "OpenCV loaded successfully");
                  

                  try {                	  
                	  
             
              		
              		
              		//(CompCam): object creations 
                    imgOne = new Mat();
                    imgTwo = new Mat();                    
                    
                    //(CompCam): for the feature detection/extraction/matching
                    detector = FeatureDetector.create(FeatureDetector.ORB);
                 	descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);   
                 	matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
                 	
                 	imgMatches = new MatOfDMatch();
                    
                    onePointList = new ArrayList<Point>();
                    twoPointList = new ArrayList<Point>();
                    
                    oneDescriptors = new Mat();    
                 	twoDescriptors = new Mat();	             	

                 	oneKeypoints = new MatOfKeyPoint();     	
                 	twoKeypoints = new MatOfKeyPoint();     
                 	
                 	oneCorners = new Mat(4,1,CvType.CV_32FC2);
               	 	twoCorners = new Mat(4,1,CvType.CV_32FC2);
               	 	
		           	pointsOne = new MatOfPoint2f();
		     		pointsTwo = new MatOfPoint2f();
               	 	
		     		
            		H = new Mat();		
            		
            		finalImg = new Mat();
            		
            		matchingImg = new Mat();
                 	     
              	
				} catch (Exception e) {
					e.printStackTrace();
				}
                  
                  
              } break;
              default:
              {
                  super.onManagerConnected(status);
              } break;
          }
      }
  };
  
  public void onResume()
  {
      super.onResume();
      OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
  }
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
	   	// set up our preview surface
  		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
  		cameraSurfaceView = new CameraSurfaceView(act);
  		preview.addView(cameraSurfaceView);
   
  		// grab out shutter button so we can reference it later
  		shutterButton = (Button) findViewById(R.id.shutter_button);
  		shutterButton.setOnClickListener(act);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_camera, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
					
		takePicture();
		
	}

	private void takePicture() {
		shutterButton.setEnabled(false);
		cameraSurfaceView.takePicture(this);
	}

	 protected File getPhotoPath(String addName) {
		 
		    File dir=getPhotoDirectory();
		    dir.mkdirs();

		    return(new File(dir, getPhotoFilename(addName)));
	 }
	 
	  protected File getPhotoDirectory() {
	    return(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
	  }

	  protected String getPhotoFilename(String addName) {
	    String ts=
	        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

	    return(addName + "_" + ts + ".png");
	  }
	
	
	
	public void onPictureTaken(byte[] data, Camera camera) {

		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		
		if(state == 0){
		
			Utils.bitmapToMat(bmp, imgOne);		
			
			//(CompCam): detect and extract features at the image
			getFirstFeatures();
			
			state = 1;
			
		}		
		else if(state == 1){
			
			Utils.bitmapToMat(bmp, imgTwo);
			
			//(CompCam): detect and extract features at the image
			getSecondFeatures();
			
			//(CompCam): match features and process it to get a better result 
			macthFeatures();
			
			imgBitmap = Bitmap.createBitmap(matchingImg.width(), matchingImg.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(matchingImg, imgBitmap);
			
			matchingImg.release();
			grayOne.release();
			grayTwo.release();
			
			File photo=getPhotoPath("Feature_match");			
		    if (photo.exists()) {
		      photo.delete();
		    }			

			try {
		       FileOutputStream out = new FileOutputStream(photo.getPath());
		       Log.d("debugHDR", "img  saving");
		       imgBitmap.compress(Bitmap.CompressFormat.PNG, 90, out); //if you find that the image is poor in quality, adjust the "90" here to a different compression amount.
		       Log.d("debugHDR", "img  saved");
		       out.close();
			} 
			catch (Exception e) {
		       e.printStackTrace();
			}
			
			imgBitmap = null;
			
			//(CompCam): here the functions calculate the homography between images
			calcHomography();					
			
			//(CompCam): here the first image is warped to the second image plane and stitched togeter 
			stitchProcessing();		
			
			
			imgBitmap = Bitmap.createBitmap(finalImg.width(), finalImg.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(finalImg, imgBitmap); 		
			
			photo=getPhotoPath("Warped");
			
		    if (photo.exists()) {
		      photo.delete();
		    }			

			try {
		       FileOutputStream out = new FileOutputStream(photo.getPath());
		       Log.d("debugHDR", "img one saving");
		       imgBitmap.compress(Bitmap.CompressFormat.PNG, 90, out); //if you find that the image is poor in quality, adjust the "90" here to a different compression amount.
		       Log.d("debugHDR", "img one saved");
		       out.close();
			} 
			catch (Exception e) {
		       e.printStackTrace();
			}
			
			
			imgBitmap = Bitmap.createBitmap(imgOne.width(), imgOne.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(imgOne, imgBitmap); 	
			photo=getPhotoPath("ImageOne_Original");
			
		    if (photo.exists()) {
		      photo.delete();
		    }			

			try {
		       FileOutputStream out = new FileOutputStream(photo.getPath());
		       Log.d("debugHDR", "img one saving");
		       imgBitmap.compress(Bitmap.CompressFormat.PNG, 90, out); //if you find that the image is poor in quality, adjust the "90" here to a different compression amount.
		       Log.d("debugHDR", "img one saved");
		       out.close();
			} 
			catch (Exception e) {
		       e.printStackTrace();
			}
			
			imgBitmap = Bitmap.createBitmap(imgTwo.width(), imgTwo.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(imgTwo, imgBitmap); 		
			
			photo=getPhotoPath("ImageTwo_with_WarpedImageOne");
			
			
		    if (photo.exists()) {
		      photo.delete();
		    }			

			try {
		       FileOutputStream out = new FileOutputStream(photo.getPath());
		       Log.d("debugHDR", "img saving");
		       imgBitmap.compress(Bitmap.CompressFormat.PNG, 90, out); //if you find that the image is poor in quality, adjust the "90" here to a different compression amount.
		       Log.d("debugHDR", "img saved");
		       out.close();
			} 
			catch (Exception e) {
		       e.printStackTrace();
			}
			
			
			state = 0;
			
			 clearData();	
		}
	
		
		// Restart the preview and re-enable the shutter button so that we can take another picture
		camera.startPreview();
		shutterButton.setEnabled(true);
	}
	
	
	
	//(CompCam): ADD CODE HERE : This method will detect and extract the features of first image. 
	private void getFirstFeatures(){
		
		
		//(CompCam): first convert the image (imgOne) to a gray image to compute the features (Imgproc, Color_RGB2Gray)
		//          (additionally you can resize the image too be smaller to get better performance and decrease memory usage, 
		// 			use resize function or better, the "Imgproc.pyrDown" function. This function create a image half of the size of the original)		
		
		
					///YOUR CODE
     	
		//(CompCam): Use the "FeatureDetector" ("detector" object we already defined above) method "detect" to detect features of the image (use oneKeyPoints object defined above)
  	
    				///YOUR CODE


     	//(CompCam):  use "DescriptorExtractor" ("descriptor" object we already defined above) method "compute" to extract the features descriptions (use oneKeyPoints and oneDescriptors objects defined above)
     
     				//YOUR CODE

     	
     	
     	
     	
     	//ParamCode - Leave the code below as is.
		float[] valUL = {0,0}; oneCorners.put(0, 0, valUL); 
		float[] valUR = {imgOne.width(),0};oneCorners.put(1, 0, valUR);
		float[] valDR = {imgOne.width(),imgOne.height()};oneCorners.put(2, 0, valDR);
		float[] valDL = {0,imgOne.height()};oneCorners.put(3, 0, valDL);		
     			
	}
	
	//(CompCam): ADD CODE HERE : This method detect and extract the features of second image, exactly the same as the first image (but do not include the "ParamCode" block commented above). Also you'll be using twoKeyPoints and twoDescriptors, instead.
	private void getSecondFeatures(){
		
		
		//YOUR CODE HERE
     			
	}

	//(CompCam): ADD CODE HERE : This method will do the matching between the two images
	private void macthFeatures(){
		
		//(CompCam): do the matching using "match" function of "DescriptorMatcher" ("matcher" object here) using the oneDescriptors, twoDescriptors, and imgMatches objects defined above
		
				//YOUR CODE HERE
		
		
		//(CompCam): Clear our ArrayLists out (leave code below as is)
		twoPointList.clear();
		onePointList.clear();
		
		//(CompCam): Here you will filter the matched points to get only the good matches. Let's define some variables first (leave code below as is).
		
		DMatch[] matches = imgMatches.toArray();
		
		KeyPoint[] twoKeyArray = twoKeypoints.toArray();
		KeyPoint[] oneKeyArray = oneKeypoints.toArray();		
	
		float maxVal = Float.MIN_VALUE;
		float minVal = Float.MAX_VALUE;
		
		
		//(CompCam): Here you will iterate through your matches array to find the min and max values (using the maxVal and minVal above)

				//YOUR CODE HERE
		
		//(CompCam): Now we're going to get the keypoints from the good matches.
		//First, iterating through your matches array again, check if the matches distance is less than or equal too 1.5*minVal (this is our threshold)
		//if it is, you'll need to add that match point's queryIdx and trainIdx into onePointList and twoPointList, respectively. Use your oneKeyArray and twoKeyArray here too.
		//If you're confused, you will want to go back to the tutorial link described at the top of this file

				//YOUR CODE HERE
		
		//(CompCam): Next, use "Features2d.drawMatches" function to get a image with the matched features and save it as an image for debuggin (this is the colorful lines image you've seen in the explanation).
		//This image will automatically add the both images in one big image and draw the features matched. 
		
			    //YOUR CODE HERE
		
	}
	
	//(CompCam): ADD CODE HERE : calculate the homography between images
	private void calcHomography(){

		
		//(CompCam): convert the pointLists above (one and two) to "MatOfPoint2f" using  the "fromList" function of "MatOfPoint2f" (since you processed the points as ab ArrayList<Points>). 
				//YOUR CODE HERE
		
		//(CompCam): use function "Calib3d.findHomography" function to calculate the homography matrix. (use "Calib3d.RANSAC", as parameter and 3.0f) 
				
				//YOUR CODE HERE
		
		//(CompCam): Next, use the function "Core.perspectiveTransform" to detect where the 4 corners to the first images goes over the second image (use oneCorners and twoCorners)
				
				//YOUR CODE HERE	
		
	}
	
	
	//(CompCam): The code below warps the first image and stitches both images. Using "Imgproc.warpPerspective" and  the homography matrix H as parameter to warp the image. (leave the below code as is)

	private void stitchProcessing()
	{
		
		Imgproc.warpPerspective(imgOne, finalImg, H, new Size(imgOne.width(), imgOne.height()), Imgproc.INTER_CUBIC);
		
		Core.line( imgTwo, new Point(twoCorners.get(0, 0)[0], twoCorners.get(0, 0)[1]), new Point(twoCorners.get(1, 0)[0], twoCorners.get(1, 0)[1]),  new Scalar(255, 0, 0, 255), 4 );
		Core.line( imgTwo, new Point(twoCorners.get(1, 0)[0], twoCorners.get(1, 0)[1]), new Point(twoCorners.get(2, 0)[0], twoCorners.get(2, 0)[1]), new Scalar( 255, 0, 0, 255), 4 );
		Core.line( imgTwo, new Point(twoCorners.get(2, 0)[0], twoCorners.get(2, 0)[1]), new Point(twoCorners.get(3, 0)[0], twoCorners.get(3, 0)[1]), new Scalar( 255, 0, 0, 255), 4 );
		Core.line( imgTwo, new Point(twoCorners.get(3, 0)[0], twoCorners.get(3, 0)[1]), new Point(twoCorners.get(0, 0)[0], twoCorners.get(0, 0)[1]),new Scalar( 255, 0, 0, 255), 4 );
				
	}
	
	
	private void clearData(){
		imgOne.release();
		imgOne = new Mat();
		imgTwo.release();	
		imgTwo = new Mat();	
	}
	
}
