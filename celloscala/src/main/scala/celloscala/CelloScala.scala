package celloscala

import scala.swing._
import scala.swing.event._
import java.awt.Graphics2D
import java.awt.Color
import Common._
import celloscala.midi._
import celloscala.widgets._
import javax.swing.{SwingUtilities,BorderFactory}
import java.io.File
import scala.xml.XML

object CelloScala extends SimpleSwingApplication {

	var playingLoopOnce = false
	var positionAfterPlayingLoopOnce = 0f
	
	var stateFileName:String = null
	def synchronizeToSound = player.fileName != null
	
	val player = new Player()
	
	
	val timer = new Timer()
	
	val looper = new Looper(timer)
	val synchronizationPoints = new SynchronizationPoints(timer)
	
	val neck =  new Neck()
	neck.background = Color.WHITE
	val tracker = new Tracker(timer)
	tracker.background = Color.WHITE

	def secondsPerPixel = looper.secondsPerPixel
	def secondsPerPixel_=(newSecondsPerPixel:Float) {
		looper.secondsPerPixel = newSecondsPerPixel
		tracker.secondsPerPixel = newSecondsPerPixel
		synchronizationPoints.secondsPerPixel = newSecondsPerPixel
	}
	
	def currentTimeInSeconds = {
		if(synchronizeToSound)
			player.positionInSeconds
		else
			timer.currentTimeInSeconds	
	}
	def currentTimeInSeconds_=(newCurrentTimeInSeconds:Float) {
		if(synchronizeToSound)
			player.jumpTo(newCurrentTimeInSeconds)
		else
			timer.currentTimeInSeconds = newCurrentTimeInSeconds
	}
	def playing = {
		if(synchronizeToSound)
			player.playing
		else
			tracker.playing
	}
	def playing_=(newPlaying:Boolean) {
		if(synchronizeToSound)
			player.playing = newPlaying
		else
			tracker.playing = newPlaying
		
		if(!newPlaying)
			playingLoopOnce = false
	}
	val notes = (-33 to 12).map(index => Note(index)).filter(!_.sharp)
	val randomFrom = new ComboBox(notes)
	val randomTo = new ComboBox(notes)
	randomTo.selection.item = notes(14)
	val sharps = new CheckBox()
	sharps.background = Color.WHITE
	val randomSettings = new FlowPanel {
		contents += new Label("Random from: ")
		contents += randomFrom
		contents += new Label("To: ")
		contents += randomTo
		contents += new Label("Allow sharps: ")
		contents += sharps
	}
	
	randomSettings.background = Color.WHITE

	val statusLabel = new Label("")

		val menuBar = new MenuBar {
		contents += new Menu("Menu") {
			contents += new MenuItem(Action("Save state") { 
				val fileChooser = new FileChooser()

				if(stateFileName != null)
					fileChooser.selectedFile = new File(stateFileName)

				if(fileChooser.showOpenDialog(this) == FileChooser.Result.Approve)
				{
					saveState(fileChooser.selectedFile.getPath())
				}

			})
			contents += new MenuItem(Action("Load state") { 
				val fileChooser = new FileChooser()

				if(stateFileName != null)
					fileChooser.selectedFile = new File(stateFileName)

				if(fileChooser.showOpenDialog(this) == FileChooser.Result.Approve)
				{
					loadState(fileChooser.selectedFile.getPath())
				}

			})
			contents += new MenuItem(Action("Load midi") { 
				val fileChooser = new FileChooser()
				if(tracker.midiFileName != null)
					fileChooser.selectedFile = new File(tracker.midiFileName)
				if(fileChooser.showOpenDialog(this) == FileChooser.Result.Approve)
				{
	                tracker.midiFileName = fileChooser.selectedFile.getPath()
	                updateStatus
				}
			})
			
			contents += new MenuItem(Action("Load mp3") { 
				val fileChooser = new FileChooser()
				if(player.fileName != null)
					fileChooser.selectedFile = new File(player.fileName)
				if(fileChooser.showOpenDialog(this) == FileChooser.Result.Approve)
				{
	                player.openFile(fileChooser.selectedFile.getPath())
	                updateStatus
				}

			})

			contents += new MenuItem(Action("Scale to loop") { 
				if(looper.isLoopDefined)
				{
					secondsPerPixel = (looper.loopEnd.get - looper.loopStart.get) / looper.bounds.width
					currentTimeInSeconds = looper.loopStart.get + looper.bounds.width/2*secondsPerPixel
					
				}

			})
		}

	}

