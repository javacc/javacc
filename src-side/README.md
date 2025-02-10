# src-side-gram: side grammars

In this directory, which follows the standard Maven structure, lie grammars that have been used to build the JavaCC / JJTree / JJDOC grammars, for the moment grammars for the Java language, derived from the Java Language Specification (JLS).

These grammars are not (yet) part of the project's automated build process; the JavaCC / JJTree / JJDOC grammars have been manually written from them.

These grammars are built through the `build-side-gram.xml` ant script.

The `build-on-gvm.xml` ant script allows to build a native image with GraalVM and test it.
