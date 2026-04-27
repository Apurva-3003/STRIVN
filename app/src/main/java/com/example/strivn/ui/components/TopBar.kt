package com.example.strivn.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    centerTitle: Boolean = true,
    /** When set, shows a back arrow (e.g. pop to Home from Daily Check-In). */
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
    )

    val navigationIcon: @Composable () -> Unit = {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }
    }

    if (centerTitle) {
        CenterAlignedTopAppBar(
            modifier = modifier.fillMaxWidth(),
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            windowInsets = TopAppBarDefaults.windowInsets,
        )
    } else {
        androidx.compose.material3.TopAppBar(
            modifier = modifier.fillMaxWidth(),
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            windowInsets = TopAppBarDefaults.windowInsets,
        )
    }
}

