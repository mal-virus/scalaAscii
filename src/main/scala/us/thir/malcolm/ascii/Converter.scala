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
}

class AsciiImage(originalImage: BufferedImage, pixelSquareLength: Int = 1) extends Ascii {
	if(pixelSquareLength<1) throw new IllegalArgumentException("You can't have a pixel block smaller than one pixel")
	val lines = buffIm2SeqStr(originalImage)
	override def toString = lines.foldLeft("")((z,x) => z.concat(x).concat("\n"))
	lazy val toImage = seqStr2buffIm(lines)
}

class AsciiGif(originalImageStream: ImageInputStream) extends Ascii {
  private val reader = new GIFImageReader(new GIFImageReaderSpi())
  reader.setInput(originalImageStream)
  val lines = Seq[Seq[String]]()
  for(i<-0 until reader.getNumImages(true)) {
    lines :+ buffIm2SeqStr(reader.read(i))
  }
  
  lazy val frames = lines.map { x => seqStr2buffIm(x) }
  
  def toFile(file: File, delay: Int=1) = {
    if(delay<1)
			throw new IllegalArgumentException("Delay must be greater than 0")
    
    val writer = ImageIO.getImageWritersByFormatName("gif").next()
    val stream = ImageIO.createImageOutputStream(file)
    writer.setOutput(stream)
    writer.prepareWriteSequence(null)
    
    def getGraphicController(root: Node): Node = {
      var holder = root.getFirstChild
      while(holder!=null) {
        if("GraphicControlExtension".equals(holder.getNodeName))
            return holder
        holder = holder.getNextSibling
      }
      null
    }
    
    def setRootAttributes(image: BufferedImage, meta: IIOMetadata) = {
      val format = meta.getNativeMetadataFormatName
      val root = meta.getAsTree(format)
      val aes = new IIOMetadataNode("ApplicationExtensions")
      val ae = new IIOMetadataNode("ApplicationExtension")
      ae.setAttribute("applicationId", "NETSCAPE")
      ae.setAttribute("authenticationCode", "2.0")
      val userObject: Array[Byte] = Array(0x1,0x0,0x0)
      ae.setUserObject(userObject)
      aes.appendChild(ae)
      root.appendChild(aes)
    }
    
    def setAttributes(image: BufferedImage, meta: IIOMetadata) = {
      val format = meta.getNativeMetadataFormatName
      val root = meta.getAsTree(format)
      val controller =  getGraphicController(root).asInstanceOf[IIOMetadataNode]
      controller.setAttribute("userDelay", "FALSE")
      controller.setAttribute("delayTime", delay.toString())
      meta.setFromTree(format, root)
    }
    
    // We need to create the app extension code
    setRootAttributes(
        frames(0),
        writer.getDefaultImageMetadata(new ImageTypeSpecifier(frames(0)), writer.getDefaultWriteParam)
        )
    
    for(image<-frames) {
      val meta = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), writer.getDefaultWriteParam)
      setAttributes(image,meta)
      writer.writeToSequence(new IIOImage(image,null,meta), null)
    }
    
    writer.endWriteSequence()
    stream.close()
    
    file
  }
}

trait Ascii {
  protected val font = new Font("Courier", Font.PLAIN, 10)
  protected val frc = new FontRenderContext(null, true, true)
  protected def grayFromRgb(r: Int, g: Int, b: Int) = (r*0.21+g*0.72+b*0.07).toInt
  protected def buffIm2SeqStr(original: BufferedImage) = {
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
		  	pixel.invert
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