> Copyright (c) 2006, Sun Microsystems, Inc.
> All rights reserved.
> 
> Redistribution and use in source and binary forms, with or without
> modification, are permitted provided that the following conditions are met:
> 
>    * Redistributions of source code must retain the above copyright notice,
>      this list of conditions and the following disclaimer.
>    * Redistributions in binary form must reproduce the above copyright
>      notice, this list of conditions and the following disclaimer in the
>      documentation and/or other materials provided with the distribution.
>    * Neither the name of the Sun Microsystems, Inc. nor the names of its
>      contributors may be used to endorse or promote products derived from
>      this software without specific prior written permission.
> 
> THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
> AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
> IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
> ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
> LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
> CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
> SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
> INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
> CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
> ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
> THE POSSIBILITY OF SUCH DAMAGE.
> 

# Examples grammars

In this directory rest a bunch of (quite old) example grammars.

(Note that for the moment they are also part of the project's build process.)

We recommend you see the examples in the following order.  
Each directory contains a **README.md** file with more detailed instructions

## a_simple_examples
This is a set of very simple JavaCC examples.  
After trying these examples out, you should be able to build reasonably complex examples yourself.

## b_mail_processing
These examples illustrate the use of JavaCC in parsing emacs mail files.  
They highlight the use of lexical states.

## c_jjtree_examples
There are simple input grammars for JJTree, the tree building preprocessor.

## d_corba_idl
This is a grammar for the IDL interface definition language of OMG CORBA 2.0.

## e_java_grammars
This directory contains old Java grammars.

## f_javacc_grammar
This directory contains an old JavaCC grammar.  
This is the version of the grammar on which an old JavaCC was built.

## g_transformer
This example illustrates how a Java language extension is defined and transformed back into Java.  
This makes use of JJTree.

## h_gui_parsing
These examples illustrate how one may obtain input for parsing or lexical analysis from a GUI, and thereby provide "parsing" of GUI interactions.  
In addition, one of these examples illustrates how state machines may be described as lexical specifications.

## i_obfuscator
This is a complete implementation of a Java obfuscator.  
This allows one to take a set of Java source files and modify them into semantically equivalent source files that are much more difficult to read.  
This example shows the usage of multiple parsers used from one system.

## j_interpreter
This is an interpreter for a simple language with declarations, assignments, expressions, conditionals, loops, etc.  
It demonstrates really nicely a complex example where JJTree is used to drastically simplify the process of generating parse trees.  
All actions are really methods built into the generated tree nodes.

## k_lookahead
This directory contains the tutorial on LOOKAHEAD along with all examples used in the tutorial.

## l-VTransformer
This directory contains an example of using the Visitor design pattern with JJTree.  
Like the Transformer example, it shows how a Java program can be processed into a slightly different form.
