package us.thir.malcolm.ascii

import java.awt.Color
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage

class PixelBlock private(grays: Int*) {
	private var mean = (grays.sum / grays.size).toInt
	def invert() = mean = 256 - mean
	private val characters = Array(
	    "$","@","B","%","8","&","W","M","#","0","*","o","a","h","h","b","d",
	    "p","q","w","m","Z","O","Q","L","C","J","U","Y","X","z","c","v","u",
	    "n","x","r","j","f","t","/","\\","|","(",")","1","{","}","[","]","?",
	    "-","_","+","~","<",">","i","!","l","I",";",":",",","\"","^","`","'",
	    "."," ")
	private val step = (256d/characters.length)
	
	lazy val toAscii = characters((mean/step).toInt.min(characters.length-1))
}

object PixelBlock {
	def apply(grays: Int*) = {
		// If the number of arguments is not a perfect square, complain
		val side = Math.sqrt(grays.length).toInt
		if(side*side != grays.length)
			throw new IllegalArgumentException("Number of pixels given to a block MUST be a perfect square")

		new PixelBlock(grays:_*)
	}
	
	def fromRgb(r: Int, g: Int, b: Int) = (r*0.21+g*0.72+b*0.07).toInt
}

class Ascii(originalImage: BufferedImage, pixelSquareLength: Int = 1) {
	if(pixelSquareLength<1) throw new IllegalArgumentException("You can't have a pixel block smaller than one pixel")
	private val w = originalImage.getWidth
	private val h = originalImage.getHeight

	
	var lines = Seq[String]()
	private var sequence = ""
	for(y<-0 until h) {
		if(sequence.length!=0) 
		  lines = lines :+ sequence
		sequence = ""
		for(x<-0 until w) {
			val color = new Color(originalImage.getRGB(x,y))
			val pixel = PixelBlock(PixelBlock.fromRgb(color.getRed,color.getGreen,color.getBlue))
			pixel.invert
			sequence = sequence + pixel.toAscii + " "
		}
	}
	def toLines = lines
	override def toString = lines.foldLeft("")((z,x) => z.concat("\n").concat(x))
	def toImage = {
		val font = new Font("Courier", Font.PLAIN, 10)
		val frc = new FontRenderContext(null, true, true)
		val bounds = font.getStringBounds(lines(0), frc)
		val w = bounds.getWidth.toInt
		val lineH = bounds.getHeight.toInt
		val h = lineH*lines.size

		val image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB)
		val graphic = image.createGraphics
 
		graphic setColor Color.BLACK
		graphic.fillRect(0, 0, w, h)
		graphic setColor Color.WHITE
		graphic setFont font
		val rootPixel = bounds.getY.toFloat
		var lineNumber = 0
		for(line<-lines) {
			graphic.drawString(line, bounds.getX.toFloat, rootPixel+lineNumber*lineH)
			lineNumber += 1
 		}
		graphic.dispose()
		image
	}
}