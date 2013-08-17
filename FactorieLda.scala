import cc.factorie._
import cc.factorie.directed._
import cc.factorie.app.strings.Stopwords
import scala.collection.mutable.HashMap
import java.io.{PrintWriter, FileWriter, File, BufferedReader, InputStreamReader, FileInputStream}
import collection.mutable.{ArrayBuffer, HashSet, HashMap, LinkedHashMap}
import cc.factorie.directed._
import cc.factorie.optimize.TrainerHelpers
import java.util.concurrent.Executors
import cc.factorie.app.topics.lda._

object FactorieLda {
  import scala.collection.mutable.ArrayBuffer
  import scala.util.control.Breaks._
  import java.io.Reader
  import cc.factorie.app.strings.StringSegmenter
  
  var verbose = false

  val minDocLength = 3

  def newDocument(
    domain:CategoricalSeqDomain[String],
    name:String,
    contents:Reader,
    segmenter:StringSegmenter): Doc =
    Document.fromReader(domain, name, contents, segmenter)

  def main(args:Array[String]): Unit = {
    opts.parse(args)

    val random = new scala.util.Random(0)

    /** The domain of the words in documents */
    object WordSeqDomain extends CategoricalSeqDomain[String]

    val model = DirectedModel()

    val lda = new LDA(WordSeqDomain, opts.numTopics.value, opts.alpha.value,
      opts.beta.value, opts.optimizeBurnIn.value)(model,random)
    
    val mySegmenter = new cc.factorie.app.strings.RegexSegmenter(opts.tokenRegex.value.r)

    for {
      file <- opts.readDirs.value.map(new File(_)).flatMap(fileIterator).toIterator
      doc = Document.fromFile(WordSeqDomain, file, "UTF-8", segmenter=mySegmenter)
      if doc.length >= minDocLength
    } {
      lda.addDocument(doc, random)
      if (lda.documents.size % 1000 == 0) {
        print(" "+lda.documents.size)
        Console.flush
        if (lda.documents.size % 10000 == 0) println
      }
    }
    
    println("\nRead "+lda.documents.size+" documents, "
      + WordSeqDomain.elementDomain.size+" word types, "
      + lda.documents.map(_.ws.length).sum+" word tokens.")
    
    // Run inference to discover topics
    if (opts.numIterations.value > 0) {
      val startTime = System.currentTimeMillis

      if (opts.numThreads.value > 1)
        lda.inferTopicsMultithreaded(opts.numThreads.value, opts.numIterations.value,
          diagnosticInterval = opts.diagnostic.value,
          diagnosticShowPhrases = opts.diagnosticPhrases.value)
      else
        lda.inferTopics(opts.numIterations.value, fitAlphaInterval = opts.fitAlpha.value,
          diagnosticInterval = opts.diagnostic.value,
          diagnosticShowPhrases = opts.diagnosticPhrases.value)

      println("Finished in " + ((System.currentTimeMillis - startTime) / 1000.0) + " seconds")
    }

    println(lda.topicsSummary(opts.printTopics.value))
  }

  def fileIterator(file: File): List[File] =
    if (file.isFile)
      List(file)
    else
      file.listFiles.flatMap(fileIterator).toList

  val opts = new cc.factorie.util.DefaultCmdOptions {
    val numTopics = new CmdOption("num-topics", 't', 10, "N", "Number of topics.")
    val alpha = new CmdOption("alpha", 0.1, "N", "Dirichlet parameter for per-document topic proportions.")
    val beta = new CmdOption("beta", 0.01, "N", "Dirichlet parameter for per-topic word proportions.")
    val numThreads = new CmdOption("num-threads", 1, "N", "Number of threads for multithreaded topic inference.")
    val numIterations = new CmdOption("num-iterations", 'i', 50, "N", "Number of iterations of inference.")
    val diagnostic = new CmdOption("diagnostic-interval", 'd', 100, "N", "Number of iterations between each diagnostic printing of intermediate results.")
    val diagnosticPhrases= new CmdOption("diagnostic-phrases", false, "true|false", "If true diagnostic printing will include multi-word phrases.")
    val fitAlpha = new CmdOption("fit-alpha-interval", Int.MaxValue, "N", "Number of iterations between each re-estimation of prior on per-document topic distribution.")
    val optimizeBurnIn =new CmdOption("optimize-burn-in", 100, "N", "Number of iterations to run before the first estimation of the alpha parameters")
    val tokenRegex = new CmdOption("token-regex", "\\p{Alpha}+", "REGEX", "Regular expression for segmenting tokens.")
    val readDirs = new CmdOption("read-dirs", List(""), "DIR...", "Space-(or comma)-separated list of directories containing plain text input files.")
    val maxNumDocs = new CmdOption("max-num-docs", Int.MaxValue, "N", "The maximum number of documents to read.")
    val printTopics = new CmdOption("print-topics", 20, "N", "Just before exiting print top N words for each topic.")
    val verbose = new CmdOption("verbose", "Turn on verbose output") { override def invoke = FactorieLda.this.verbose = true }
  }

  
}
