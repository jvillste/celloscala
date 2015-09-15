package celloscala.widgets
import javazoom.jl.player.{Player => JLPlayer }
import javazoom.jl.decoder._
import scala.swing._
import java.io.{FileInputStream,InputStream,File,ByteArrayInputStream, ByteArrayOutputStream}
import java.awt.Color
import javax.sound.sampled._
import scala.actors._

class Player  {
	var line:SourceDataLine = null
	var stream :AudioInputStream = null
	var fileName :String = null
	var startPosition = 0f
	var lineStartPosition = 0l
	var isFileLoaded = false
	var playBuffer:Array[Byte] = null
	def positionInSeconds : Float = {
		if(line != null)
		{
			return startPosition + (line.getMicrosecondPosition - lineStartPosition)/ 1000000f
		}
		else
			return 0
	}

	def closeFile {
		playing = false
		fileName = null
	}
	
	def jumpTo(positionInSeconds : Float)
	{
		val oldPlaying = playing
		playing = false
		line.flush()
		
		stream.reset()
		
	    stream.skip((stream.getFormat.getSampleRate * stream.getFormat.getFrameSize * positionInSeconds).toLong)
	    
	    startPosition = positionInSeconds
	    lineStartPosition = line.getMicrosecondPosition
	    playing = oldPlaying
	}
	
	def openFile(fileName:String)
	{
		playing = false
		isFileLoaded = false
		if(line != null)
		{
			line.close()
			line = null
		}
		if(stream != null)
		{
			stream.close()
			stream = null
		}
			
		this.fileName = fileName
		val baseStream = AudioSystem.getAudioInputStream(new File(fileName))

		playBuffer = new Array[Byte](4096)
		
	    val baseFormat = baseStream.getFormat()
	    

		val decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);

		stream = AudioSystem.getAudioInputStream(decodedFormat, baseStream);

		val buffer = new ByteArrayOutputStream()
		var n = stream.read(playBuffer,0,playBuffer.size)
		while(n != -1)
		{
			buffer.write(playBuffer,0,n)
	    	n = stream.read(playBuffer,0,playBuffer.size)
		}
		
		val byteArray = buffer.toByteArray()
		
		stream = new AudioInputStream(new ByteArrayInputStream(byteArray), decodedFormat, byteArray.length / decodedFormat.getFrameSize());
		
		stream.mark(byteArray.length)
		
	    line =  AudioSystem.getLine(new DataLine.Info(classOf[SourceDataLine],decodedFormat)).asInstanceOf[SourceDataLine]
	    line.open(decodedFormat)
	    
	    startPosition = 0
	    isFileLoaded = true
	}
	
	private var _playing = false
	def playing_=(value:Boolean)
	{
		if(isFileLoaded)
		{
			if(value)
			{
				line.start()
				_playing = true
			}
			else
			{
				line.stop()
				_playing = false
			}
		}
	}
	
	def playing:Boolean = _playing 

	
	def play {
		playing = true
	}
	
	def stop{
		playing = false
	}
	
	object player extends Actor {
		def act() {
			while(true)
			{
			     if(playing)
			     {
				     val n = stream.read(playBuffer,0,playBuffer.size)

				     if(n>0)
				    	 line.write(playBuffer,0,n)
			     }
			}
		}
	}
	player.start()
}