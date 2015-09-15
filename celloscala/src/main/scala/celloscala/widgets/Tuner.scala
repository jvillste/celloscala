package celloscala.widgets
import scala.swing._
import java.awt.Color
import celloscala.Note._
import celloscala.Common._
import java.text.DecimalFormat

class Tuner extends Panel
{
	var frequency : Double = 0
	
	override def paint(g : java.awt.Graphics2D){
		super.paint(g)
		
		val closest = closestNote(frequency, openStringNotes)
		val difference = frequency - closest.frequency

		if(frequency > 0)
		{
			g.setColor(Color.BLUE)
			g.setStroke(new java.awt.BasicStroke(1))
			val y = (difference*10.0).toInt + 100
			g.drawLine(50, y, 70, y)
			g.drawString(closest.name + " " + new DecimalFormat("0.00").format(difference),0,20)
		}
		
		g.setStroke(new java.awt.BasicStroke(1))
		g.setColor(Color.BLACK)
		g.drawLine(50, 100, 70, 100)

	}
}