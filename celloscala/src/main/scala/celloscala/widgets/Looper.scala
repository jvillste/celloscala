package celloscala.widgets

import scala.swing.{Panel}
import java.awt.{Dimension,Color}
import scala.swing.event.{MouseClicked}
import celloscala.Timer

class Looper(timer:Timer) extends TimeLine(timer) {
	
	override val header = "Loop"

	var loopStart : Option[Float] = None
	var loopEnd : Option[Float] = None
	
	def isLoopDefined = !loopStart.isEmpty && !loopEnd.isEmpty

	override def click(time:Float) {
		loopStart = Some(time)
		if(!loopEnd.isEmpty)
			if(loopEnd.get <= loopStart.get)
				loopEnd = None
	}
	
	override def rightClick(time:Float)
	{
		loopEnd = Some(time)
		if(!loopStart.isEmpty)
			if(loopStart.get >= loopEnd.get)
				loopStart = None
	}
	
	override def paint(g : java.awt.Graphics2D){
		super.paint(g)
		
		g.setColor(Color.BLACK)
		g.setStroke(new java.awt.BasicStroke(2))
		
		if(!loopStart.isEmpty)
		{
			drawPointInTime(g,loopStart.get)
		}
		if(!loopEnd.isEmpty)
		{
			drawPointInTime(g,loopEnd.get)
		}

		if(isLoopDefined)
		{
			g.drawLine(timeToX(loopStart.get),bounds.height/2,timeToX(loopEnd.get), bounds.height/2)
		}

		
	}
}