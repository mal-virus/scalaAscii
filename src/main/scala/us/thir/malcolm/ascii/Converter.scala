package us.thir.malcolm.ascii

import java.awt.Color
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream
import com.sun.imageio.plugins.gif.GIFImageReader
import com.sun.imageio.plugins.gif.GIFImageReaderSpi
import javax.imageio.ImageTypeSpecifier
import javax.imageio.IIOImage
import javax.imageio.metadata.IIOMetadataNode
import org.w3c.dom.Node
import javax.imageio.metadata.IIOMetadata
import javax.imageio.stream.FileImageOutputStream

class PixelBlock private(grays: Int*) {
	private var mean = (grays.sum / grays.size).toInt
	def invert() = mean = 256 - mean
	private val characters = Array(
	    " ",".","'","`","^","\"",",",":",";",
	    "I","l","!","i","<","~","+","_","-",
	    "?","[","{","1","(","|","/","t","f",
	    "j","r","x","n","u","v","c","z","X",
	    "Y","U","J","C","L","Q","O","Z","m",
	    "w","p","b","h","a","o","*","0","#",
	    "M","W","&","8","%","b","@","$")
	private val conversion = (256d/characters.length)
	
	lazy val toAscii = characters((mean/conversion).toInt.min(characters.length-1))
}

object PixelBlock {
	def apply(grays: Int*) = {
		// If the number of arguments is not a perfect square, complain
		val side = Math.sqrt(grays.length).toInt
		if(side*side != grays.length)
			throw new IllegalArgumentException("Number of pixels given to a block MUST be a perfect square")

		new PixelBlock(grays:_*)
	}
}

class AsciiImage(originalImage: BufferedImage, invert: Boolean = false, pixelSquareLength: Int = 1) extends Ascii {
	if(pixelSquareLength<1) throw new IllegalArgumentException("You can't have a pixel block smaller than one pixel")
	val lines = buffIm2SeqStr(originalImage, invert)
	override def toString = lines.foldLeft("")((z,x) => z.concat(x).concat("\n"))
	lazy val toImage = seqStr2buffIm(lines)
}

class AsciiGif(originalImageStream: ImageInputStream, invert: Boolean = false) extends Ascii {
  private val reader = new GIFImageReader(new GIFImageReaderSpi())
  reader.setInput(originalImageStream)
  var lines = Seq[Seq[String]]()
  
  for(i<-0 until reader.getNumImages(true)) {
    lines = lines :+ buffIm2SeqStr(reader.read(i),invert)
  }
  reader.dispose()
  
  def length = lines.length
  
  def apply(index: Int) = seqStr2buffIm(lines(index))
  
  def toFile(file: File, delay: Int=5) = {
    if(delay<0)
			throw new IllegalArgumentException("Delay must be positive")
    
    def getNodeByName(root: Node, nodeName: String): Node = {
      var holder = root.getFirstChild
      while(holder!=null) {
        if(nodeName.equals(holder.getNodeName))
            return holder
        holder = holder.getNextSibling
      }
      val createdNode = new IIOMetadataNode(nodeName)
      root.appendChild(createdNode)
      return createdNode
    }
    
    
    val writer = ImageIO.getImageWritersByFormatName("gif").next()
    val stream = new FileImageOutputStream(file)
    val imageParam = writer.getDefaultWriteParam
    
    
    val imageMeta = writer.getDefaultImageMetadata(
          ImageTypeSpecifier.createFromBufferedImageType(seqStr2buffIm(lines(0)).getType),
          imageParam)
    
          
    // We need to create the app extension code
    val root = imageMeta.getAsTree(imageMeta.getNativeMetadataFormatName)
    val graphics =  getNodeByName(root, "GraphicControlExtension").asInstanceOf[IIOMetadataNode]
    graphics.setAttribute("userInputFlag", "FALSE")
    graphics.setAttribute("delayTime", delay.toString())
    graphics.setAttribute("disposalMethod", "none")
    
    val aes = getNodeByName(root,"ApplicationExtensions")
    val ae = new IIOMetadataNode("ApplicationExtension")
    ae.setAttribute("applicationID", "NETSCAPE")
    ae.setAttribute("authenticationCode", "2.0")
    val userObject: Array[Byte] = Array(1.toByte,0.toByte,0.toByte)
    ae.setUserObject(userObject)
    aes.appendChild(ae)
    
    imageMeta.setFromTree(imageMeta.getNativeMetadataFormatName, root)
    writer.setOutput(stream)
    writer.prepareWriteSequence(null)
    
    for(frame<-lines)
      writer.writeToSequence(new IIOImage(seqStr2buffIm(frame),null,imageMeta), imageParam)
    
    writer.endWriteSequence()
    stream.close()
    
    file
  }
}

trait Ascii {
  protected val font = new Font("Courier", Font.PLAIN, 10)
  protected val frc = new FontRenderContext(null, true, true)
  protected def grayFromRgb(r: Int, g: Int, b: Int) = (r*0.21+g*0.72+b*0.07).toInt
  protected def buffIm2SeqStr(original: BufferedImage, invert: Boolean = false) = {
  val w = original.getWidth
	val h = original.getHeight
    var lines = Seq[String]()
	  var sequence = ""
	  for(y<-0 until h) {
		  if(sequence.length!=0) 
		    lines = lines :+ sequence
	    sequence = ""
	  	for(x<-0 until w) {
		  	val color = new Color(original.getRGB(x,y))
		  	val pixel = PixelBlock(grayFromRgb(color.getRed,color.getGreen,color.getBlue))
		  	if(invert) pixel.invert
			  sequence = sequence + pixel.toAscii + " "
	  	}
	  }
  lines
  }
  protected def seqStr2buffIm(lines: Seq[String]) = {
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