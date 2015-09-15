package celloscala
import scala.collection.immutable.TreeMap
class Timer {
	var defaultSecondsPerTick = 0.003
	
	var currentTimeInSeconds = 0f
	private var secondsToTicks = TreeMap.empty[Double,Double]
	private var ticksToSeconds = TreeMap.empty[Double,Double]

	def addCheckPoint(time:Double,tick:Double)
	{	for((checkPointTime, checkPointTick) <- secondsToTicks.to(time))
		{
			if(checkPointTime == time || checkPointTick > tick)
			{
				secondsToTicks -= checkPointTime
				ticksToSeconds -= checkPointTick
			}
		}
		
		secondsToTicks = secondsToTicks.insert(time,tick)
		ticksToSeconds = ticksToSeconds.insert(tick,time)
	}
	
	
	def currentTick:Double = mapSecondsToTicks(currentTimeInSeconds)
	
	def mapSecondsToTicks(seconds:Double) = {
		linearMap(seconds,secondsToTicks,1/defaultSecondsPerTick)
	}
	def mapTicksToSeconds(ticks:Double) = {
		linearMap(ticks,ticksToSeconds,defaultSecondsPerTick)
	}
	
	def removeCheckPointInTime(time:Double){
		ticksToSeconds -= secondsToTicks(time)
		secondsToTicks -= time
	}
	
	def clearCheckPoints {
		secondsToTicks = TreeMap.empty[Double,Double] 
		ticksToSeconds = TreeMap.empty[Double,Double] 
	}
	
	def getCheckPointsInTime(from:Double, to:Double) : List[Double] = 
	{
		return secondsToTicks.range(from,to).toList.map(_._1)
	}
	
	def allCheckPoints : List[(Double,Double)] =
	{
		secondsToTicks.toList
	}
	
	def linearMap(source:Double, checkPoints:TreeMap[Double,Double],defaultTargetPerSource:Double):Double = {
		
		val checkPointsBefore = checkPoints.until(source)
		val checkPointsAfter = checkPoints.from(source)
		
		if(checkPointsBefore.size == 0 && checkPointsAfter.size == 0)
		{
			return (source*defaultTargetPerSource).toDouble
		}else if(checkPointsBefore.size == 0 && checkPointsAfter.size > 0)
		{
			var (nextSource, nextTarget) = checkPointsAfter.first
			if(nextSource == 0d)
				nextSource = 0.001d
			val speed = nextTarget / nextSource
			return speed*source
		}else if(checkPointsBefore.size == 1 && checkPointsAfter.size == 0)
		{
			val (lastSource, lastTarget) = checkPointsBefore.last
			return lastTarget + defaultTargetPerSource*(source - lastSource)
		}else if(checkPointsBefore.size > 1 && checkPointsAfter.size == 0)
		{
			val (lastSource, lastTarget) = checkPointsBefore.last
			val (secondLastSource, secondLastTarget) = checkPointsBefore.until(lastSource).last
			
			val speed = (lastTarget - secondLastTarget) / (lastSource - secondLastSource)
			return lastTarget + speed*(source - lastSource)
		}else
		{
			val (lastSource, lastTarget) = checkPointsBefore.last
			val (nextSource, nextTarget) = checkPointsAfter.first
			val speed = (nextTarget - lastTarget) / (nextSource - lastSource)
			return lastTarget + speed*(source - lastSource)
		}
	}
}