/*
 *  Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*

// todo make package com.borisfarber.csviewer
class FileWatcherThread @Throws(Exception::class)
constructor(directoryName: String, private val fileName: String,
            listener:PropertyChangeListener) : Thread() {

    private val watcher: WatchService = FileSystems.getDefault().newWatchService()
    private val pcs: PropertyChangeSupport
    private var commandIndex: Int = 0

    private var command: String? = null

    init {
        val dir = Paths.get(directoryName)
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        println("Watch Service registered for dir: " + dir.fileName)
        this.pcs = PropertyChangeSupport(listener)
    }

    override fun run() {
        while (true) {
            val key: WatchKey
            try {
                key = watcher.take()

            } catch (ex: InterruptedException) {
                return
            }

            for (event in key.pollEvents()) {
                val kind = event.kind()

                val ev = event as WatchEvent<Path>
                val fileName = ev.context()

                val commandText = kind.name() + ": " + fileName
                println(commandText)

                if (fileName.endsWith(this.fileName)) {
                    if (event != ENTRY_DELETE) {
                        setCommand(commandText)
                    }
                }
            }

            val valid = key.reset()
            if (!valid) {
                break
            }
        }
    }



    private fun setCommand(command: String) {
        val old = this.command
        this.command = command + (commandIndex++)
        pcs.firePropertyChange("command", old, command)
    }

    fun addPropertyChangeListener(listener: PropertyChangeListener) {
        pcs.addPropertyChangeListener(listener)
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val directoryName = "/Users/"

            val dwd = FileWatcherThread(directoryName,
                    "User.class", PropertyChangeListener {  })
            dwd.run()
        }
    }
}