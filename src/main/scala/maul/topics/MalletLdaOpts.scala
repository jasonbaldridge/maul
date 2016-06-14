package maul.topics

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
      descr="The file to save basic model information (top words per topic).")

    val outputdir = opt[String]("outputdir",
      descr="The directory to save the model output, including topic proportions, topic-word distributions, and document topic distributions.")

    val outputDocumentTopicDistributions = opt[Boolean]("output-document-topic-distributions",
        default = Some(false),
        descr = "Output the topic distributions for each document. Only shows topics above uniform probability.")

    val whitespaceTokenization = opt[Boolean]("whitespace-tokenization",
      default = Some(false),
      descr = "Just tokenize by whitespace rather than using LEX_ALPHA. This is useful if you are processing non-language data.")

    val outputNewLines = opt[Boolean]("output-new-lines",
      default = Some(false), noshort = true,
      descr = "When outputting the final model, use long form with one word per line, rather than one topic per line.")

    val documentIdsFile = opt[String]("document-ids-file",
      descr="The file containing the ids of the documents. Only works if input data is a file (rather than a directory).")

    val data = trailArg[String]("data",
      descr="The directory containing the documents to use for computing the topic model.")

  }

}
