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
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.awt.Color

object Main extends App {
val canvas = new CanvasFrame("Webcam")
 
//Set Canvas frame to close on exit
canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
 
//Declare FrameGrabber to import output from webcam
val grabber = new OpenCVFrameGrabber(0)
grabber.setImageWidth(160)
grabber.setImageHeight(120)
grabber.setImageMode(ImageMode.COLOR)
grabber.start()

val font = new Font("Courier", Font.PLAIN, 10);	
val frc = new FontRenderContext(null, true, true)
var count = 200
while (count>0) {
 val frame = grabber.grab()
 
 val ascii = new Ascii(frame.getBufferedImage)
 val asciiLines = ascii.toLines
 val bounds = font.getStringBounds(asciiLines(0), frc)
 
  val w = bounds.getWidth.toInt
 val lineH = bounds.getHeight.toInt
 val h = lineH*asciiLines.size
 
 val image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB)
 
 val graphic = image.createGraphics
 
 graphic setColor Color.BLACK
 graphic.fillRect(0, 0, w, h)
 graphic setColor Color.WHITE
 graphic setFont font
 val rootPixel = bounds.getY.toFloat
 var lineNumber = 0
 for(line<-asciiLines) {
   graphic.drawString(line, bounds.getX.toFloat, rootPixel+lineNumber*lineH)
   lineNumber += 1
 }
 canvas.showImage(image)
 graphic.dispose()
 count -= 1
}

 grabber.stop()
 
 canvas.dispose()

  // Test resources
	/*val file = new File("test.jpg")
	val url = new URL("http://lorempixel.com/125/125/")*/

	//creating the file
  //ImageIO.write(image, "jpeg", new File("output.jpeg"));
	//val image = ImageIO.read(url)
	//val myScii = new Ascii(image)
	
	
	//println(myScii.toString)
	/*val output = new PrintWriter(new File("output.txt"))
	output.print(myScii.toString)
	output.close*/
}