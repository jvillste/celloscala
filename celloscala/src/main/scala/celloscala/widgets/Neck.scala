package celloscala.widgets

import scala.swing._
import scala.swing.event._
import celloscala.FrequencyMeter
import java.awt.Graphics2D
import java.awt.Color
import celloscala.Common._
import celloscala.midi._
import celloscala.widgets._
import celloscala.Note

class Neck extends Panel
{
	val optimumSize = new Dimension(250,600)
	minimumSize = optimumSize
	maximumSize = optimumSize
	preferredSize = optimumSize
	
	
	var playedFrequency = 0.0
	val maxFrequency = 100.0
	//val height = 400.0
	val cMajor = Set(0,2,4,5,7,9,11)
	val scale = cMajor
	var notes = List.empty[Note]
	
	override def paint(g : java.awt.Graphics2D){
		
		super.paint(g)
		
		val scaleLength = 40
		val minimumFrequency = openStringFrequencies(0)
		val maximumFrequency = getNoteFrequency(openStringFrequencies(0),scaleLength)
		
		val noteFrequencies = notes.map(note => getNoteFrequency(440, note.halfStepsFromA))
		
		// Draw the neck
		drawNeck(g, playedFrequency, noteFrequencies)
		
	}
	
	def drawNeck(g : Graphics2D, frequency:Double, notes:List[Double]){
		val length = 600

		for(n <-(0 to 3))
			drawString(g,10+55*n,30,length,openStringNoteNameIndexes(n), openStringOctaves(n), openStringFrequencies(n), frequency, notes)
	}
	
	def drawString(g:Graphics2D, x:Int, y:Int, length:Int, openStringNoteIndex:Int, openStringOctave:Int, openStringFrequency:Double, playedFrequency:Double, notes:List[Double])
	{
		def drawFrequencyOnString(frequency:Double) {
			val location = y + length - frequencyOnString(frequency, openStringFrequency, length).toInt
			g.drawLine(x+25, location, x+45, location)
		}
		
		// Draw frets
		
		for(n <-(0 to 30))
		{
			val frequency = getNoteFrequency(openStringFrequency,n)
			val location = y + length - frequencyOnString(frequency, openStringFrequency, length).toInt
			
			g.setColor(getNoteColor(inScale(n+openStringNoteIndex,scale)))
			g.setStroke(new java.awt.BasicStroke(1))
			g.drawLine(x+25, location, x+45, location)
			g.drawString(getNoteName(openStringNoteIndex, openStringOctave, n), x, location+5)
		}
	
		// Draw played note
		if(playedFrequency > 0)
		{
			g.setColor(java.awt.Color.RED)
			g.setStroke(new java.awt.BasicStroke(3))
			drawFrequencyOnString(playedFrequency)
		}

		for(note <- notes)
		{
			g.setColor(java.awt.Color.BLUE)
			g.setStroke(new java.awt.BasicStroke(3))
			drawFrequencyOnString(note)	
		}
	}

	def getNoteColor(inScale : Boolean) : Color = {
		if(inScale)
			return Color.BLACK
		else 
			return new Color(0.8f, 0.8f, 0.8f)
	}
	
}
