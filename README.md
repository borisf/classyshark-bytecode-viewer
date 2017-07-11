# Classyshark Bytecode Viewer

![alt text](https://github.com/borisf/classyshark-bytecode-viewer/blob/master/img/CS%20Viewer.png)

## Why
Instantaneously assess performance impact of my Kotlin code on a [class](https://en.wikipedia.org/wiki/Java_class_file) level.

## How
The most accurate and measurable way is to look at Kotlin generated executable (.class) files, the same files that that both JVM and Android DX tool see. 

## What
From every kotlin-compiler generated class file the user sees side by side comparison of:

1. Equivalent Java code
2. Raw class file internals
 
The user (mind) flow will be as follows:
1. From Kotlin code to Java code
2. From Java code to Java bytecode (class format)
 
Here is the tricky part, instead of doing source to source translation from Kotlin to Java, it is 
better (faster and more accurate) to decompile Kotlin generated class file right into Java.
 
To support the above the following 2 libraries are the best fit
* [Procyon](https://bitbucket.org/mstrobel/procyon/wiki/Java%20Decompiler)- an open source Java decompiler.
* [ASM](http://asm.ow2.org/) - the best Java bytecode reading library (used both by Kotlin and Android Studio).

