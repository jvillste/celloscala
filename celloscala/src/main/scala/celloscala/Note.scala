package celloscala


case class Note(val halfStepsFromA:Int) {
	var start:Long = 0
	var end:Long = 0
	
	val frequency = Math.pow(1.059463094359, halfStepsFromA)* 440

	def isEqualTo(otherNote : Note) : Boolean =
	{
		return otherNote.start == start && otherNote.end == end && otherNote.halfStepsFromA == halfStepsFromA
	}
	
	def name : String = {
		val noteNames = List("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
		val octave = Math.floor((halfStepsFromA + 10) / 12).toInt + 4
		
		noteNames(scalaIndex) + octave.toString
	}
	
	def scalaIndex : Int = {
		var index = halfStepsFromA%12
		if(index < 0)
			index = 12 + index
		
		index
	}
	
	def scoreIndex : Int = {
		
		var octavesFromA = if(halfStepsFromA > 0) Math.floor(halfStepsFromA / 12.0) else Math.ceil(halfStepsFromA / 12.0) 
		var index = halfStepsFromA%12
		
		val scalaNotesFromA = if(index > 0) Note.scala.slice(1,index+1).foldLeft(0)(_ + _)
							  else if(index < 0)- Note.invertedScala.slice(0,-index).foldLeft(0)(_ + _)
							  else 0
		//println(scalaNotesFromA  + " " + index +  " "  + halfStepsFromA)
		scalaNotesFromA + octavesFromA.toInt * 7
		
	}
	
	def distanceFromFrequencyInHalfSteps(targetFrequency:Double) : Double = {
		if( targetFrequency < frequency )
			return (targetFrequency - frequency) / (frequency - new Note(halfStepsFromA -1).frequency)
		else
			return (targetFrequency - frequency) / (new Note(halfStepsFromA + 1).frequency - frequency)
	}
	
	def sharp : Boolean = Note.scala(scalaIndex) == 0
	
	override def toString = name
}

object Note {
	

	
	val scala = List(1,0,1,1,0,1,0,1,1,0,1,0)
	//val invertedScala = List(0,1,0,0,1,0,1,0,0,1,0,1)
	val invertedScala = List(1,0,1,0,1,1,0,1,0,1,1,0)
	
	val allNotes = (-35 to 10).toList.map(Note(_))
	val scalaIntervals = List(2,1,2,2,1,2,2)
	
	def closestNote(frequency:Double) : Note = closestNote(frequency, allNotes)
	
	def closestNote(frequency:Double, notes:List[Note]) : Note = {
		
		def distance(note:Note) = Math.abs(frequency - note.frequency)
		
		return notes.sort((note1,note2) => distance(note1) < distance(note2)).head

	}
	
	

	def fromScoreIndex(scoreIndex:Int) : Note =
	{
		var octavesFromA = if(scoreIndex > 0) Math.floor(scoreIndex / 7.0)
					      else Math.ceil(scoreIndex / 7.0)
		var index = scoreIndex%7
		
		val halfStepsFromA = if(index > 0) Note.scalaIntervals.slice(0,index).foldLeft(0)(_ + _)
							  else -Note.scalaIntervals.reverse.slice(0,-index).foldLeft(0)(_ + _)    
		
	    Note(halfStepsFromA + octavesFromA.toInt * 12)
	}
}