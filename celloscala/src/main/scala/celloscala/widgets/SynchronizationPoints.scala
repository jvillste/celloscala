package celloscala.widgets

import celloscala._
import java.awt.Color

class SynchronizationPoints(timer:Timer) extends TimeLine(timer){
	
	override val header = "Midi synchronization points"
	
	def isInSelectionDistance(mousePosition:Float, checkPointPosition:Float) = 
	{
		Math.abs(mousePosition - checkPointPosition) / secondsPerPixel < 5
	}
		
	override def click(time:Float){
		var removedCheckPoint = false
		
		for(checkPoint <- visibleCheckPoints)
		{
			if(isInSelectionDistance(time, checkPoint.toFloat))
			{
				timer.removeCheckPointInTime(checkPoint)
				removedCheckPoint = true
			}
		}
		
		if(!removedCheckPoint)
			timer.addCheckPoint(timer.currentTimeInSeconds , timer.mapSecondsToTicks(time))
	}

	def visibleCheckPoints: List [Double] =
	{
		return timer.getCheckPointsInTime(timer.currentTimeInSeconds - bounds.width * secondsPerPixel, 
				timer.currentTimeInSeconds + bounds.width * secondsPerPixel)
		
	}
	
	override def paint(g : java.awt.Graphics2D){
		super.paint(g)
		
		g.setColor(Color.BLACK)
		
		
		for(checkPoint <- visibleCheckPoints)
		{
			g.setColor(mousePositionInTime match {
				case Some(time) if(isInSelectionDistance(time,checkPoint.toFloat)) =>
					Color.RED
				case _ => Color.BLACK
			})
			
			drawPointInTime(g, checkPoint.toFloat)
		}
	}
}