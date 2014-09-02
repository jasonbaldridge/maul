package maul.lm

import collection.JavaConversions._
import edu.berkeley.nlp.lm.io.ComputeLogProbabilityOfTextStream._

object OutputAverageLogProbabilityPerLine {

  def main(args: Array[String]) {
    val Array(binaryFile, textFile) = args
    val lm = readBinary(null, binaryFile)

    for {
      line <- io.Source.fromFile(textFile).getLines
      words = line.trim.split("\\s+").toList
    } {
      println(lm.scoreSentence(words)/words.length)
    }
     
  }

}
