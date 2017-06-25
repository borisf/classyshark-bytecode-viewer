import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTextPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class SyntaxPane extends JTextPane {

    static final Color BACKGROUND_LIGHT = new Color(46, 48, 50);
    static final Color IDENTIFIERS = new Color(0xFF, 0xFF, 0x80);
    static final Color DEFAULT = Color.CYAN;
    static final Color KEYWORDS = new Color(133, 153, 0);
    static final Color ANNOTATIONS = new Color(108, 113, 196);
    static final Color SELECTION_BG = new Color(7, 56, 66);
    public static final Color NAMES = new Color(88, 110, 117);

    private final static Set KEY_WORDS = new HashSet<String>();

    static {
        KEY_WORDS.add("class");
        KEY_WORDS.add("import");
        KEY_WORDS.add("class");
        KEY_WORDS.add("public");
        KEY_WORDS.add("private");
        KEY_WORDS.add("null");
        KEY_WORDS.add("static");
        KEY_WORDS.add("return");
        KEY_WORDS.add("void");
        KEY_WORDS.add("for");
        KEY_WORDS.add("while");
        KEY_WORDS.add("final");
        KEY_WORDS.add("if");
        KEY_WORDS.add("else");
        KEY_WORDS.add("package");
        KEY_WORDS.add("abstract");
        KEY_WORDS.add("interface");

        //<<<<<<<<<<<<<<<
        KEY_WORDS.add("INNERCLASS");
        KEY_WORDS.add("FRAME FULL");
    }

    @Override
    public void setText(String text) {
        if(text.equals(SharkBG.SHARKEY)) {
            super.setText(text);
            return;
        }

        super.setText("");

        StringBuilder currentWord = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            if (currentChar == ' ' || currentChar == '\n' || currentChar == ';') {
                if (isKeyword(currentWord.toString())) {
                    appendToPane(currentWord.toString(), KEYWORDS);
                } else if (isLabel(currentWord.toString())) {
                    appendToPane(currentWord.toString(), ANNOTATIONS);
                } else if (isCompound(currentWord.toString())) {
                    appendToPane(currentWord.toString(), NAMES);
                } else {
                    appendToPane(currentWord.toString(), DEFAULT);
                }

                currentWord.setLength(0);
                appendToPane(String.valueOf(currentChar), DEFAULT);
            } else {
                currentWord.append(String.valueOf(currentChar));
            }
        }
    }

    private boolean isCompound(String word) {
        return word.contains("/");
    }

    private boolean isLabel(String word) {
        return (word.length() > 1) && word.charAt(0) == 'L' && Character.isDigit(word.charAt(1));
    }

    private boolean isKeyword(String word) {
        return KEY_WORDS.contains(word);
    }

    public void appendToPane(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = getDocument().getLength();
        setCaretPosition(len);
        setCharacterAttributes(aset, false);
        replaceSelection(msg);
    }

    // Override getScrollableTracksViewportWidth
    // to preserve the full width of the text
    public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();
        ComponentUI ui = getUI();

        return parent != null ? (ui.getPreferredSize(this).width <= parent
                .getSize().width) : true;
    }
}