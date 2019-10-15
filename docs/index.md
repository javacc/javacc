<!---
Created from:
* the original README file at https://github.com/javacc/javacc
* the documentation https://github.com/javacc/javacc/www
* an example README.md file from https://github.com/apache/flink
-->

# <a name="top"></a>JavaCC

Java Compiler Compiler (JavaCC) is the most popular parser generator for use with Java applications.

A parser generator is a tool that reads a grammar specification and converts it to a Java program that can recognize matches to the grammar.

In addition to the parser generator itself, JavaCC provides other standard capabilities related to parser generation such as tree building (via a tool called JJTree included with JavaCC), actions and debugging.

All you need to run a JavaCC parser, once generated, is a Java Runtime Environment (JRE).

## <a name="toc"></a>Contents

- [Introduction](#introduction)
  * [Features](features.md)
  * [Tutorials](tutorials/index.md)
  * [FAQ](faq.md)
- [Getting Started](#getting-started)
  * [Download & Installation](#download)
  * [Building JavaCC from source](#building-from-source)
  * [Developing JavaCC](#developing)
- [Community](#community)
  * [Support](#support)
  * [Documentation](#documentation)
  * [Powered by JavaCC](#powered-by)
- [License](#license)

## <a name="getting-started"></a>Getting Started

Follow the steps here to get started with JavaCC.

This guide will walk you through locally building the project, running an existing example, and setup to start developing and testing your own JavaCC application.

### <a name="download"></a>Download & Installation

JavaCC 7.0.5 is our latest stable release.

* JavaCC 7.0.5 - 2019-10-14 ([Source (zip)](https://github.com/javacc/javacc/archive/7.0.5.zip), [Source (tar.gz)](https://github.com/javacc/javacc/archive/7.0.5.tar.gz), [Binaries](https://repo1.maven.org/maven2/net/java/dev/javacc/javacc/7.0.5/javacc-7.0.5.jar), [Javadocs](https://repo1.maven.org/maven2/net/java/dev/javacc/javacc/7.0.4/javacc-7.0.5-javadoc.jar), [Release Notes](release-notes.md#javacc-7.0.5))

All JavaCC releases are available via [GitHub](https://github.com/javacc/javacc/releases) and [Maven](https://mvnrepository.com/artifact/net.java.dev.javacc/javacc) including checksums and cryptographic signatures.

For all previous releases, please see [stable releases](downloads.md).

#### <a name="installation"></a>Installation

To install JavaCC, navigate to the download directory and type:

```
$ unzip javacc-7.0.5.zip
or
$ tar xvf javacc-7.0.5.tar.gz
```

Once you have completed installation add the `bin/` directory in the JavaCC installation to your `PATH`. The JavaCC, JJTree, and JJDoc invocation scripts/executables reside in this directory.

#### <a name="binary-distribution"></a>Binary distribution

The binary distributions contain the JavaCC, JJTree and JJDoc sources, launcher scripts, example grammars and documentation. It also contains a bootstrap version of JavaCC needed to build JavaCC.

On Unix-based systems, you need to make sure the files in the `bin/` directory of the distribution are in your path.

### <a name="building-from-source"></a>Building JavaCC from source

Prerequisites for building JavaCC:

* Git
* Ant (we require version 1.5.3 or above - you can get ant from [http://ant.apache.org](http://ant.apache.org))
* Maven
* Java 8 (Java 9 and 10 are not yet supported)

```
$ git clone https://github.com/javacc/javacc.git
$ cd javacc
$ ant
```

This will build the `javacc.jar` file in the `target/` directory

### <a name="developing"></a>Developing JavaCC

<!---
The JavaCC committers use IntelliJ IDEA to develop the JavaCC codebase.
-->
Minimal requirements for an IDE are:
* Support for Java
* Support for Maven with Java


#### IntelliJ IDEA

The IntelliJ IDE supports Maven out of the box and offers a plugin for JavaCC development.

* IntelliJ download: [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/)
* IntelliJ JavaCC Plugin: [https://plugins.jetbrains.com/plugin/11431-javacc/](https://plugins.jetbrains.com/plugin/11431-javacc/)

<!---
Check out our [Setting up IntelliJ](https://ci.apache.org/projects/flink/flink-docs-master/flinkDev/ide_setup.html#intellij-idea) guide for details.
-->

#### Eclipse IDE

* Eclipse download: [https://www.eclipse.org/ide/](https://www.eclipse.org/ide/)
* Eclipse JavaCC Plugin: [https://marketplace.eclipse.org/content/javacc-eclipse-plug](https://marketplace.eclipse.org/content/javacc-eclipse-plug)

## <a name="community"></a>Community

JavaCC is by far the most popular parser generator used with Java applications with an estimated user base of over 1,000 users and more than 100,000 downloads to date.

It is maintained by the [developer community](https://github.com/javacc/javacc/graphs/contributors) which includes the original authors and [Chris Ainsley](https://github.com/ainslec), [Tim Pizney](https://github.com/timp) and [Francis Andre](https://github.com/zosrothko).

### <a name="support"></a>Support

Don’t hesitate to ask!

Contact the developers and community on the [Google user group](https://groups.google.com/forum/#!forum/javacc-users) or email us at [JavaCC Support](mailto:support@javacc.org) if you need any help.

[Open an issue](https://github.com/javacc/javacc/issues) if you found a bug in JavaCC.

For questions relating to development please join our [Slack channel](https://javacc.slack.com/).

### <a name="documentation"></a>Documentation

The documentation is located on the website [https://javacc.org](https://javacc.org) and in the `docs/` directory of the source code on [Github](https://github.com/javacc/javacc).

It includes [detailed documentation](documentation/index.md) for JavaCC, JJTree, and JJDoc.

### <a name="powered-by"></a>Powered by JavaCC

JavaCC is used in many commercial applications and open source projects. The following list highlights a few notable JavaCC projects that run interesting use cases in production, with links to the relevant grammar specifications.

User                                                 | Use Case                                                       | Grammar File(s)
:--------------------------------------------------- |:-------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------:
[Apache ActiveMQ](https://activemq.apache.org/)      | Parsing JMS selector statements                                | [SelectorParser.jj](https://github.com/apache/activemq/blob/master/activemq-client/src/main/grammar/SelectorParser.jj), [HyphenatedParser.jj](https://github.com/apache/activemq-artemis/blob/master/artemis-selector/src/main/javacc/HyphenatedParser.jj)
[Apache Avro](https://avro.apache.org/)              | Parsing higher-level languages into Avro Schema                | [idl.jj](https://github.com/apache/avro/blob/master/lang/java/compiler/src/main/javacc/org/apache/avro/compiler/idl/idl.jj)
[Apache Calcite](https://calcite.apache.org/)        | Parsing SQL statements                                         | [Parser.jj](https://github.com/apache/calcite/blob/master/core/src/main/codegen/templates/Parser.jj)
[Apache Camel](https://camel.apache.org/)            | Parsing stored SQL templates                                   | [sspt.jj](https://github.com/apache/camel/blob/master/components/camel-sql/src/main/java/org/apache/camel/component/sql/stored/template/grammar/sspt.jj)
[Apache Jena](https://jena.apache.org/)              | Parsing queries written in SPARQL, ARQ, SSE, Turtle and JSON   | [sparql_10](https://github.com/apache/jena/blob/master/jena-arq/Grammar/Final/sparql_10-final.jj), [sparql_11](https://github.com/apache/jena/blob/master/jena-arq/Grammar/Final/sparql_11-final.jj), [arq.jj](https://github.com/apache/jena/blob/master/jena-arq/Grammar/arq.jj), [sse.jj](https://github.com/apache/jena/blob/master/jena-arq/Grammar/sse/sse.jj), [turtle.jj](https://github.com/apache/jena/blob/master/jena-arq/Grammar/turtle.jj), [json.jj](https://github.com/apache/jena/blob/master/jena-arq/Grammar/JSON/json.jj)
[Apache Lucene](https://lucene.apache.org/)          | Parsing search queries                                         | [QueryParser.jj](https://github.com/apache/lucene-solr/blob/master/lucene/queryparser/src/java/org/apache/lucene/queryparser/classic/QueryParser.jj)
[Apache Tomcat](https://tomcat.apache.org/)          | Parsing Expression Language (EL) and JSON                      | [ELParser.jjt](https://github.com/apache/tomcat/blob/master/java/org/apache/el/parser/ELParser.jjt), [JSONParser.jj](https://github.com/apache/tomcat/blob/master/java/org/apache/tomcat/util/json/JSONParser.jj)
[Apache Zookeeper](https://zookeeper.apache.org/)    | Optimising serialisation/deserialisation of Hadoop I/O records | [rcc.jj](https://github.com/apache/zookeeper/blob/master/zookeeper-jute/src/main/java/org/apache/jute/compiler/generated/rcc.jj)
[Java Parser](https://javaparser.org/)               | Parsing Java language files                                    | [java.jj](https://github.com/javaparser/javaparser/blob/master/javaparser-core/src/main/javacc/java.jj)

<!---
## <a name="contributing"></a>Contributing

This is an active open-source project. We are always open to people who want to use the system or contribute to it.
Contact us if you are looking for implementation tasks that fit your skills.
This article describes [how to contribute to Apache Flink](https://flink.apache.org/contributing/how-to-contribute.html).

https://blog.scottlowe.org/2015/01/27/using-fork-branch-git-workflow/

-->

## <a name="license"></a>License

JavaCC is an open source project released under the [BSD License 2.0](LICENSE). The JavaCC project was originally developed at Sun Microsystems Inc. by [Sreeni Viswanadha](https://github.com/kaikalur) and [Sriram Sankar](https://twitter.com/sankarsearch).

<br>

---

[Top](#top)

<br>
