maul
====

A simple command-line access to computing topic models with Mallet. Well, not quite command line, but pretty much, if you don't mind doing it through SBT as specified below.

## How to use it

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
> run-main MalletLda 20news-bydate-train
```
  
This will show Mallet estimating the topics and end with output.

There are a number of options, which you can see by using the `--help` option with either MalletLda or FactorieLda

```
> run-main MalletLda --help
> run-main FactorieLda --help
```


## Completely non-rigorous time comparison

I was interested in see how topic inference time varied between Mallet and Factorie for the same data. To compare this, I looked at the following configuration for each and varied the number of threads over 1, 2, 4, and 8.

```
> run-main MalletLda --num-topics 200 --num-iterations 1000 --num-threads 1 20news-bydate-train
> run-main FactorieLda --num-topics 200 --num-iterations 1000 --num-threads 1 --read-dirs 20news-bydate-train
```

The time reported was based on getting the system time around the inference call, which should be sufficient to get a feel for the difference. 

On a Linux box with a AMD Phenom(tm) II X4 960T Processor and 16GB memory:

| System        |   1   |   2   |   4   |   8   |
| ------------- | -----:| -----:| -----:| -----:|
| Mallet        |  360  |  306  |  224  |  253  |
| Factorie      |  388  |  549  | 1022  | 1102  |

So, it is roughly comparable for the single thread case, but then Mallet gets better while Factorie gets worse. (It's entirely possible that I've set up something wrong for Factorie.)