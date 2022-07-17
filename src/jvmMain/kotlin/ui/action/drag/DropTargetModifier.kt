package ui.action.drag

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.dnd.DropTargetListener
import java.io.File
import java.awt.dnd.DropTarget as AwtDropTarget

actual class PlatformDropTargetModifier(
    density: Float,
    window: ComposeWindow,
) : DropTargetModifier by dropTargetModifier() {
    init {
        val awtDropTarget = AwtDropTarget()
        awtDropTarget.addDropTargetListener(
            dropTargetListener(
                dropTargetModifier = this,
                density = density
            )
        )
        window.contentPane.dropTarget = awtDropTarget
    }
}

private fun dropTargetListener(
    dropTargetModifier: DropTargetModifier,
    density: Float
) = object : DropTargetListener {
    override fun dragEnter(dtde: DropTargetDragEvent?) {
        if (dtde == null) return
        dropTargetModifier.onDragStarted(
            dtde.files(),
            Offset(
                dtde.location.x * density,
                dtde.location.y * density
            )
        )
        dropTargetModifier.onDragEntered()
    }

    override fun dragOver(dtde: DropTargetDragEvent?) {
        if (dtde == null) return
        dropTargetModifier.onDragMoved(
            Offset(
                dtde.location.x * density,
                dtde.location.y * density
            )
        )
    }

    override fun dropActionChanged(dtde: DropTargetDragEvent?) = Unit

    override fun dragExit(dte: DropTargetEvent?) {
        dropTargetModifier.onDragExited()
        dropTargetModifier.onDragEnded()
    }

    override fun drop(dtde: DropTargetDropEvent?) {
        if (dtde == null) return dropTargetModifier.onDragEnded()

        dtde.acceptDrop(DnDConstants.ACTION_REFERENCE)
        dtde.dropComplete(
            dropTargetModifier.onDropped(
                dtde.files(),
                Offset(
                    dtde.location.x * density,
                    dtde.location.y * density
                )
            )
        )
        dropTargetModifier.onDragEnded()
    }
}

private fun DropTargetDragEvent.files(): List<File> = transferable
    .getTransferData(DataFlavor.javaFileListFlavor)
    .let { it as? List<*> ?: listOf<File>() }
    .filterIsInstance<File>()

private fun DropTargetDropEvent.files(): List<File> = transferable
    .getTransferData(DataFlavor.javaFileListFlavor)
    .let { it as? List<*> ?: listOf<File>() }
    .filterIsInstance<File>()