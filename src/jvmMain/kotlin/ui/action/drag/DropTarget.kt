package ui.action.drag

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.debugInspectorInfo
import java.io.File

interface DropTarget {
    fun onDragStarted(files: List<File>, position: Offset): Boolean
    fun onDragEntered()
    fun onDragMoved(position: Offset) {}
    fun onDragExited()
    fun onDropped(files: List<File>, position: Offset): Boolean
    fun onDragEnded()
}

interface DropTargetModifier : DropTarget, Modifier.Element

internal fun dropTargetModifier(): DropTargetModifier = DropTargetContainer(
    onDragStarted = { _, _ -> DragAction.Reject }
)

expect class PlatformDropTargetModifier : DropTargetModifier

fun Modifier.dropTarget(
    onDragStarted: (files: List<File>, Offset) -> Boolean,
    onDragEntered: () -> Unit = { },
    onDragMoved: (position: Offset) -> Unit = {},
    onDragExited: () -> Unit = { },
    onDropped: (files: List<File>, position: Offset) -> Boolean,
    onDragEnded: () -> Unit = {},
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "dropTarget"
        properties["onDragStarted"] = onDragStarted
    },
    factory = {
        val node = remember {
            DropTargetContainer { files, offset ->
                when (onDragStarted(files, offset)) {
                    false -> DragAction.Reject
                    true -> DragAction.Accept(
                        object : DropTarget {
                            override fun onDragStarted(files: List<File>, position: Offset): Boolean = onDragStarted(
                                files,
                                position
                            )

                            override fun onDragEntered() = onDragEntered()

                            override fun onDragMoved(position: Offset) = onDragMoved(position)

                            override fun onDragExited() = onDragExited()

                            override fun onDropped(files: List<File>, position: Offset): Boolean = onDropped(
                                files,
                                position
                            )

                            override fun onDragEnded() = onDragEnded()
                        }
                    )
                }
            }
        }
        this.then(node)
    })
