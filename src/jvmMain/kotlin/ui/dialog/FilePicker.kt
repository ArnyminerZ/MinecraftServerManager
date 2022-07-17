package ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun FilePickerDialog(
    parent: Frame? = null,
    onCloseRequest: (result: List<File>) -> Unit,
    title: String,
    isMultipleMode: Boolean = false,
    allowedExtensions: Collection<String> = emptyList(),
) = AwtWindow(
    create = {
        object : FileDialog(parent, title, LOAD) {
            override fun setVisible(visible: Boolean) {
                super.setVisible(visible)
                if (!visible)
                    onCloseRequest.invoke(files.toList())
                else {
                    this.isMultipleMode = isMultipleMode

                    // Set allowed extensions
                    file = allowedExtensions.joinToString(";") { it } // e.g. '*.jpg'
                    setFilenameFilter { _, name -> allowedExtensions.any { name.endsWith(it) } }
                }
            }
        }
    },
    dispose = FileDialog::dispose
)
