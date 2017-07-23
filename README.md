# Classyshark Bytecode Viewer

![alt text](https://github.com/borisf/classyshark-bytecode-viewer/blob/master/img/CS%20Viewer.png)

## Download & Run
To run, grab the [latest JAR](https://github.com/borisf/classyshark-bytecode-viewer/releases)
and run `java -jar ClassySharkBytecodeViewer.jar`. Optionally you can add a class file to open.

## Why
Instantaneously assess performance impact of your Kotlin code on a [class](https://en.wikipedia.org/wiki/Java_class_file) level.

## How
The most accurate and measurable way is to look at Kotlin generated executable (.class) files, the same files that both JVM and Android DX tool see. 

## What
From every kotlin-compiler generated class file you will see side by side comparison of:

1. Equivalent Java code
2. Raw class file internals
 
The (mind) flow will be as follows:
1. From Kotlin code to Java code
2. From Java code to Java bytecode (class format)
 
Here is the tricky part, instead of doing source to source translation from Kotlin to Java, it is 
better (faster and accurate) to decompile Kotlin generated class file right into Java.
 
To support the above the following 2 libraries are the best fit
* [Procyon](https://bitbucket.org/mstrobel/procyon/wiki/Java%20Decompiler)- an open source Java decompiler.
* [ASM](http://asm.ow2.org/) - the best Java bytecode reading library (used both by Kotlin and Android Studio).

This is not an official Google product.

### License

```
Copyright 2017 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.
```
