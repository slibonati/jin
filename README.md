Jin
===

Jin is a graphical chess client for the [Free Internet Chess Service](http://freechess.org/) (FICS). It was written by [Alexander Maryanovsky](mailto:msasha@gmail.com) and has its [own website](http://jinchess.com). The main development trunk for Jin is hosted on [Sourceforge](http://sourceforge.net/projects/jin/). That project has been dormant for some years.

This GitHub repository is a fork of the Jin project designed to fix bugs and add features that are missing from Jin.

The instructions that follow are primarily intended for Java programmers who are interested in hacking on Jin.

Getting Jin
===========

1. Install `git`. If you need help, you can follow [these instructions](http://git-scm.com/book/en/Getting-Started-Installing-Git).
2. Clone this repository: `git clone https://github.com/sjamesr/jin.git`.

You will now have a directory called `jin` in your current working directory.

Building Jin and running Jin
============================

This fork of Jin uses [Apache Maven](http://maven.apache.org/) as its build system. So,

1. Install Maven. If you need help, you can follow [these instructions](http://maven.apache.org/download.cgi#Installation).
2. Build Jin. Run the following in your `jin` directory: `mvn package`
3. Run Jin. `MAVEN_OPTS=-Djava.library.path=target/natives mvn exec:java -Dexec.mainClass=free.jin.JinApplication`
