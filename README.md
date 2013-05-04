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
> run-main Maul 20news-bydate-train
```

This will show Mallet estimating the topics and end with output. It might get stuck at the end, in wich case just CTRL-C out of it and do `./build` again if you want to run things again.

There are a number of options, which should be self-explanatory:

```
> run-main Maul --help
[info] Running Maul --help

For usage see below:

  -i, --iterations  <arg>         The maximum number of iterations to perform.
                                  (default = 1000)
  -n, --num-topics  <arg>         The number of topics to use in the model.
                                  (default = 100)
  -o, --output  <arg>             The file to save the model as. If left
                                  unspecified, it will write to standard output.
                                  (default = stdout)
  -t, --threads  <arg>            The number of threadls to use. (default = 4)
  -w, --words-to-display  <arg>   The number of words per topic to show in the
                                  output. (default = 20)
      --help                      Show this message
      --version                   Show version of this program

 trailing arguments:
  data (required)   The directory containing the documents to use for computing
                    the topic model.
```

