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

import java.awt.*
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter
import javax.swing.text.JTextComponent

class HexPanel @JvmOverloads
constructor(bytes: ByteBuffer, private val bytesPerLine: Int = DEFAULT_BYTES_PER_LINE) :
        JPanel(BorderLayout()), CaretListener {
    private val offsetView: JTextComponent
    val hexView: JTextComponent
    val asciiView: JTextComponent
    private val statusLabel: JLabel
    private val highlightColor: Color
    private val highlighterPainter: DefaultHighlighter.DefaultHighlightPainter
    private var hexLastSelectionStart = 0
    private var hexLastSelectionEnd = 0
    private var asciiLastSelectionStart = 0
    private var asciiLastSelectionEnd = 0

    constructor(file: File) : this(ByteBuffer.allocate(0)) {
        fillFromFile(file)
    }

    init {

        val font = Font("Menlo", Font.PLAIN, 18)

        offsetView = JTextArea()
        hexView = JTextArea()
        asciiView = JTextArea()
        val statusView = JPanel()

        offsetView.setBackground(BACKGROUND)
        hexView.setBackground(BACKGROUND)
        asciiView.setBackground(BACKGROUND)

        offsetView.setForeground(Color.CYAN)
        hexView.setForeground(Color.CYAN)
        asciiView.setForeground(Color.CYAN)

        statusView.border = BevelBorder(BevelBorder.LOWERED)
        add(statusView, BorderLayout.SOUTH)
        statusView.preferredSize = Dimension(this.width, 18)
        statusView.layout = BoxLayout(statusView, BoxLayout.X_AXIS)
        statusLabel = JLabel("")
        statusLabel.horizontalAlignment = SwingConstants.LEFT
        statusView.add(this.statusLabel)

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.hexView,
                this.asciiView)
        splitPane.resizeWeight = 0.5
        splitPane.isOneTouchExpandable = true
        splitPane.isContinuousLayout = true

        val panes = JPanel(BorderLayout())
        panes.add(this.offsetView, BorderLayout.WEST)
        panes.add(splitPane, BorderLayout.CENTER)
        val scroller = JScrollPane(panes)
        add(scroller, BorderLayout.CENTER)

        offsetView.setFont(font)
        hexView.setFont(font)
        asciiView.setFont(font)

        fillFromByteBuffer(bytes)

        hexView.addCaretListener(this)
        asciiView.addCaretListener(this)

        asciiView.setSelectedTextColor(asciiView.getForeground())
        hexView.setSelectedTextColor(asciiView.getForeground())
        highlightColor = hexView.getSelectionColor()
        highlighterPainter = DefaultHighlighter.DefaultHighlightPainter(highlightColor)
    }

    override fun caretUpdate(e: CaretEvent) {
        if (e.mark == e.dot) {
            this.clearHighlight()
        }

        when {
            e.source === this.asciiView -> {
                var startByte = e.mark
                var endByte = e.dot

                if (startByte > endByte) {
                    val t = endByte
                    endByte = startByte
                    startByte = t
                }

                // the number of line endings before the start,end points
                val startRows = (startByte - startByte % this.bytesPerLine) / this.bytesPerLine
                val endRows = (endByte - endByte % this.bytesPerLine) / this.bytesPerLine

                // the byte index of the start,end points in the ASCII view
                startByte -= startRows
                endByte -= endRows

                // avoid the loop
                if (asciiLastSelectionStart == startByte && asciiLastSelectionEnd == endByte) {
                    return
                }
                asciiLastSelectionStart = startByte
                asciiLastSelectionEnd = endByte

                this.setSelection(startByte, endByte)
            }
            e.source === this.hexView -> {
                var startByte = e.mark
                var endByte = e.dot

                if (startByte > endByte) {
                    val t = endByte
                    endByte = startByte
                    startByte = t
                }

                // the number of line endings before the start,end points
                val startRows = (startByte - startByte % bytesPerLine) / (3 * bytesPerLine)
                val endRows = (endByte - endByte % bytesPerLine) / (3 * bytesPerLine)

                // the byte index of the start,end points in the ASCII view
                startByte -= startRows
                startByte /= 3
                endByte -= endRows
                endByte /= 3

                if (hexLastSelectionStart == startByte && hexLastSelectionEnd == endByte) {
                    return
                }
                hexLastSelectionStart = startByte
                hexLastSelectionEnd = endByte

                setSelection(startByte, endByte)
            }
            else -> println("from unknown")
        }
    }

    fun fillFromFile(classFile: File) {

        if(!classFile.exists()) {
            return
        }

        try {
            val aFile = RandomAccessFile(classFile, "r")
            val inChannel = aFile.channel
            val fileSize = inChannel.size()
            val buffer = ByteBuffer.allocate(fileSize.toInt())
            inChannel.read(buffer)
            //buffer.rewind();
            buffer.flip()

            inChannel.close()
            aFile.close()

            fillFromByteBuffer(buffer)

            asciiView.caretPosition = 0
            hexView.caretPosition = 0
            offsetView.caretPosition = 0
        } catch (e: Exception) {

        }
    }

    private fun fillFromByteBuffer(bytes: ByteBuffer) {
        val offsetText = StringBuilder()
        val hexText = StringBuilder()
        val asciiText = StringBuilder()

        bytes.position(0x0)
        for (i in 0 until bytes.limit()) {
            ByteUtils.fillByteToTexts(bytes, offsetText, hexText, asciiText, i, bytesPerLine)
        }

        offsetView.text = offsetText.toString()
        hexView.text = hexText.toString()
        asciiView.text = asciiText.toString()
    }

    private fun clearHighlight() {
        asciiView.highlighter.removeAllHighlights()
        hexView.highlighter.removeAllHighlights()
    }

    private fun setHighlight(startByte: Int, endByte: Int) {
        val startRows = (startByte - startByte % this.bytesPerLine) / this.bytesPerLine
        val endRows = (endByte - endByte % this.bytesPerLine) / this.bytesPerLine

        this.clearHighlight()

        try {
            this.asciiView.highlighter.addHighlight(startByte + startRows,
                    endByte + endRows, this.highlighterPainter)
            this.hexView.highlighter.addHighlight(startByte * 3 + startRows,
                    endByte * 3 + endRows, this.highlighterPainter)
        } catch (e1: BadLocationException) {
            println("bad location")
        }
    }

    private fun setSelection(startByte: Int, endByte: Int) {
        this.setHighlight(startByte, endByte)

        if (startByte != endByte) {
            val statusTemplate = "Selection: %1\$d to %2\$d (len: %3\$d) [0x%1\$x to 0x%2\$x (len: 0x%3\$x)]"
            this.statusLabel.text = String.format(statusTemplate, startByte, endByte, endByte - startByte)
        } else {
            val statusTemplate = "Position: %1\$d [0x%1\$x]"
            this.statusLabel.text = String.format(statusTemplate, startByte)
        }
    }

    companion object {
        private val DEFAULT_BYTES_PER_LINE = 16
        private val BACKGROUND = Color(46, 48, 50)

        @JvmStatic
        fun main(args: Array<String>) {
            EventQueue.invokeLater {
                try {
                    val testFile = File(System.getProperty("user.home") +
                            "/Development/" + "classyshark-bytecode-viewer/out/" +
                            "production/classyshark-bytecode-viewer/SyntaxPane.class")

                    val hexPanel = HexPanel(testFile)
                    val testFrame = JFrame()
                    testFrame.add(hexPanel)
                    testFrame.isVisible = true
                } catch (e: Exception) {
                }
            }
        }
    }
}