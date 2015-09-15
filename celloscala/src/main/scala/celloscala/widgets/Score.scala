package celloscala.widgets
import scala.swing._
import celloscala.Note
import java.awt.Color
import java.awt.Paint

class Score extends Label
{
	val scoreLineStart = 50
	var playingNoteX = 0
	val scoreLineGap = 12
	var performedNote : Note = null
	var performedNoteDistanceFromNote : Double = 0
	
	val lowestScoreLineIndex = -20
	val highestScoreLineIndex = 8
	
	var listening = true

	def frequency = 0
	def frequency_=(value:Double) = {
		if(value == 0)
		{
			performedNote = null
			performedNoteDistanceFromNote = 0
		}
		else
		{
			if(listening)
			{
				performedNote  = Note.closestNote(value)
				performedNoteDistanceFromNote = performedNote.distanceFromFrequencyInHalfSteps(value)
			}
		}
	}

	override def paint(g : java.awt.Graphics2D){
		playingNoteX = bounds.width / 2
		
		val discantLines = List(Note(-5), Note(-2), Note(2), Note(5), Note(7))
		val baseLines = List(Note(-26), Note(-22), Note(-19), Note(-16), Note(-12))
		
		super.paint(g)
		
		for(i <- lowestScoreLineIndex to highestScoreLineIndex)
		{
			drawScoreLine(g, i)
		}
		
		if(performedNote != null)
			drawPerformedNote(g,performedNote)

		g.setColor(Color.BLACK)
		g.setStroke(new java.awt.BasicStroke(1))
		g.drawLine(playingNoteX, 20, playingNoteX, bounds.height)

	
	}
	
	def drawScoreLine(g : java.awt.Graphics2D, scoreIndex : Int)
	{
		val color = if((( scoreIndex >= -15 && scoreIndex <= -7 ) ||
						( scoreIndex >= -3 && scoreIndex <= 5 ))
						&& scoreIndex%2 != 0)
						Color.BLACK
					else if(scoreIndex%2 != 0)
						new Color(0.85f, 0.85f, 0.85f)
					else
						null
		
		val y = scoreNoteHeight(scoreIndex)
		
		if(color != null)
		{
	        g.setColor(color)
			g.setStroke(new java.awt.BasicStroke(3))
			g.drawLine(scoreLineStart, y, bounds.width, y)
		}
		
		g.setColor(Color.BLACK)
		g.drawString(Note.fromScoreIndex(scoreIndex).name, 0, y + 5)
	}
	
	def scoreNoteHeight(scoreIndex:Int) = (highestScoreLineIndex - scoreIndex)*scoreLineGap + 15

	def drawNote(g : java.awt.Graphics2D,color:Color, note:Note, start:Int, end:Int)
	{

		g.setColor(color)

		g.setStroke(new java.awt.BasicStroke(3))
		val y = scoreNoteHeight(note.scoreIndex) 
		
		val clippedStart = Math.max(scoreLineStart,playingNoteX + start)
		
		if(end + playingNoteX > scoreLineStart)
		{
			
			g.fillRoundRect(clippedStart, y- scoreLineGap/2, playingNoteX + end - clippedStart, scoreLineGap, 8, 8)
			//g.drawLine(clippedStart, y, playingNoteX + end, y)
			
			if(note.sharp)
			{
				g.setColor(Color.WHITE)
				g.drawString("#", clippedStart+2, y+scoreLineGap/2-2)
			}
			/*
			if(start > 0)
			{
				g.setColor(new Color(0f,0.8f,0f))
				g.drawLine(clippedStart, y, clippedStart+1, y)
			}
			*/
		}
	}

	def drawPerformedNote(g : java.awt.Graphics2D, note:Note)
	{
		g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
        
        val y = scoreNoteHeight(note.scoreIndex) 
                        
        g.setColor(Color.BLACK)
        g.setStroke(new java.awt.BasicStroke(1))
		g.drawLine(scoreLineStart - 25,
					y,
					scoreLineStart-10,
					y)
		
		g.fillPolygon(Array(scoreLineStart-10,scoreLineStart-3, scoreLineStart-10),
					Array(y-scoreLineGap,y,y+scoreLineGap),
					3)
		
		g.setColor(Color.RED)
		g.setStroke(new java.awt.BasicStroke(3))
		g.drawLine(scoreLineStart - 25,
					y - (performedNoteDistanceFromNote*scoreLineGap*2).toInt,
					scoreLineStart-12,
					y - (performedNoteDistanceFromNote*scoreLineGap*2).toInt)
		
				
		if(note.sharp)
			g.drawString("#", scoreLineStart - 35, y + 5)
	}
}