	val trackerPanel = new BoxPanel(Orientation.Vertical) {
		background = Color.WHITE
		contents += menuBar
		contents += statusLabel
		contents += randomSettings
		contents += looper
		contents += synchronizationPoints
		contents += tracker
		menuBar.xLayoutAlignment = 0
		statusLabel.xLayoutAlignment = 0
		randomSettings.xLayoutAlignment = 0
		looper.xLayoutAlignment = 0
		synchronizationPoints.xLayoutAlignment = 0
		tracker.xLayoutAlignment = 0

		border = BorderFactory.createEmptyBorder(5,5,5,5)
	}

	val panel = new BoxPanel(Orientation.Horizontal) {
		background = Color.WHITE
		contents += neck
		contents += trackerPanel
		border = BorderFactory.createLineBorder(Color.GREEN)
	}

	def updateStatus {
		var status = "%.1f".format(timer.currentTimeInSeconds)
		
		if(player.fileName != null)
			status += " " + new File(player.fileName).getName()
				
		if(tracker.midiFileName != null)
			status += " " + new File(tracker.midiFileName).getName()
			
		statusLabel.text =  status
		
		panel.revalidate()
	}

	def top = new MainFrame {
		title = "Cello teacher"
		contents = panel
		size = new Dimension(1000,600)
	}

	panel.focusable = true
	
	listenTo(panel.keys)
	listenTo(panel.mouse.clicks)
	reactions += {
		case MouseClicked(source,point,modifiers,clicks,triggersPopup) if source == panel =>
			panel.requestFocusInWindow()
		case KeyPressed(panel, Key.BackSpace, modifiers, location) =>
			currentTimeInSeconds = 0
		case KeyPressed(panel, Key.P, modifiers, location) =>
			if(looper.isLoopDefined)
			{
				positionAfterPlayingLoopOnce = currentTimeInSeconds
				playingLoopOnce = true
				currentTimeInSeconds = looper.loopStart.get
				playing = true
			}
		case KeyPressed(panel, Key.Down, modifiers, location) =>
			timer.defaultSecondsPerTick *= 1.2
		case KeyPressed(panel, Key.Up, modifiers, location) =>
			timer.defaultSecondsPerTick *= 0.8
		case KeyPressed(panel, Key.Space, modifiers, location) =>
			playing = !playing
		case KeyPressed(panel, Key.S, modifiers, location) =>
			tracker.soundOn = !tracker.soundOn
		case KeyPressed(panel, Key.C, modifiers, location) =>
			player.closeFile
		case KeyPressed(panel, Key.PageUp, modifiers, location) =>
			if(tracker.tracks.size > 0)
				tracker.playTrack =  Math.min(tracker.tracks.size-1, tracker.playTrack + 1)
		case KeyPressed(panel, Key.PageDown, modifiers, location) =>
			tracker.playTrack = Math.max(0, tracker.playTrack - 1)
		case KeyPressed(panel, Key.L, modifiers, location) =>
			tracker.learningMode = !tracker.learningMode
		case KeyPressed(panel, Key.Left, Key.Modifier.Shift, location) =>
			secondsPerPixel /= 2
		case KeyPressed(panel, Key.Right, Key.Modifier.Shift, location) =>
			secondsPerPixel *= 2
		case KeyPressed(panel, Key.Left, modifiers, location) =>
			currentTimeInSeconds = Math.max(0f,currentTimeInSeconds-1f)
		case KeyPressed(panel, Key.Right, modifiers, location) =>
			currentTimeInSeconds = currentTimeInSeconds+1f
   		case KeyPressed(panel, Key.R, modifiers, location) =>
			if(randomFrom.selection.item.halfStepsFromA < randomTo.selection.item.halfStepsFromA)
			{
				tracker.randomNotesFrom = randomFrom.selection.item.halfStepsFromA
				tracker.randomNotesTo = randomTo.selection.item.halfStepsFromA
				tracker.randomSharps = sharps.selected
				tracker.playRandomNotes
			}
		case KeyPressed(_, Key.N, modifiers, location) =>
			if(panel.contents.contains(neck))
				panel.contents -= neck
			else
				panel.contents.insert(0,neck)
			
			panel.revalidate()
	}
	
	
	def saveState(fileName:String)
	{
		val node =
<celloScala>
	<midiFile>{tracker.midiFileName}</midiFile>
	<mp3File>{player.fileName}</mp3File>
	{timer.allCheckPoints.map(checkPoint => <checkPoint><from>{checkPoint._1}</from><to>{checkPoint._2}</to></checkPoint>)}
	<secondsPerPixel>{secondsPerPixel}</secondsPerPixel>
	<currentTimeInSeconds>{timer.currentTimeInSeconds}</currentTimeInSeconds>
	{if(!looper.loopStart.isEmpty) <loopStart>{looper.loopStart.get}</loopStart>}
	{if(!looper.loopEnd.isEmpty) <loopEnd>{looper.loopEnd.get}</loopEnd>}
</celloScala>
		
		XML.saveFull(fileName, node, "UTF8",true, null)
		stateFileName = fileName
	}
	
	
	def loadState(fileName:String)
	{
		val node = XML.loadFile(fileName)
		
		if((node \ "midiFile").text != "")
			tracker.midiFileName = (node \ "midiFile").text

		if((node \ "mp3File").text != "")
			player.openFile((node \ "mp3File").text)
	
		if(!(node \ "secondsPerPixel").isEmpty)
			secondsPerPixel = (node \ "secondsPerPixel").text.toFloat
		
		if(!(node \ "currentTimeInSeconds").isEmpty)
		{
			currentTimeInSeconds = (node \ "currentTimeInSeconds").text.toFloat
		}
		
		if(!(node \ "loopStart").isEmpty)
			looper.loopStart = Some((node \ "loopStart").text.toFloat)
		
		if(!(node \ "loopEnd").isEmpty)
			looper.loopEnd = Some((node \ "loopEnd").text.toFloat)

		timer.clearCheckPoints
		for(checkPointNode <- (node \ "checkPoint"))
			timer.addCheckPoint((checkPointNode \ "from").text.toDouble, (checkPointNode \ "to").text.toDouble)
		
		updateStatus
		
		stateFileName = fileName
	}
	
