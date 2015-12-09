package us.thir.malcolm

import us.thir.malcolm.ascii._
import javax.imageio.ImageIO
import java.io.{File,PrintWriter}
import java.net.URL
import org.bytedeco.javacpp.helper.opencv_core.AbstractCvScalar
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier
import org.bytedeco.javacpp.{opencv_imgproc, opencv_core}
import org.bytedeco.javacv.FrameGrabber.ImageMode
import org.bytedeco.javacv.{OpenCVFrameGrabber, CanvasFrame}

object Main extends App {
	val canvas = new CanvasFrame("Webcam")
 
//Set Canvas frame to close on exit
canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
 
//Declare FrameGrabber to import output from webcam
val grabber = new OpenCVFrameGrabber(0)
grabber.setImageWidth(148)
grabber.setImageHeight(111)
grabber.setImageMode(ImageMode.COLOR)
grabber.start()
 
while (true) {
 val img = grabber.grab()
 val myScii = new Ascii(img.getBufferedImage)
 print("\r\n" + myScii.toString)
 canvas.showImage(img)
}
  
  // Test resources
	val file = new File("test.jpg")
	val url = new URL("http://lorempixel.com/125/125/")

	val image = ImageIO.read(url)
	val myScii = new Ascii(image)
	
	
	println(myScii.toString)
	/*val output = new PrintWriter(new File("output.txt"))
	output.print(myScii.toString)
	output.close*/
}