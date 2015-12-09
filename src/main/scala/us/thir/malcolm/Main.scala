package us.thir.malcolm

import us.thir.malcolm.ascii._
import javax.imageio.ImageIO
import java.io.{File,PrintWriter}
import java.net.URL

object Main extends App {
	// Test resources
	val file = new File("test.jpg")
	val url = new URL("http://lorempixel.com/100/100/")

	val image = ImageIO.read(file)
	val myScii = new Ascii(image)
	
	val output = new PrintWriter(new File("output.txt"))
	output.print(myScii.toString)
	output.close
}