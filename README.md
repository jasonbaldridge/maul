maul
====

A simple codebase to provide command-line access for computing topic models with [Mallet](http://mallet.cs.umass.edu) and language models with [BerkeleyLM](https://code.google.com/p/berkeleylm/). Also, it has some data for others to try the stuff I did for [my blog post on SXSW proposal titles](http://www.peoplepattern.com/text-analytics-sxsw-proposals/) in `maul/data/sxsw`.

Note: Mallet is used as a standard dependency. The code for BerkeleyLM is included directly in this project as there is no up-to-date published version of it.

## Installation

To use it, you need to obtain and compile the code. Here's the recipe, with an attempt to be helpful if you are not an experienced Unix user.

First, clone this repository and compile the code.

```
$ git clone https://github.com/jasonbaldridge/maul.git
$ cd maul
$ ./build compile
```

If this went smoothly, you should be ready to go!

## For topic modeling on 20 newsgroups data.

First, you need some data, so go to the maul top-level and do the following:

```
$ wget http://qwone.com/~jason/20Newsgroups/20news-bydate.tar.gz
$ tar xzf 20news-bydate.tar.gz
```

This will give you the 20 Newsgroup data. We'll use the `20news-bydate-train` directory that you should have as a result of this.

To compute topics from it, boot up SBT:

```
$ ./build
```

You will now be in SBT, which can compile the (minimal) code in this repository and ensure that all the relevant classes (e.g. from Mallet) are available. Now, you can learn topics for the 20 Newsgroups data above by doing the following:

```
> run-main maul.topics.MalletLda 20news-bydate-train
```

This will show Mallet estimating the topics and end with output.

There are a number of options, which you can see by using the `--help` option with either MalletLda or FactorieLda

```
> run-main MalletLda --help
> run-main FactorieLda --help
```

Here are example commands with options:

```
> run-main maul.topics.MalletLda --num-topics 200 --num-iterations 1000 --num-threads 1 20news-bydate-train
> run-main maul.topics.FactorieLda --num-topics 200 --num-iterations 1000 --num-threads 1 --read-dirs 20news-bydate-train
```

There is also a script to try it out without SBT. Run the following command (assuming that you have compiled the project):

```
$ ./maul.sh mallet-lda --num-topics 200 --num-iterations 1000 --num-threads 1 20news-bydate-train
$ ./maul.sh factorie-lda --num-topics 200 --num-iterations 1000 --num-threads 1 --read-dirs 20news-bydate-train
```

Here is an example of some further options that (a) turn off standard tokenization (useful if you have non-language data or just want to be sure to split on whitespace), (b) output topics to a named file, (c) output the topics as one word per line (each topic is headed by it's topic index with its words on following lines), and (d) specify the number of words to display per topic.

```
$ ./maul.sh mallet-lda --num-topics 200 --num-iterations 1000 --whitespace-tokenization --output topics-20news-output.txt --output-new-lines --words-to-display 100 20news-bydate-train
```

## Getting more detailed topic model output

You can get additional information by using the `--outputdir` option, which allows you to get the top words, the overall topic proportions for the corpus, and the topic distributions for each document. Here's an example with the SXSW data (which is too small to produce informative topics, but shows how this runs).

```
$ ./maul.sh mallet-lda --num-topics 10 --num-iterations 1000 --whitespace-tokenization  --outputdir sxsw-full-output --output-document-topic-distributions --words-to-display 20 data/sxsw/example1k_statuses.txt
```

Here's what you get from that command:

```
$ ls sxsw-full-output/
document-topics.txt.gz		proportions-of-topics.txt	topic-top-words.txt		vocab.txt.gz
```

The `proportions-of-topics.txt` and `topic-top-words.txt` files give topic proportion and top-k words, respectively, for the topic id that corresponds to the row number (0-based).

```
$ paste sxsw-full-output/proportions-of-topics.txt sxsw-full-output/topic-top-words.txt
0.060355317595522026	- album artist life pretty good miss accidentally cut kids line i've peanut co-written @nyahjewel bottle" record checkout flip
0.38318325626673155	rt i'm - & it's don't great ... people love make @ day today time 2 it. feel 2013
0.06510099780968605	ha // dancing :) double point catch feed offer y'all house cool @3rdeyegirl hey pretty la latest lame. free
0.05062058895108299	dear tickets i'd group things lay huge leaving 75 $ rico. puerto desacheo framed print, archival inch 20x17 #superbowl
0.06631783889024094	loved washington people! stuff: school meet bad university strange wall more: hot @kjhabig vs. girls beautiful room yeah playing
0.060233633487466534	— today sick. draws american died glad malcolm email back weird game hear checking here. @youtube @ rt https://t.c…
0.06084205402774397	month w/ i'll brand bowl (@ super show talking @guardian concert publishing seat 9 they're goat obama anne regret
0.06254563154052081	boom 2nd pay live free, what's announces #ff add you're baby incline ♫ #soundtracking #funk end support great people
0.12679484059381846	| back photo: health we're won't #london - ted century ride live ray! remix design (at work night sweet
0.06400584083718666	& rt austin read lunch singing recession? local ways star me. love... weekend pretty finished @ world it's tonight

The file `vocab.txt.gz` simply lists all the words that are in any topic's top words.

The file `document-topics.txt.gz` gives the topic distributions for each document. The first element is the document id, and the rest is Vowpal Wabbit format items with topic-id:topic-probability entries. Any topic with less than uniform (1/num-topics) probability is discarded.

```
starling:maul jasonbaldridge$ gzcat sxsw-full-output/document-topics.txt.gz | head -5
0	0:0.010485220510416832 1:0.06409445494729427 2:0.17599010083915362 3:0.010377604247168225 4:0.011221814714589459 5:0.010617288320693221 6:0.010384568849183694 7:0.3401279360896533 8:0.35473656851476165 9:0.011964442967085698
1	1:0.4827995709501295 3:0.2268786842969476 6:0.11692109416288506 8:0.017789152740568706 9:0.11797978870846952
2	0:0.01560742139573266 1:0.09540563944252761 2:0.26196412965710336 3:0.015447232836237279 4:0.016703853858049993 5:0.015804006480972164 6:0.01545759975969883 7:0.017769692255028294 8:0.03951487940178537 9:0.5063255449128644
3	1:0.9008411344095093 2:0.01021828986791618 7:0.010255052301641867 8:0.022804399150115423 9:0.010277891884299707
4	0:0.19037477599785949 1:0.02426946559785231 4:0.25278853216337344 7:0.5015990366077482 8:0.010051869175119545
```

Document ids start from zero. If you have identifiers for each document, you can use the `--document-ids-file` option. The argument to that option should be a file with one document id per line. These should match the document on the corresponding line of the input file. Note that this only works when the input is a file (not a directory).
