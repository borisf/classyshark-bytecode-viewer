import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter
import javax.swing.text.Document
import javax.swing.text.JTextComponent

class IncrementalSearch(private var content: JTextComponent) : DocumentListener, ActionListener {
    private var matcher: Matcher? = null

    override fun insertUpdate(evt: DocumentEvent) {
        runNewSearch(evt.document)
    }

    override fun removeUpdate(evt: DocumentEvent) {
        runNewSearch(evt.document)
    }

    override fun changedUpdate(evt: DocumentEvent) {
        runNewSearch(evt.document)
    }

    override fun actionPerformed(evt: ActionEvent) {
        continueSearch()
    }

    private fun runNewSearch(query_doc: Document) {
        try {
            val query = query_doc.getText(0, query_doc.length)
            val pattern = Pattern.compile(query)
            val content_doc = content.document
            val body = content_doc.getText(0, content_doc.length)
            matcher = pattern.matcher(body)
            continueSearch()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun continueSearch() {
        if (matcher != null) {

            content.highlighter.removeAllHighlights()

            while (matcher!!.find()) {
                val group = matcher!!.group()

                if (group.length > 2) {
                    content.caret.dot = matcher!!.start()
                    content.caret.moveDot(matcher!!.end())

                    val highlightPainter = DefaultHighlighter.DefaultHighlightPainter(HIGHLIGHTER_COLOR)

                    try {
                        content.highlighter.addHighlight(matcher!!.start(),
                                matcher!!.end(),
                                highlightPainter)
                    } catch (e: BadLocationException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    companion object {
        private val HIGHLIGHTER_COLOR = Color(71, 86, 89)
    }
}
