package celloscala.widgets
import celloscala.midi._
import celloscala.Note
import java.awt.{Color,Dimension}
import javax.sound.midi._
import scala.collection.immutable.TreeMap
import scala.util.Random
import scala.actors._
import scala.swing.event._
import celloscala.Timer

class Tracker(timer:Timer) extends Score {
	
	minimumSize = new Dimension(100,200)
	maximumSize = new Dimension(2000,500)
	preferredSize = maximumSize

	
	val longestNote = 2000
	val synthesizer =  MidiSystem.getSynthesizer
	synthesizer.open
	
	setInstrument(43)
	
	//override def preferredSize = new java.awt.Dimension(200,200)
	
	var cursorPosition : Option[Float] = None
	
	var playTrack = 0
	var showTracks:List[Int] = List.empty[Int]
	var learningMode = false
	var secondsPerPixel = 0.02f

	var randomSharps = true
	var randomNotesFrom = -20
	var randomNotesTo = -15
	var tracks = List.empty[celloscala.midi.Track]

	
	var _midiFileName:String = null
	def midiFileName_=(newFileName : String) = {
		playTrack = 0
		timer.currentTimeInSeconds = 0
		
		_midiFileName = newFileName
		tracks = MidiReader.getTracks(newFileName)
		
		showTracks = List.range(0 ,tracks.size)
	}
	def midiFileName = _midiFileName

	var soundOn = true

	
	
	def positionInTicks(x:Int) = {
		timer.mapSecondsToTicks(timer.currentTimeInSeconds + (x-playingNoteX)*secondsPerPixel)
	}
	
	def playRandomNotes {
		tracks = List(new celloscala.midi.Track("Random notes",
												Set.empty[Int],
												getRandomNotes(randomNotesFrom, randomNotesTo,50)))
		timer.currentTimeInSeconds = 0
		playTrack = 0
		showTracks = List.empty[Int]
	}

	var playingNote : Note = null
	
	var milliSecondsPerTick = 3.0
	var lastUpdate : Long = 0
	
	private[this] var _playing = false
	def playing = _playing
	def playing_=(value:Boolean) { 
		_playing = value
		if(!value && playingNote != null)
		{
			stopNote(playingNote)
			playingNote = null
		}
		lastUpdate = System.currentTimeMillis()
	}
	
	
	def update(){
		if(playing)
		{
			if(learningMode == false
					|| playingNotes.size == 0 
					|| (listening && performedNote != null && performedNote.halfStepsFromA == playingNotes(0).halfStepsFromA && Math.abs(performedNoteDistanceFromNote) < 0.2 )
					|| timer.mapTicksToSeconds(playingNotes(0).end) - timer.currentTimeInSeconds < 2*secondsPerPixel)
				
				timer.currentTimeInSeconds += (System.currentTimeMillis() - lastUpdate) / 1000f
		
			lastUpdate = System.currentTimeMillis()

		}
		
		updateSound
	}
	
	def updateSound {
		if(playingNotes.size > 0){
				if(playingNote == null || ! playingNote.isEqualTo(playingNotes(0)))
				{
					if(playingNote != null)
						stopNote(playingNote)
					
					playingNote = playingNotes(0)
					if(soundOn)
					{
						if(learningMode)
							playNoteShortlyWhileNotListening(playingNote)
						else
							playNote(playingNote)
					}
						
					
				}
			}
			else
			{
				if(playingNote != null){
					stopNote(playingNote)
					playingNote = null
				}
			}
	}

	def getRandomNotes(from:Int, to:Int, count:Int) : TreeMap[Long,List[Note]] =
	{
		val noteLength = 50
		var notes = new TreeMap[Long,List[Note]]()
		var lastNote:Note = null
		for(i <- 0 until count)
		{
			var note:Note = null
			do{
				note = new Note(Random.nextInt(to - from + 1) + from)
			} while((lastNote != null && (note.halfStepsFromA  == lastNote.halfStepsFromA))
					|| (!randomSharps && note.sharp))
			lastNote = note
			
			note.start = i * noteLength
			note.end = (i + 1) * noteLength
			notes = notes.insert(note.start, List.empty[Note])
			notes = notes.updated(note.start, note :: notes(note.start))
		}
		
		notes
	}
	
	def setInstrument(instrumentIndex : Int)
	{
		synthesizer.getChannels()(0).programChange(0, instrumentIndex)
	}
	
	def playNote(note : Note)
	{
//		println("playing " + note.start + " " + note.end + " " + note.halfStepsFromA )
		synthesizer.getChannels()(0).noteOn(note.halfStepsFromA + 69,100)
	}
	
	def playNoteShortlyWhileNotListening(note:Note)
	{
		object player extends Actor {
			def act() {
				listening = false
				frequency = 0
				playNote(note)
				Thread.sleep(1000)
				stopNote(note)
				Thread.sleep(1000)
				listening = true
			}
		}
		player.start()
	}
	
	def stopNote(note : Note)
	{
	//	println("stopping " + note.start + " " + note.end + " " + note.halfStepsFromA )
		synthesizer.getChannels()(0).noteOff(note.halfStepsFromA + 69)
	}
	
	def getNotes(track:Int, from:Long, to:Long) : List[Note] = {
		if(tracks.size > track)
			return tracks(track).notes.range(from, to).values.foldLeft(List.empty[Note])(_ ::: _)
		else
			return List.empty[Note]
	}

	def playingNotes : List[Note] =
	{
		getNotes(playTrack, timer.currentTick.toLong-longestNote, timer.currentTick.toLong).filter(note => note.end >= timer.currentTick.toLong)
	}
	
	
	def timeToScreenX(time:Double) = (playingNoteX + (time - timer.currentTimeInSeconds)/secondsPerPixel).toInt
			
	override def paint(g : java.awt.Graphics2D){
		super.paint(g)
		
		g.setColor(Color.BLACK)
		
		var status = (if(learningMode) " learning mode" else "") 
		
		if(tracks.size > 0)
			status += " " + (playTrack +1) + "/" + (tracks.size) + " " + tracks(playTrack).name + " (" + tracks(playTrack).instruments.foldLeft("")(_+_) + ")" 
       	
        g.drawString(status, 30, 10)
        
        def drawTracks(tracksToDraw:List[Int], colors:List[Color]){
			
			for((track,color) <- tracksToDraw.zip(colors);
				note <- getNotes(track,
						timer.mapSecondsToTicks(timer.currentTimeInSeconds - bounds.width * secondsPerPixel).toLong - longestNote,
						timer.mapSecondsToTicks(timer.currentTimeInSeconds + bounds.width * secondsPerPixel).toLong))
			{
	
				drawNote(g,
						color,
						 note,
						((timer.mapTicksToSeconds(note.start) - timer.currentTimeInSeconds)/secondsPerPixel).toInt,
						((timer.mapTicksToSeconds(note.end) - timer.currentTimeInSeconds)/secondsPerPixel).toInt)
						
			}
		}

		drawTracks(showTracks.filter(_ != playTrack),List(Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW,Color.MAGENTA,Color.CYAN))
		drawTracks(List(playTrack),List(Color.BLACK))
		
		cursorPosition match {
			case Some(time) =>
				g.setStroke(new java.awt.BasicStroke(1))
				val x = timeToScreenX(time)
				g.drawLine(x,20,x,bounds.height)
			case None =>
		}
	}
}