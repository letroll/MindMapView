package com.mindsync.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mindsync.library.R
import com.mindsync.library.model.Node
import com.mindsync.library.model.OperationType
import com.mindsync.library.model.OperationType.Add
import com.mindsync.library.model.OperationType.Edit
import com.mindsync.library.model.OperationType.FitScreen
import com.mindsync.library.model.OperationType.Remove
import com.mindsync.library.ui.dialog.EditDescriptionDialog

@Composable
fun MapViewBar(
    modifier: Modifier = Modifier,
    selectedNode: Node,
    canRemove: Boolean,
    onAction: (OperationType,String) -> Unit,
    dialogToShow: (EditDescriptionDialog)->Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Button(
            iconResId = R.drawable.ic_add,
            onClick = {
                showDialog(
                    operationType = Add,
                    selectedNode = selectedNode,
                    onResult = {
                        onAction(Add,it)
                    },
                    dialogToShow = dialogToShow,
                )
            }
        )
        Button(
            iconResId = if (canRemove) R.drawable.ic_remove else R.drawable.ic_banned_remove,
            onClick = {
                if (canRemove) {
                    onAction(Remove,"")
                }
            }
        )
        Button(
            iconResId = R.drawable.ic_outlined_drawing,
            onClick = {
                showDialog(
                    Edit,
                    selectedNode,
                    onResult = {
                        onAction(Edit,it)
                    },
                    dialogToShow = dialogToShow,
                )
            }
        )
        Button(
            iconResId = R.drawable.ic_fit_screen,
            onClick = {
                onAction(FitScreen,"")
            }
        )
    }
}

private fun showDialog(
    operationType: OperationType,
    selectedNode: Node,
    onResult:(String)->Unit,
    dialogToShow: (EditDescriptionDialog)->Unit,
) {
    val description = if (operationType == Add) "" else selectedNode.description
    val editDescriptionDialog = EditDescriptionDialog()
    editDescriptionDialog.setDescription(description)
    editDescriptionDialog.setSubmitListener { description ->
        when (operationType) {
            Add -> {
                onResult(description)
            }

            Edit -> {
                onResult(description)
            }

            else -> return@setSubmitListener
        }
    }
    dialogToShow(editDescriptionDialog)
}

