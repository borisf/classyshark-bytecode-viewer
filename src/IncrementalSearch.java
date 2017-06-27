import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class IncrementalSearch implements DocumentListener, ActionListener {
    private static final Color HIGHLIGHTER_COLOR = new Color(71, 86, 89);
    protected JTextComponent content;
    protected Matcher matcher;

    public IncrementalSearch(JTextComponent comp) {
        this.content = comp;
    }

    /* DocumentListener implementation */
    public void insertUpdate(DocumentEvent evt) {
        runNewSearch(evt.getDocument());
    }

    public void removeUpdate(DocumentEvent evt) {
        runNewSearch(evt.getDocument());
    }

    public void changedUpdate(DocumentEvent evt) {
        runNewSearch(evt.getDocument());
    }

    /* ActionListener implementation */
    public void actionPerformed(ActionEvent evt) {
        continueSearch();
    }

    private void runNewSearch(Document query_doc) {
        try {
            String query = query_doc.getText(0, query_doc.getLength());
            Pattern pattern = Pattern.compile(query);
            Document content_doc = content.getDocument();
            String body = content_doc.getText(0, content_doc.getLength());
            matcher = pattern.matcher(body);
            continueSearch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void continueSearch() {
        if (matcher != null) {

            content.getHighlighter().removeAllHighlights();

            while (matcher.find()) {
                String group = matcher.group();

                if (group.length() > 2) {

                    content.getCaret().setDot(matcher.start());
                    content.getCaret().moveDot(matcher.end());

                    DefaultHighlighter.DefaultHighlightPainter highlightPainter =
                            new DefaultHighlighter.DefaultHighlightPainter(HIGHLIGHTER_COLOR);

                    try {
                        content.getHighlighter().addHighlight(matcher.start(),
                                matcher.end(),
                                highlightPainter);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
