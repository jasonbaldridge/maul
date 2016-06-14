package maul.topics

import maul.io._

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

      // If a file with document ids is provided, get the iterator for those. Otherwise,
      // get a stream starting from 0 as ids.
      val documentIds = if (opts.documentIdsFile.isDefined) {
        getSource(opts.documentIdsFile()).getLines
      } else {
        Stream.from(0).toIterator
      }

      val documents = getSource(datasetDir).getLines

      val instanceLines = for ((id,doc) <- documentIds.zip(documents)) yield {
        new Instance(doc,"",id,"")
      }

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

    // Get the standard string representation of the top words in each topic.
    val tmStringRep = lda.displayTopWords(opts.numWordsToDisplay(), opts.outputNewLines())

    if (opts.output.isDefined) {
      val out = createWriter(opts.output())
      out.write(tmStringRep)
      out.close
    } else {
      println(tmStringRep)
    }

    // Output full information with topics, topic proportions, and document-topic proportions.
    if (opts.outputdir.isDefined) {

      val outputdir = new File(opts.outputdir())
      outputdir.mkdirs

      // Output the top words per topic. The words for each topic are given on the row
      // corresponding to the 0-based topic index.
      val standardTopicOutputWriter = createWriter(outputdir,"topic-top-words.txt")
      val vocab = collection.mutable.HashSet.empty[String]
      for {
        line <- tmStringRep.split("\n")
        Array(id,_,topWords) = line.split("\t")
        words = topWords.split(" ")
      } {
        words.foreach(word => vocab += word)
        standardTopicOutputWriter.write(topWords+"\n")
      }
      standardTopicOutputWriter.close

      // Write out the words that occured in the top-k of at least one topic.
      val vocabWriter = createWriter(outputdir,"vocab.txt.gz")
      vocab.toSeq.sorted.foreach(word=>vocabWriter.write(word+"\n"))
      vocabWriter.close

      // Output the overall proportions of each topic in the corpus.
      val topicProportionsWriter = createWriter(outputdir,"proportions-of-topics.txt")
      val topicCounts = lda.tokensPerTopic
      val numTopics = topicCounts.length
      val totalCount = topicCounts.sum.toDouble
      val topicProportions = topicCounts.map(_/totalCount)
      topicProportions.foreach(p => topicProportionsWriter.write(p+"\n"))
      topicProportionsWriter.close

      // Do this if you want the topic word weights.
      //lda.printTopicWordWeights(new File(outputdir,"topic-word-weights.txt"))

      if (opts.outputDocumentTopicDistributions()) {
        // Write the document topic proportions. Begin by outputting to a tmp file with the full
        // output given by Mallet.
        val tmpDocTopicsFile = tmpFile("document-topics")
        lda.printDocumentTopics(tmpDocTopicsFile)

        // Next, read that tmp file and output only topics above uniform probability for the document,
        // and use Vowpal Wabbit format with topicId:topicProportion values.
        val documentTopicsWriter = createWriter(outputdir,"document-topics.txt.gz")
        val minTopicProbability = math.min(.01,1.0/numTopics)

        for {
          line <- getSource(tmpDocTopicsFile).getLines.drop(1)
          rowNumber :: docId :: proportions = line.split("\t").toList
        } {

          val docTopicProportions = (for {
            List(topicIdStr,topicProbStr) <- proportions.grouped(2)
            topicProb = topicProbStr.toDouble
            if topicProb > minTopicProbability
            topicId = topicIdStr.toInt
          } yield (topicId,topicProb)).toSeq

          if (docTopicProportions.length > 0) {
            documentTopicsWriter.write(docId + "\t")
            val paired = for ((topicId, topicProb) <- docTopicProportions.sortBy(_._1)) yield
            s"$topicId:$topicProb"
            documentTopicsWriter.write(paired.mkString(" ")+"\n")
          }

        }
        documentTopicsWriter.close
      }
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
