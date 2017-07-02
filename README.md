# classyshark-bytecode-viewer

![alt text](https://github.com/borisf/classyshark-bytecode-viewer/blob/master/img/CS%20Viewer.png)

## Why
Instantaneously assess performance impact of my Kotlin code on a class level
## How
The most accurate and measurable way is to look at Kotlin generated executable (.class) files, the same files that that both JVM and Android DX tool see. Refer to Appendix 1 for more info about Koltin compilation. 
## What
From every kotlin-compiler generated class file the user sees side by side comparison of

1. Equivalent Java code
2. Raw class file internals
 
The user (mind) flow will be as follows:
1. From Kotlin code to Java code
2. From Java code to Java bytecode (class format)
 
Here is the tricky part, instead of doing source to source translation from Kotlin to Java (section 1 above), it is 
better (faster and more accurate) to decompile Koltin generated class file into Java.
 
To support the above the following 2 libraries are the best fit
* Procyon (Apache 2) - open source Java decompiler. It is not the most famous, but pretty good and fast
* ASM (Apache 2) - best Java bytecode reading library (used both by Kotlin and Android Studio)
Current Stat
