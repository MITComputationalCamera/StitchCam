StitchCam (HW5)
=========

Uses OpenCV to find where one image may be aligned in another image (and shows what that border/warp looks like)


You will create an app that, upon taking two overlapping images, figures out where the first image overlaps the second using Features2D/Homographies. This technique is used, in part, by
panorama apps to identify features between two overlapping images that are the same, allowing the device to combine and stitch together the image intelligently.


As always, you will be adding code into a ready-made skeleton above. This involves OpenCV so keep in mind you will need to make sure all your settings are correct and that the libary has benen added correctly (Right Click Project>Properties>Android>Add the OpenCV Library Properly)

# Desired Output #
When you've completed the app, you will run it, take 2 pictures (with overlapping images and common features), and examine the output in your phone's DCIM folder. Again, you should be using a file explorer to do this so you can see the files as soon as they are created and also see the filenames.

Once the images have saved (the take picture button should re-enable after the 2nd time you press it), you should get 4 output images similar to the ones below:

1. First Image you took ("ImageOne_Original"): [Link](http://i.imgur.com/lpODinZ)
2. First and Second Image you took with Feature Matching algorithm ("Feature_match"): [Link](http://i.imgur.com/h9eYPs0)
3. First Image Warped as if it were to be matched up on the second image ("Warped"): [Link](http://i.imgur.com/I7wN7tm)
4. Second Image containing the Warped First Image's red outline ("ImageTwo_with_WarpedImageOne"): [Link](http://i.imgur.com/X7mHHGn)



# Operating the App #
1. Open the App, turn the phone sideways (landscape works better for some reason)
2. Click Take Picture
3. Move the camera to an other part of the scene where the 2 images overlap in both frame and contain similar features
4. Click Take Picture again
5. (wait)
6. Rejoice! The photos should now be saved in your DCIM folder


# Troubleshooting (known problems) #
* App crashing after taking second image - If the difference between your first image and second image is too extreme, the features are too blurry/dark, or you've shaken the camera too much, the homography will fail and will not compute correctly (this is a simple version of homographies/stitching). Try make smaller changes and changing your scene to something well lit and wiht hard edges.
* App is full of errors at start - Make sure you've imported your OpenCV Library properly! Right Click Project>Properties>Android>Add the OpenCV Library at the bottom right and remove the faulty one)
* The read outline is just a dot/tiny line - This means the matching algorithm likely failed. Try a brighter scene with more distinct features (sharp edges/bold corners)


# Submission #
MIT/Skolkovo Students - Please submit your 4 image file output for a scene of your choosing (do something interesting!) and your compiled .apk file.
Online Students - Please submit your image files on the online Facebook Page.
