package us.thir.malcolm

import us.thir.malcolm.ascii._
import javax.imageio.ImageIO
import java.io.{File,PrintWriter}
import java.net.URL

object Main extends App {
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