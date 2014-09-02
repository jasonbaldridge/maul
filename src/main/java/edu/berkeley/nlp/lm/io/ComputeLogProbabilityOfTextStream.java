package edu.berkeley.nlp.lm.io;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.collections.Iterators;
import edu.berkeley.nlp.lm.util.Logger;

/**
 * Computes the log probability of a list of files. With the <code>-g</code>
 * option, it interprets the next two arguments as a <code>vocab_cs.gz</code>
 * file (see {@link LmReaders} for more detail) and a Berkeley LM binary,
 * respectively. Without <code>-g</code>, it interprets the next file as a
 * Berkeley LM binary. All remaining files are treated as plain-text (possibly
 * gzipped) files which have one sentence per line; a dash is used to indicate
 * that text should from standard input. If no files are given, reads from
 * standard input.
 *
 * @author adampauls
 */
public class ComputeLogProbabilityOfTextStream {
    private static PrintStream writer = null;

    private static void usage() {
        System.err.println("Usage: [-o output_file] [-g <vocab_cs file>] <Berkeley LM binary file> <input_file>*\n" +
                "-s prints per sentence probability");
        System.exit(1);
    }

    public static void main(final String[] argv) throws IOException {
        int i = 0;
        if (i >= argv.length) usage();
        String vocabFile = null;
        if (argv[i].equals("-o")) {
            if (++i >= argv.length) usage();
            writer = new PrintStream(new File(argv[i++]));
        }
        if (argv[i].equals("-g")) {
            if (++i >= argv.length) usage();
            vocabFile = argv[i++];
        }
        if (i >= argv.length) usage();
        String binaryFile = argv[i++];
        List<String> files = Arrays.asList(Arrays.copyOfRange(argv, i, argv.length));
        if (files.isEmpty()) files = Collections.singletonList("-");
        Logger.setGlobalLogger(new Logger.SystemLogger(System.out, System.err));
        NgramLanguageModel<String> lm = readBinary(vocabFile, binaryFile);
        double prob = computeProb(files, lm);
        System.out.println(String.format("Normalized Log probability of text is: %f", prob));
        if (writer != null) {
            writer.close();
        }
    }

    /**
     * @param files
     * @param lm
     * @throws IOException
     */
    public static double computeProb(List<String> files, NgramLanguageModel<String> lm) throws IOException {
        double logProb = 0.0;
        long wordCount = 0;
        for (String file : files) {
            Logger.startTrack("Scoring file " + file + "; current log probability is " + logProb);
            final InputStream is = (file.equals("-")) ? System.in : (file.endsWith(".gz") ? new GZIPInputStream(new FileInputStream(file))
                    : new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));
            for (String line : Iterators.able(IOUtils.lineIterator(reader))) {
                List<String> words = Arrays.asList(line.trim().split("\\s+"));
                double sentenceProb = lm.scoreSentence(words);
                if (writer != null)
                    writer.println(String.format("%f;%f", sentenceProb, sentenceProb / words.size()));
                logProb += sentenceProb;
                wordCount +=  words.size();
            }
            Logger.endTrack();
        }
        if (writer != null)
            writer.println(String.format("%f;%f", logProb, logProb / wordCount));
        return logProb/wordCount;
    }

    /**
     * @param vocabFile
     * @param binaryFile
     * @return
     */
    public static NgramLanguageModel<String> readBinary(String vocabFile, String binaryFile) {
        NgramLanguageModel<String> lm;
        if (vocabFile != null) {
            Logger.startTrack("Reading Google Binary " + binaryFile + " with vocab " + vocabFile);
            lm = LmReaders.readGoogleLmBinary(binaryFile, vocabFile);
            Logger.endTrack();
        } else {
            Logger.startTrack("Reading LM Binary " + binaryFile);
            lm = LmReaders.readLmBinary(binaryFile);
            Logger.endTrack();
        }
        return lm;
    }

}
