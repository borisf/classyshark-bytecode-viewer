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

package filewatch;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

// todo make package com.borisfarber.csviewer
public class DirectoryWatcherThread extends Thread {

    private final WatchService watcher;
    private PropertyChangeSupport pcs;

    private String command;

    public DirectoryWatcherThread(String directoryName, String fileName, Object listener) throws Exception {

        // TODO handle file name

        watcher = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get(directoryName);
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        System.out.println("Watch Service registered for dir: " + dir.getFileName());

        this.pcs = new PropertyChangeSupport(listener);
    }

    @Override
    public void run() {
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                String commandText = kind.name() + ": " + fileName;
                System.out.println(commandText);

                // TODO bug with file names src file is not mapped
                // TODO to the class file
                //if(fileName.equals(this.fileName)) {
                    setCommand(commandText);
                //}
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    private void setCommand(String command) {
        String old = this.command;
        this.command = command;
        pcs.firePropertyChange("command", old, command);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public static void main(String[] args) throws Exception {
        String directoryName = "/Users/";

        DirectoryWatcherThread dwd = new DirectoryWatcherThread(directoryName, "User.class", new Object());
        dwd.run();
    }
}