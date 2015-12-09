package us.thir.malcolm.ascii

import java.awt.Color
import java.awt.image.BufferedImage

class PixelBlock private(grays: Int*) {
	private var mean = (grays.sum / grays.size).toInt
	def invert = mean = 256 - mean
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
	val w = originalImage.getWidth
	val h = originalImage.getHeight

	var sequence = ""
	for(y<-0 until h) {
		if(sequence.length!=0) sequence = sequence + "\n"
		for(x<-0 until w) {
			val color = new Color(originalImage.getRGB(x,y))
			val pixel = PixelBlock(PixelBlock.fromRgb(color.getRed,color.getGreen,color.getBlue))
			pixel.invert
			sequence = sequence + pixel.toAscii
		}
	}

	override def toString = sequence
}