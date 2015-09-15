package celloscala.widgets

import scala.swing._
import java.io.{FileInputStream,InputStream,File}
import java.awt.Color
import javax.sound.sampled._
import celloscala.FrequencyMeter

class Grapher extends Panel {
	val optimumSize = new Dimension(4000,100)
	minimumSize = new Dimension(100,100)
	maximumSize = optimumSize
	preferredSize = optimumSize
    
	var currentTimeInSeconds = 0f
	var samplesPerPixel = 1f
	var secondsPerSample = 1f / 10f
	
	var visualizableStream :AudioInputStream = null
	var intBuffer : Array[Int] = null
	
	def fileName_=(newFileName : String) = {
		val outDataFormat = new AudioFormat(44100f, 16, 2, true, false)
		val fileStream = AudioSystem.getAudioInputStream(new File(newFileName))
		visualizableStream = AudioSystem.getAudioInputStream(outDataFormat, fileStream)
		
		val framesPerSample = (44100 * secondsPerSample).toInt
		val buffer = new Array[Byte](framesPerSample * 4)
		intBuffer = new Array[Int](visualizableStream.getFrameLength.toInt / framesPerSample)
		val frequencyAnalysisBuffer = new Array[Int](framesPerSample) 
	    var n = visualizableStream.read(buffer)
		var graphIndex = 0
		var volume = 0f
		while(n > 0)
		{
			for ( i <- 0 until n by 4 ) {

				val value1 = (buffer(i+1) << 8) + (buffer(i) & 0x00ff)
		    	val value2 = (buffer(i+3) << 8) + (buffer(i+2) & 0x00ff)
		    	volume += (value1.abs.toFloat + value2.abs.toFloat) / 5000000f  /secondsPerSample
		    	
		    		//intBuffer(graphIndex) = ((intBuffer(graphIndex) + ((value1.toFloat + value2.toFloat) / 2f)) / 2f).toInt;
		    	//frequencyAnalysisBuffer(i>>4) = ((value1.toFloat + value2.toFloat) / 2f).toInt
			}
			
			if(intBuffer.length > graphIndex)
			{
				

		    	intBuffer(graphIndex) = Math.round(volume)
		    	//println(intBuffer(graphIndex))
			}
			volume = 0
			//println(frequencyAnalysisBuffer.size)
			//
		
		    	/*
	    	if(intBuffer.length > graphIndex)
	    	{
	    		val pitch = (FrequencyMeter.getPitch(frequencyAnalysisBuffer,44100)/1000f*100f).toInt //((intBuffer(graphIndex) + ((value1.toFloat + value2.toFloat) / 2f)) / 2f).toInt;
	    		if(pitch == 0 && graphIndex > 1)
	    			intBuffer(graphIndex) = intBuffer(graphIndex-1)
	    		else
	    			intBuffer(graphIndex) = pitch
	    		//println(intBuffer(graphIndex))
	    	}
		*/
			n = visualizableStream.read(buffer);
			graphIndex += 1
		}
	}
	
	override def paint(g : java.awt.Graphics2D){
		super.paint(g)
		g.setColor(Color.BLACK)
		g.setStroke(new java.awt.BasicStroke(1))
		//g.drawLine(0, 0, 10, 10)

		var lastY = 0
		var index = 0

		val currentX = bounds.width / 2
		for(i <- 0 until bounds.width)
		{
			val index = (currentTimeInSeconds / secondsPerSample + (i-currentX) * samplesPerPixel).toInt
			if(index >= 0 && index < intBuffer.size)
			{
				val value = intBuffer(index) /// Math.pow(2,16)) * 100f).toInt
				//println(value)
				g.drawLine(i-1,99-lastY, i, 99-value)
				lastY = value
			}
			if(i%50 == 0)
			{
				g.drawString("%.1f".format(currentTimeInSeconds + ((i-currentX)*samplesPerPixel * secondsPerSample)), i, 10)
			}
		}
		g.drawLine(currentX,0, currentX, bounds.height)
	}
}