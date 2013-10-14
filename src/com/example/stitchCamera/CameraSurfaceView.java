package com.example.stitchCamera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
 
class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private Camera camera;
	
	public Camera getCamera(){return camera;}

	CameraSurfaceView(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		SurfaceHolder holder = this.getHolder();
		holder.addCallback(this);
	} 

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
 

		// The default orientation is landscape, so for a portrait app like this
		// one we need to rotate the view 90 degrees.
		
		
		// IMPORTANT: We must call startPreview() on the camera before we take
		// any pictures
		
		  
	
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		  if (camera == null) {
			  
	            camera = Camera.open();
		   
		        try {
	
		            camera.setPreviewDisplay(holder);
		            
		            camera.setDisplayOrientation(90);
		            
		        	camera.startPreview();

		            // TODO test how much setPreviewCallbackWithBuffer is faster
		           // camera.setPreviewCallback(this);
		        } catch (Exception e) {
		            camera.release();
		            camera = null;
		        }
		    }

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when replaced with a new screen
		// Always make sure to release the Camera instance
		if (camera != null) {
	        camera.stopPreview();
	        camera.setPreviewCallback(null);
	        camera.release();
	        camera = null;
	    }
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
	}

	public void takePicture(PictureCallback imageCallback) {
		camera.takePicture(null, null, imageCallback);
	}

}