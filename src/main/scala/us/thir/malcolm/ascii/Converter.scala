package us.thir.malcolm.ascii

import java.awt.Color
import java.awt.image.BufferedImage

class PixelBlock private(grays: Int*) {
	private var mean = (grays.sum / grays.size).toInt
	def invert = mean = 256 - mean
	def toAscii = mean match {
		case x if 0 until 50 contains x => "@"
		case x if 50 until 70 contains x => "#"
		case x if 70 until 100 contains x => "8"
		case x if 100 until 130 contains x => "&"
		case x if 130 until 160 contains x => "o"
		case x if 160 until 180 contains x => ":"
		case x if 180 until 200 contains x => "*"
		case x if 200 until 230 contains x => "."
		case _ => " " 
	}
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