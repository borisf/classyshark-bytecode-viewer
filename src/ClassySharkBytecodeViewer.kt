
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.io.*
import javax.swing.*

class ClassySharkBytecodeViewer @Throws(Exception::class)
constructor() : JFrame() {

    internal var textArea: JTextArea

    init {
        val cp = JPanel(BorderLayout())

        textArea = JTextArea(30, 90)
        textArea.font = Font("Menlo", Font.PLAIN, 18)
        textArea.text = SharkBG.SHARKEY

        textArea.background = Color.BLACK
        textArea.foreground = Color.CYAN
        textArea.transferHandler = FileTransferHandler(this)

        val sp = JScrollPane(textArea)
        cp.add(sp)

        contentPane = cp
        title = "ClassyShark Byte Code Viewer"
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        pack()
        setLocationRelativeTo(null)
    }

    fun onFileDragged(file: File) {
        val inputStream: InputStream
        try {
            inputStream = FileInputStream(file)
            val reader = ClassReader(inputStream)
            val dw = StringWriter()
            val visitor = TraceClassVisitor(PrintWriter(dw))
            reader.accept(visitor, ClassReader.EXPAND_FRAMES)
            textArea.text = dw.toString()

            // // Start capturing
            val buffer = ByteArrayOutputStream()
            System.setOut(PrintStream(buffer))

            // Run what is supposed to output something
            org.benf.cfr.reader.Main.main(arrayOf(file.absolutePath))

            // Stop capturing
            System.setOut(PrintStream(FileOutputStream(FileDescriptor.out)))

            // Use captured content
            val content = buffer.toString()
            buffer.reset()

            textArea.append("\n\n\n\n\n " + content)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            SwingUtilities.invokeLater {
                try {
                    val csbv = ClassySharkBytecodeViewer()
                    csbv.isVisible = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}