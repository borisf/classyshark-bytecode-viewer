import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

public class ClassySharkBytecodeViewer extends JFrame {

    JTextArea textArea;

    public ClassySharkBytecodeViewer() throws Exception {

        JPanel cp = new JPanel(new BorderLayout());

        textArea = new JTextArea(30, 90);
        textArea.setFont(new Font("Menlo", Font.PLAIN, 18));
        textArea.setText(SharkBG.SHARKEY);

        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.CYAN);
        textArea.setTransferHandler(new FileTransferHandler(this));

        JScrollPane sp = new JScrollPane(textArea);
        cp.add(sp);

        setContentPane(cp);
        setTitle("ClassyShark Byte Code Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void onFileDragged(File file) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
            ClassReader reader = new ClassReader(inputStream);
            StringWriter dw = new StringWriter();
            ClassVisitor visitor = new TraceClassVisitor(new PrintWriter(dw));
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            textArea.setText(dw.toString());

            // // Start capturing
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            System.setOut(new PrintStream(buffer));

            // Run what is supposed to output something
            org.benf.cfr.reader.Main.main(new String[]{file.getAbsolutePath()});

            // Stop capturing
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

            // Use captured content
            String content = buffer.toString();
            buffer.reset();

            textArea.append("\n\n\n\n\n " + content);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClassySharkBytecodeViewer csbv = new ClassySharkBytecodeViewer();
                csbv.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}