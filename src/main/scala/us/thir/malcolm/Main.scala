package us.thir.malcolm

import java.io.File
import java.net.URL

import org.bytedeco.javacv.{CanvasFrame,OpenCVFrameGrabber}
import org.bytedeco.javacv.FrameGrabber.ImageMode

import javax.imageio.ImageIO
import us.thir.malcolm.ascii._

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
	ImageIO.write(image1, "jpeg", new File("input1.jpeg"))
	ImageIO.write(new AsciiImage(image1).toImage, "jpeg", new File("output1.jpeg"));
	
	val file = new File("Koala.jpg")
	val image2 = ImageIO.read(file)
	ImageIO.write(new AsciiImage(image2).toImage, "jpeg", new File("output2.jpeg"));
	
	val gif = new File("kermit.gif")
	val asciiGif = new AsciiGif(ImageIO.createImageInputStream(gif))
	asciiGif.toFile(new File("output3.gif"))
}