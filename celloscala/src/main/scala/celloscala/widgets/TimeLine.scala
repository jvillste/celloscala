package celloscala.widgets

import scala.swing.{Panel,Swing}
import java.awt.{Dimension,Color}
import scala.swing.event.{MouseClicked,MouseMoved,MouseExited}
import celloscala.Timer

class TimeLine(timer:Timer) extends Panel {
	border = Swing.LineBorder(Color.WHITE,1)
	
	val unactiveColor = new Color(0.9f,0.9f,0.9f)
	val activeColor = new Color(0.8f,0.8f,0.8f)
	background = unactiveColor
	
	val optimumSize = new Dimension(4000,15)
	minimumSize = new Dimension(100,15)
	maximumSize = optimumSize
	preferredSize = optimumSize
    var playingNoteX = 0
    
    var mousePositionInTime : Option[Float] = None
    
	var secondsPerPixel = 0.02f
	
	def click(time:Float){}
	def rightClick(time:Float){}
	
	val header = ""
	
	listenTo(mouse.clicks)
	listenTo(mouse.moves)
	reactions += {
		case MouseClicked(source,point,0,clicks,triggersPopup) =>
			click(xToTime(point.x))
					
		case MouseClicked(source,point,256,clicks,triggersPopup) =>
			rightClick(xToTime(point.x))
		
		case MouseMoved(source, point, modifiers) =>
			mousePositionInTime = Some(xToTime(point.x))
			background = activeColor
			
		case MouseExited(_,_,_) =>
			mousePositionInTime = None
			background = unactiveColor
	}
	
	def timeToX(time:Float) = ((time - timer.currentTimeInSeconds) / secondsPerPixel + playingNoteX).toInt 
	def xToTime(x:Int) =  timer.currentTimeInSeconds + (x-playingNoteX)*secondsPerPixel
	
	def drawPointInTime(g : java.awt.Graphics2D, time : Float)
	{
		g.fillOval(timeToX(time)-5,bounds.height/2-5,11,11)	
	}

	override def paint(g : java.awt.Graphics2D){
		super.paint(g)

		g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON)

		playingNoteX = bounds.width / 2
		
		g.setColor(Color.BLACK)
		g.setStroke(new java.awt.BasicStroke(1))
		
		// Draw the center
		g.drawLine(playingNoteX,0,playingNoteX, bounds.height)
		
		// Draw te header
		g.setColor(Color.GRAY)
		g.drawString(header, 2,11)

	}
}