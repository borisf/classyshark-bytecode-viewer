import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.TransferHandler;

public class FileTransferHandler extends TransferHandler {

    private final ClassySharkBytecodeViewer classySharkBytecodeViewer;

    public FileTransferHandler(ClassySharkBytecodeViewer classySearch) {
        this.classySharkBytecodeViewer = classySearch;
    }

    public boolean canImport(TransferSupport ts) {
        return true;
    }

    public boolean importData(TransferSupport ts) {
        try {
            @SuppressWarnings("rawtypes")
            List data = (List) ts.getTransferable().getTransferData(
                    DataFlavor.javaFileListFlavor);
            if (data.size() < 1) {
                return false;
            }

            for (Object item : data) {
                File file = (File) item;
                classySharkBytecodeViewer.onFileDragged(file);
            }

            return true;

        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;

        }
    }
}