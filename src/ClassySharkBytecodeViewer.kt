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

import com.strobel.decompiler.DecompilerDriver
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.*
import java.util.prefs.Preferences
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter



class ClassySharkBytecodeViewer: JFrame(), PropertyChangeListener {
    
    private var loadedFile: File
    private var loadedFileWatcherThread: FileWatcherThread
    private val searchText: JTextField
    private var javaArea: JTextPane
    private var asmArea: JTextPane
    private var ASM: String = ""
    private val panelTitle = "ClassyShark Byte Code Viewer"
    private val RESULT_AREAS_BACKGROUND = Color(46, 48, 50)
    private val INPUT_AREA_BACKGROUND = Color(88, 110, 117)

    object IntroTextHolder {
        @JvmStatic val INTRO_TEXT = "\n\n\n\n\n\n\n\n\n\n" +
                "       Drag your class file over here ....\n" +
                "\n\n\n\n\n       ClassyShark ByteCode Viewer ver." +
                Version.MAJOR + "." + Version.MINOR
    }
    
    init {
        title = panelTitle
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        preferredSize = Dimension(1230, 650)

        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        val tabbedPane = JTabbedPane()

        val openButton = JButton(createImageIcon("ic_open.png", "Search"))
        openButton.addActionListener { openButtonPressed() }
        searchText = JTextField()
        searchText.font = Font("Menlo", Font.PLAIN, 18)
        searchText.background = INPUT_AREA_BACKGROUND
        searchText.foreground = Color.CYAN
        val searchIcon = JLabel(createImageIcon("ic_magnify.png", "Search"))

        val toolbar = JPanel(BorderLayout())
        toolbar.add(openButton, BorderLayout.WEST)
        toolbar.add(searchText, BorderLayout.CENTER)
        toolbar.add(searchIcon, BorderLayout.EAST)
        mainPanel.add(toolbar)

        val resultPanel = JPanel()
        resultPanel.layout = BoxLayout(resultPanel, BoxLayout.X_AXIS)
        javaArea = SyntaxPane()
        javaArea.font = Font("Menlo", Font.PLAIN, 18)

        javaArea.text =
                IntroTextHolder.INTRO_TEXT
        javaArea.background = RESULT_AREAS_BACKGROUND
        javaArea.foreground = Color.CYAN
        javaArea.transferHandler = FileTransferHandler(this)
        javaArea.preferredSize = Dimension(830, 250)
        val javaScrollPane = JScrollPane(javaArea)

        tabbedPane.addTab("Java code", null, javaScrollPane,
                "Java sources")

        asmArea = SyntaxPane()
        asmArea.font = Font("Menlo", Font.PLAIN, 18)
        asmArea.transferHandler = FileTransferHandler(this)
        asmArea.background = RESULT_AREAS_BACKGROUND
        asmArea.foreground = Color.CYAN
        asmArea.text = SharkBG.SHARKEY
        val asmScrollPane = JScrollPane(asmArea)
        tabbedPane.addTab("Java bytecode", null, asmScrollPane,
                "Java bytecode")
        resultPanel.add(tabbedPane)
        mainPanel.add(resultPanel)

        val asmSearch = IncrementalSearch(asmArea)
        val javaSearch = IncrementalSearch(javaArea)
        searchText.document.addDocumentListener(asmSearch)
        searchText.addActionListener(asmSearch)
        searchText.document.addDocumentListener(javaSearch)
        searchText.addActionListener(javaSearch)

        contentPane = mainPanel
        pack()
        setLocationRelativeTo(null)

        loadedFile = File("")
        loadedFileWatcherThread = FileWatcherThread("", "", this)
    }

    override fun propertyChange(evt: PropertyChangeEvent) {
        if (evt.propertyName == "command") {
            // Received new command (outside EDT)
            SwingUtilities.invokeLater {
                // Updating GUI inside EDT
                onFileRecompiled()
            }
        }
    }

    fun onFileLoaded(file: File) {
        try {
            this.loadedFile = file
            title = panelTitle + " - " + file.name

            fillJavaArea(file)
            fillAsmArea(file)

            watchLoadedFileChanges(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val LAST_USED_FOLDER: String = "ClassySharkBytecodeViewer"

    private fun openButtonPressed() {

        val prefs = Preferences.userRoot().node("ClassySharkBytecodeViewer")

        val fc = JFileChooser(prefs.get(LAST_USED_FOLDER,
                 File(".").absolutePath))
        fc.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES

        val filter = FileNameExtensionFilter("classes", "class")
        fc.fileFilter = filter
        fc.addChoosableFileFilter(filter)

        val retValue = fc.showOpenDialog(JPanel())
        if (retValue == JFileChooser.APPROVE_OPTION) {
            onFileLoaded(fc.selectedFile)

            prefs.put(LAST_USED_FOLDER, fc.selectedFile.parent)
        }
    }

    private fun onFileRecompiled() {
        try {
            title = panelTitle + " - " + loadedFile.name
            searchText.text = ""
            fillJavaArea(loadedFile)
            fillAsmArea(loadedFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun watchLoadedFileChanges(file: File) {

        if(loadedFileWatcherThread.isAlive) {
            loadedFileWatcherThread.interrupt()
        }

        loadedFileWatcherThread = FileWatcherThread(file.parent, file.name, this)
        loadedFileWatcherThread.start()

        // the line below must be there otherwise threading + file system events
        // don't work possibly after the thread had started
        loadedFileWatcherThread.addPropertyChangeListener(this)
    }

    private fun fillAsmArea(file: File) {
        javaArea.caretPosition = 0
        val inputStream = FileInputStream(file)
        val reader = ClassReader(inputStream)
        val asmCode = StringWriter()
        val visitor = TraceClassVisitor(PrintWriter(asmCode))
        reader.accept(visitor, ClassReader.EXPAND_FRAMES)
        asmArea.text = asmCode.toString()
        ASM = asmCode.toString()
        asmArea.caretPosition = 0
    }

    private fun fillJavaArea(file: File) {
        // // Start capturing
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer))

        // Run what is supposed to output something
        DecompilerDriver.main(arrayOf(file.absolutePath))

        // Stop capturing
        System.setOut(PrintStream(FileOutputStream(FileDescriptor.out)))

        // Use captured content
        val content = buffer.toString()
        buffer.reset()

        javaArea.text = content
    }

    private fun createImageIcon(path: String,
                                description: String): ImageIcon? {
        val imgURL = javaClass.getResource(path)
        return if (imgURL != null) {
            ImageIcon(imgURL, description)
        } else {
            System.err.println("Couldn't find file: " + path)
            null
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SwingUtilities.invokeLater {
                try {
                    val bytecodeViewer = ClassySharkBytecodeViewer()

                    if (args.size == 1) {
                        bytecodeViewer.onFileLoaded(File(args[0]))
                    } else if (args.size > 1) {
                        System.out.println("Usage: java -jar ClassySharkBytecodeViewer.jar <path to .class file>")
                    }

                    bytecodeViewer.isVisible = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}