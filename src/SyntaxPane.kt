import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

class SyntaxPane : JTextPane() {
    init {

        // hack to block editing
        addFocusListener(object : FocusListener {

            override fun focusLost(e: FocusEvent) {
                isEditable = true
            }

            override fun focusGained(e: FocusEvent) {
                isEditable = false

            }
        })

        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {

            }

            override fun keyPressed(e: KeyEvent) {
                val toolkit = Toolkit.getDefaultToolkit()
                val ctrlModifier = toolkit.menuShortcutKeyMask

                //check if the modifier: ctrl for linux, windows or command for mac is pressed
                // with the'c' key
                if (e.keyChar == 'c' && e.modifiers and ctrlModifier == ctrlModifier) {

                    var copyText: String? = selectedText

                    //if there is no selection, copy the entire text
                    if (copyText == null) {
                        copyText = text
                    }

                    //Add the text to the clipboard
                    toolkit.systemClipboard.setContents(StringSelection(copyText), null)
                }
            }

            override fun keyReleased(e: KeyEvent) {

            }
        })
    }

    override fun setText(text: String?) {
        if (text == SharkBG.SHARKEY || text == ClassySharkBytecodeViewer.IntroTextHolder.INTRO_TEXT) {
            super.setText(text)
            return
        }

        super.setText("")

        val currentWord = StringBuilder()

        (0..text!!.length - 1).map { text[it] }.forEach {
            if (it == ' ' || it == '\n' || it == ';') {
                if (isKeyword(currentWord.toString())) {
                    appendToPane(currentWord.toString(), KEYWORDS_COLOR)
                } else if (isLabel(currentWord.toString())) {
                    appendToPane(currentWord.toString(), LABELS)
                } else if (isCompound(currentWord.toString())) {
                    appendToPane(currentWord.toString(), COMPOUNDS)
                } else {
                    appendToPane(currentWord.toString(), DEFAULT)
                }

                currentWord.setLength(0)
                appendToPane(it.toString(), DEFAULT)
            } else {
                currentWord.append(it.toString())
            }
        }
    }

    private fun isCompound(word: String): Boolean {
        return word.contains("/")
    }

    private fun isLabel(word: String): Boolean {
        return word.length > 1 && word[0] == 'L' && Character.isDigit(word[1])
    }

    private fun isKeyword(word: String): Boolean {
        return KEYWORDS.contains(word)
    }

    fun appendToPane(msg: String, c: Color) {
        val sc = StyleContext.getDefaultStyleContext()
        var aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c)

        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED)

        val len = document.length
        caretPosition = len
        setCharacterAttributes(aset, false)
        replaceSelection(msg)
    }

    // Override getScrollableTracksViewportWidth
    // to preserve the full width of the text
    override fun getScrollableTracksViewportWidth(): Boolean {
        val parent = parent
        val ui = getUI()

        return if (parent != null) {
            ui.getPreferredSize(this).width <= parent
                    .size.width
        } else {
            true
        }
    }

    companion object {

        internal val DEFAULT = Color.CYAN
        internal val KEYWORDS_COLOR = Color(133, 153, 0)
        internal val LABELS = Color(200, 113, 196)
        val COMPOUNDS = Color(87, 129, 140)
        private val KEYWORDS = HashSet<String>()

        init {
            KEYWORDS.add("class")
            KEYWORDS.add("import")
            KEYWORDS.add("class")
            KEYWORDS.add("public")
            KEYWORDS.add("private")
            KEYWORDS.add("null")
            KEYWORDS.add("static")
            KEYWORDS.add("return")
            KEYWORDS.add("void")
            KEYWORDS.add("for")
            KEYWORDS.add("while")
            KEYWORDS.add("final")
            KEYWORDS.add("if")
            KEYWORDS.add("else")
            KEYWORDS.add("package")
            KEYWORDS.add("abstract")
            KEYWORDS.add("interface")
            KEYWORDS.add("try")
            KEYWORDS.add("catch")

            //<<<<<<<<<<<<<<<
            KEYWORDS.add("INNERCLASS")
            KEYWORDS.add("FRAME FULL")
        }
    }
}