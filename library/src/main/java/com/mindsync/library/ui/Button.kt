package com.mindsync.library.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable fun Button(
    modifier: Modifier = Modifier,
    iconResId: Int,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = ""
        )
    }
}
