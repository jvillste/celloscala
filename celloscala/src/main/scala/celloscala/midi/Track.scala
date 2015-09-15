package celloscala.midi

import scala.collection.immutable.TreeMap
import celloscala.Note

class Track(val name:String, val instruments:Set[Int], val notes:TreeMap[Long,List[Note]])
