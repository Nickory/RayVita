package com.codelab.basiclayouts.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.R

@Composable
fun DownloadFloatingButton() {
    val context = LocalContext.current

    ExtendedFloatingActionButton(
        onClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://47.96.237.130/download")
            )
            context.startActivity(intent)
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.GetApp,
                contentDescription = stringResource(R.string.profile_download_new_version)
            )
        },
        text = { Text(stringResource(R.string.profile_download_new_version)) },
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
        containerColor = Color.Transparent,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(6.dp)
    )
}

