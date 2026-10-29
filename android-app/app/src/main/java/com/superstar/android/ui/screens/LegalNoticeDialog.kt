package com.superstar.android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstar.android.ui.i18n.AppStrings

/**
 * 基于中华人民共和国法律的合规与免责声明弹窗
 * 遵循 Material Design 3 规范，支持双语展示与垂直平滑滚动
 */
@Composable
fun LegalNoticeDialog(
    currentLang: String,
    onDismiss: () -> Unit,
    onAgree: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = AppStrings.legalDialogTitle(currentLang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = AppStrings.legalDialogContent(currentLang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            if (onAgree != null) {
                Button(
                    onClick = {
                        onAgree()
                        onDismiss()
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = AppStrings.legalDialogAgree(currentLang),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = onDismiss,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = AppStrings.legalDialogClose(currentLang),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            if (onAgree != null) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(AppStrings.legalDialogClose(currentLang))
                }
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}
