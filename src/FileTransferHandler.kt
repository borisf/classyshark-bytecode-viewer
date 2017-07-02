import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File
import java.io.IOException
import javax.swing.TransferHandler

class FileTransferHandler(private val classySharkBytecodeViewer: ClassySharkBytecodeViewer) : TransferHandler() {

    override fun canImport(ts: TransferHandler.TransferSupport): Boolean {
        return true
    }

    override fun importData(ts: TransferHandler.TransferSupport): Boolean {
        try {
            val data = ts.transferable.getTransferData(
                    DataFlavor.javaFileListFlavor) as List<*>
            if (data.size < 1) {
                return false
            }

            data.map { it as File }.forEach { classySharkBytecodeViewer.onFileDragged(it) }

            return true

        } catch (e: UnsupportedFlavorException) {
            return false
        } catch (e: IOException) {
            return false

        }
    }
}