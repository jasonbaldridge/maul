package maul.topics

object MalletLda {
  
  import java.io._
  import cc.mallet.topics._
  import cc.mallet.types._
  import cc.mallet.pipe.iterator._
  import scala.collection.JavaConversions._

  def main (args: Array[String]) {
    // Parse and get the command-line options
    val opts = MalletLdaOpts(args)
    val datasetDir = new File(opts.data())
    val numTopics = opts.numTopics()

    // Get the instances
    val allInstances = new InstanceList(malletPipeline(opts.whitespaceTokenization()))
    if (datasetDir.isDirectory) {
      val allFiles = new FileIterator(
        Array(datasetDir), FileIterator.STARTING_DIRECTORIES, true)
      allInstances.addThruPipe(allFiles)
    } else {
      val instanceLines = io.Source.fromFile(datasetDir)
        .getLines
        .map(new Instance(_, "", "", ""))
      allInstances.addThruPipe(instanceLines)
    }
    
    // Compute the topics
    val topicDivFactor = math.min(numTopics,10)
    val lda = new ParallelTopicModel(numTopics, numTopics/topicDivFactor, 0.01)
    lda.printLogLikelihood = false
    lda.setTopicDisplay(200, 10)
    lda.addInstances(allInstances)
    lda.setNumThreads(opts.numThreads())
    lda.numIterations = opts.numIterations()
    lda.estimate

    val tmStringRep = lda.displayTopWords(opts.numWordsToDisplay(), opts.outputNewLines())
    if (opts.output.isDefined) {
      val filename = opts.output()
      val out = new BufferedWriter(new FileWriter(new File(filename)))
      out.write(tmStringRep)
      out.close
    } else {
      println(tmStringRep)
    }
  }
  
  def malletPipeline(whitespaceTokenization: Boolean = false) = {
    import cc.mallet.pipe._
    import cc.mallet.util.CharSequenceLexer

    val pipeList = new java.util.ArrayList[Pipe]()
    pipeList.add(new Target2Label)
    pipeList.add(new SaveDataInSource)
    pipeList.add(new Input2CharSequence(java.nio.charset.Charset.defaultCharset.displayName))

    if (whitespaceTokenization)
      pipeList.add(new CharSequence2TokenSequence(CharSequenceLexer.LEX_NONWHITESPACE_TOGETHER))
    else
      pipeList.add(new CharSequence2TokenSequence(CharSequenceLexer.LEX_ALPHA))
        
    pipeList.add(new TokenSequenceLowercase)
    pipeList.add(new TokenSequenceRemoveStopwords(false, false))
    pipeList.add(new TokenSequence2FeatureSequence)
    new SerialPipes(pipeList)
  }

}




/**
  * An object that sets up the configuration for command-line options using
  * Scallop and returns the options, ready for use.
  */
object MalletLdaOpts {

  import org.rogach.scallop._
  
  def apply(args: Array[String]) = new ScallopConf(args) {
    banner("""
For usage see below:
""")

    val help = opt[Boolean]("help", noshort = true, descr = "Show this message")
    
    val numTopics = opt[Int]("num-topics", default=Some(100), validate = (0<),
      descr="The number of topics to use in the model.")
    
    val numWordsToDisplay = opt[Int]("words-to-display", default=Some(20), validate = (0<),
      descr="The number of words per topic to show in the output.")
    
    val numIterations = opt[Int]("num-iterations", default=Some(1000), validate = (0<),
      descr="The maximum number of iterations to perform.")
    
    val numThreads = opt[Int]("num-threads", default=Some(1), validate = (0<),
      descr="The number of threadls to use.")

    val topicDisplayFreq = opt[Int]("topic-display-frequency",
      default=Some(200),
      validate = (0<),
      descr="The periodic number of iterations after which to display current topics.")

    val output = opt[String]("output",
      descr="The file to save the model. If unspecified, model is written to standard output.")

    val whitespaceTokenization = opt[Boolean]("whitespace-tokenization",
      default = Some(false),
      descr = "Just tokenize by whitespace rather than using LEX_ALPHA. This is useful if you are processing non-language data.")
    
    val outputNewLines = opt[Boolean]("output-new-lines",
      default = Some(false), noshort = true,
      descr = "When outputting the final model, use long form with one word per line, rather than one topic per line.")
    
    val data = trailArg[String]("data",
      descr="The directory containing the documents to use for computing the topic model.")
    
  }
  
}
