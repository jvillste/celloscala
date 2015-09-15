package celloscala

object Common {
	val notes = List("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#","A", "A#", "B")
	val openStringFrequencies = List(65.41, 98.00, 146.83, 220.00)
	val openStringNoteNameIndexes = List(0, 7, 2, 9)
	val openStringOctaves = List(2, 2, 3, 3)
	
	val openStringNotes = List(Note(-12), Note(-19), Note(-26), Note(-33))
	
	
	def getNoteFrequency(baseNoteFrequency:Double, halfSteps:Int) : Double =
	{
		return Math.pow(1.059463094359, halfSteps)* baseNoteFrequency
	}
	
	def getNoteFrequency(halfSteps:Int) : Double = getNoteFrequency(440,halfSteps)

	def frequencyOnString(frequency : Double, openStringFrequency : Double, openStringLength:Double) : Double =
	{
		return openStringFrequency / frequency * openStringLength;
	}
	
	def inScale(noteIndex:Int, scale : Set[Int]) : Boolean = {
		return scale.contains((noteIndex%12))
	}
	
	def getNoteName(baseNoteIndex:Int, baseNoteOctave:Int, halfStepsFromBaseNote:Int) : String = {
		val octave = Math.floor((halfStepsFromBaseNote + baseNoteIndex) / 12).toInt + baseNoteOctave
		return notes((halfStepsFromBaseNote+baseNoteIndex)%12) + octave.toString
	}
	
	def linearizeFrequency(f : Double, minimumFrequency:Double, maximumFrequency:Double) : Double = {
		return (Math.log(f) - Math.log(minimumFrequency)) / (Math.log(maximumFrequency) - Math.log(minimumFrequency)) 
	}
	

}