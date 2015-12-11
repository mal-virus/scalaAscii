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
import java.awt.image.BufferedImage
import java.awt.Color

object Main extends App {
	val canvas = new CanvasFrame("Asciicam")
 
	//Set Canvas frame to close on exit
	canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

	//Declare FrameGrabber to import output from webcam
	val grabber = new OpenCVFrameGrabber(0)
	grabber.setImageWidth(160)
	grabber.setImageHeight(120)
	grabber.setImageMode(ImageMode.COLOR)
	grabber.start()

	var count = 150
	while (count>0) {
		val frame = grabber.grab()
		val ascii = new AsciiImage(frame.getBufferedImage)
		canvas.showImage(ascii.toImage)
 		count -= 1
	}

	grabber.stop()
	canvas.dispose()

	
	/***************
	*Test resources*
	***************/
	val url = new URL("http://lorempixel.com/125/125/")
	val image1 = ImageIO.read(url)
	val myAscii = new AsciiImage(image1)
	val output = new PrintWriter(new File("output.txt"))
	output.print(myAscii.toString)
	output.close
	
	val file = new File("Koala.jpg")
	val image2 = ImageIO.read(file)
	ImageIO.write(new AsciiImage(image2).toImage, "jpeg", new File("output.jpeg"));
	
	val gif = new File("kermit.gif")
	val asciiGif = new AsciiGif(ImageIO.createImageInputStream(gif))
	
	val gifcanvas = new CanvasFrame("GifCamAS")
	gifcanvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
	for(i<-0 until 6)
	  for(image<-asciiGif.frames)
	    canvas.showImage(image)
	canvas.dispose
	
	asciiGif.toFile(new File("output.gif"))
}