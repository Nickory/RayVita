package com.codelab.basiclayouts.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.codelab.basiclayouts.R

@Composable
fun DownloadFloatingButton() {
    val context = LocalContext.current

    FloatingActionButton(
        onClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://47.96.237.130/download")
            )
            context.startActivity(intent)
        }
    ) {
        Text(stringResource(R.string.profile_download))
    }
}

