package celloscala.midi

import java.io.File
import javax.sound.midi._
import scala.collection.immutable.TreeMap
import celloscala.Note

object MidiReader {

	def getTracks(fileName:String) : List[Track] = {
		
		var tracks = List.empty[Track]
		
		val sequence = MidiSystem.getSequence( new File( fileName ) );
		
		for(track <- sequence.getTracks()){

			var trackName = ""
			var instruments = Set.empty[Int]
			var notes = new TreeMap[Long,List[Note]]()

			var openNotes = Map.empty[Int,Note]
			
			for(i <- 0 until track.size())
			{
				val event = track.get(i)

				val message = event.getMessage()
				
				if(message.isInstanceOf[ShortMessage])
				{
					val shortmessage = message.asInstanceOf[ShortMessage]
					
					def closeNote(key:Int, tick:Long) {
						if(openNotes.contains(key)){
							val note = openNotes(key)
							note.end = tick
							if(!notes.contains(note.start))
							{
								notes = notes.insert(note.start, List.empty[Note])
							}
							notes = notes.updated(note.start, note :: notes(note.start))
							openNotes -= key
						}
					}
					
					shortmessage.getCommand match{
						case ShortMessage.NOTE_ON => 
							if(shortmessage.getData2 > 0) {
								openNotes += (shortmessage.getData1 -> new Note(shortmessage.getData1 - 69) {
																			start = event.getTick
																		})
							}else{
								closeNote(shortmessage.getData1, event.getTick)
							}
						case ShortMessage.NOTE_OFF => closeNote(shortmessage.getData1, event.getTick)
						case ShortMessage.CHANNEL_PRESSURE => 
						case ShortMessage.CONTROL_CHANGE => 
						case ShortMessage.PROGRAM_CHANGE => instruments += shortmessage.getData1
						case _ =>
					}

				}else if(message.isInstanceOf[MetaMessage])
				{
					val metamessage = message.asInstanceOf[MetaMessage]

					metamessage.getType match{
						case 3 => trackName = new String( metamessage.getData, "ISO-8859-1")
						case 4 => // instrument name
						case 47 => // "end of track " 
						case 81 => // "set tempo"
						case 84 => // "SMPTE offset"
						case 88 => // "Time Signature"
						case 89 => // "Key Signature"
						case _ => 
					}
				}
			}
			
			if(notes.size > 0)
				tracks = new Track(trackName, instruments, notes) :: tracks
		}
		
		return tracks
	}
	/*
	1 Acou Grand Piano
2 Bright Aco Piano
3 Elec Grand Piano
4 Honky-tonk Piano
5 RhodesPiano
6 ChorusPiano
7 Harpschord
8 Clavinet
9 Celesta
10 Glockenspiel
11 Music Box
12 Vibraphone
13 Marimba
14 Xylophone
15 TubularBells
16 Dulcimer
17 Hamnd Organ
18 Perc Organ
19 Rock Organ
20 ChurchOrgan
21 Reed Organ
22 Accordion
23 Harmonica
24 Tango Acordn
25 Nylon Guitar
26 SteelStrGuitar
27 Jazz Guitar
28 CleanE.Guitar
29 Mute E.Guitar
30 OvrdrivGuitar
31 DistortGuitar
32 Harmonics
33 Acou Bass
34 FingerE.Bass
35 PickedE.Bass
36 FretlesBass
37 Slap Bass 1
38 Slap Bass 2
39 Synth Bass1
40 Synth Bass2
41 Violin
42 Viola
43 Cello
44 Contrabass
45 Trem Strings
46 Pizz Strings
47 OrchHarp
48 Timpani
49 Str Ensmb 1
50 Str Ensmb 2
51 Synth Str 1
52 Synth Str 2
53 Choir Aahs
54 Voice Oohs
55 Synth Voice
56 Orchestra Hit
57 Trumpet
58 Trombone
59 Tuba
60 Mute Trumpet
61 French Horn
62 Brass Section
63 SynthBrass1
64 SynthBrass2
65 SopranoSax
66 Alto Sax
67 Tenor Sax
68 Bari Sax
69 Oboe
70 EnglshHorn
71 Bassoon
72 Clarinet
73 Piccolo
74 Flute
75 Recorder
76 Pan Flute
77 BottleBlow
78 Shakuhachi
79 Whistle
80 Ocarina
81 Square Wave
82 SawTooth
83 Caliope
84 Chiff Lead
85 Charang
86 SoloSynthVox
87 Brite Saw
88 Brass&Lead
89 FantasaPad
90 Warm Pad
91 Poly Synth Pad
92 Space Vox Pad
93 Bow Glass Pad
94 Metal Pad
95 Halo Pad
96 Sweep Pad
97 Ice Rain
98 SoundTrack
99 Crystal
100 Atmosphere
101 Brightness
102 Goblin
103 Echo Drops
104 Star Theme
105 Sitar
106 Banjo
107 Shamisen
108 Koto
109 Kalimba
110 Bag Pipe
111 Fiddle
112 Shanai
113 Tinkle Bell
114 Agogo
115 Steel Drums
116 Woodblock
117 Taiko Drum
118 Melodic Tom
119 Synth Drum
120 RevrsCymbal
121 GtrFretNoise
122 BreathNoise
123 Sea Shore
124 Bird Tweet
125 Telephone Ring
126 Helicopter
127 Applause
128 Gun Shot */
}