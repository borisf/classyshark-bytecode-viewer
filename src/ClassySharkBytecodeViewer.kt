import com.strobel.decompiler.DecompilerDriver
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.io.*
import javax.swing.*


class ClassySharkBytecodeViewer @Throws(Exception::class)
constructor() : JFrame() {

    internal var javaArea: JTextPane
    internal var asmArea: JTextPane
    internal var ASM: String = ""
    internal val panelTitle = "ClassyShark Byte Code Viewer - "
    internal val RESULT_AREAS_BACKGROUND = Color(46, 48, 50)
    internal val INPUT_AREA_BACKGROUND = Color(88, 110, 117)

    init {
        title = panelTitle + "drag your .class file into the shark"
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        preferredSize = Dimension(1230, 650)

        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

        val searchLabel = JLabel(createImageIcon("magnify.png", "Search"))
        val searchText = JTextField()
        searchText.font = Font("Menlo", Font.PLAIN, 18)
        searchText.background = INPUT_AREA_BACKGROUND
        searchText.foreground = Color.CYAN

        val toolbar = JPanel(BorderLayout())
        toolbar.add(searchLabel, BorderLayout.WEST)
        toolbar.add(searchText, BorderLayout.CENTER)
        mainPanel.add(toolbar)

        val resultPanel = JPanel()
        resultPanel.layout = BoxLayout(resultPanel, BoxLayout.X_AXIS)
        javaArea = SyntaxPane()
        javaArea.font = Font("Menlo", Font.PLAIN, 18)
        javaArea.text = SharkBG.SHARKEY
        javaArea.background = RESULT_AREAS_BACKGROUND
        javaArea.foreground = Color.CYAN
        javaArea.transferHandler = FileTransferHandler(this)
        val javaScrollPane = JScrollPane(javaArea)
        resultPanel.add(javaScrollPane)

        asmArea = SyntaxPane()
        asmArea.font = Font("Menlo", Font.PLAIN, 18)
        asmArea.transferHandler = FileTransferHandler(this)
        asmArea.background = RESULT_AREAS_BACKGROUND
        asmArea.foreground = Color.BLACK
        asmArea.text = SharkBG.SHARKEY
        val asmScrollPane = JScrollPane(asmArea)
        resultPanel.add(asmScrollPane)

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
    }

    fun onFileDragged(file: File) {
        try {
            title = panelTitle + file.name
            fillJavaArea(file)
            fillAsmArea(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
        if (imgURL != null) {
            return ImageIcon(imgURL, description)
        } else {
            System.err.println("Couldn't find file: " + path)
            return null
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