maul
====

A simple command-line access codebase for computing topic models with Mallet and language models with BerkeleyLM.

## Installation
  
To use it, you need to obtain and compile the code. Here's the recipe, with an attempt to be helpful if you are not an experience Unix user.

First, download the code and put the file on your Desktop (adjust as you like if you are experienced with Unix). Then, run the following commands.
  
```
$ tar xzf maul-xyz.tgz
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