	val frequencyMeter = new FrequencyMeter() 
	
	new Thread(new Runnable {
		var lastFrequencyTime : Long = 0
		override def run(){
			while(true){
				val frequency  = frequencyMeter.getFrequency()
				
				if(frequency > 0)
				{
					lastFrequencyTime = System.currentTimeMillis()
					neck.playedFrequency = frequency

					tracker.frequency = frequency
				}else if(System.currentTimeMillis() - lastFrequencyTime > 500){
					neck.playedFrequency = 0
				}
				Thread.sleep(30)
			}
		}
	}).start();
	
      
	new Thread(new Runnable {
		override def run(){
		 	while(true){
		 		SwingUtilities.invokeLater(new Runnable() {
			        def run() {
			        	if(synchronizeToSound)
			        	{
			        		timer.currentTimeInSeconds = player.positionInSeconds
			        	}
			        		
			        	if(looper.isLoopDefined && playingLoopOnce)
		        		{
		        			if(looper.loopEnd.get < currentTimeInSeconds)
		        			{
	        					playing = false
	        					currentTimeInSeconds = positionAfterPlayingLoopOnce
		        			}
		        		}
			        	
			        	List(looper.mousePositionInTime,
			        		synchronizationPoints.mousePositionInTime).find(_ != None) match {
			        		case Some(time) => tracker.cursorPosition  = time
			        		case None => tracker.cursorPosition = None
			        	}
			        	
			        	looper.repaint()
			        	synchronizationPoints.repaint()
			        	tracker.update()
						tracker.repaint()
						neck.notes = tracker.playingNotes
						neck.repaint()
						
						updateStatus
					}
			      })
			      
				Thread.sleep(30)
			}
		}
	}).start();
	
}

