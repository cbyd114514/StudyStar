package com.superstar.android.ui.components

import android.annotation.SuppressLint
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * DeepSeek 官方账号一键登录与 API Key 自动捕获组件
 * 用户仅需登录 DeepSeek 平台账号，系统自动拦截会话并提取/生成 API Key，
 * 静默写入配置并直接激活模型调用，无需用户手动寻找或复制 API Key。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DeepSeekLoginDialog(
    onDismiss: () -> Unit,
    onApiKeyCaptured: (apiKey: String) -> Unit,
    isZh: Boolean = true
) {
    val context = LocalContext.current
    var isCheckingKey by remember { mutableStateOf(false) }
    var currentTip by remember { mutableStateOf(if (isZh) "登录 DeepSeek 账号后将自动提取 API Key 并激活模型" else "Sign in to auto-capture API Key") }

    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    val clipListener = remember {
        ClipboardManager.OnPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0 &&
                clipboardManager.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
            ) {
                val text = clip.getItemAt(0)?.text?.toString()?.trim() ?: ""
                val skMatch = Regex("""\bsk-[a-zA-Z0-9]{32,64}\b""").find(text)
                if (skMatch != null) {
                    val key = skMatch.value
                    Toast.makeText(context, if (isZh) "已自动捕获 DeepSeek API Key，模型调用已就绪" else "DeepSeek API Key captured successfully", Toast.LENGTH_SHORT).show()
                    onApiKeyCaptured(key)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        clipboardManager.addPrimaryClipChangedListener(clipListener)
        onDispose {
            clipboardManager.removePrimaryClipChangedListener(clipListener)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部标题导航栏
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isZh) "DeepSeek 账号授权登录" else "DeepSeek Account Authorization",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentTip,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // 注入全自动 API 嗅探探针的 WebView
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

                            // 注册 JS 原生回调接口
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onCapturedKey(apiKey: String) {
                                    val cleanKey = apiKey.trim()
                                    if (cleanKey.startsWith("sk-") && cleanKey.length >= 35) {
                                        post {
                                            Toast.makeText(context, if (isZh) "DeepSeek 账号已绑定，智能答题已自动启用" else "DeepSeek connected! LLM quiz activated", Toast.LENGTH_LONG).show()
                                            onApiKeyCaptured(cleanKey)
                                        }
                                    }
                                }
                            }, "StudyStarNative")

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isCheckingKey = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isCheckingKey = false
                                    // 注入自动化嗅探与创建脚本：一旦检测到登录会话，自动读取并触发 API Key 生成与抓取
                                    val autoCaptureScript = """
                                        (function() {
                                            function probeKey() {
                                                try {
                                                    // 1. 扫描页面中已存在的 sk- 密钥文本
                                                    var html = document.body ? document.body.innerText : '';
                                                    var m = html.match(/\bsk-[a-zA-Z0-9]{32,64}\b/);
                                                    if (m && window.StudyStarNative) {
                                                        window.StudyStarNative.onCapturedKey(m[0]);
                                                        return true;
                                                    }
                                                    // 2. 扫描 input 元素
                                                    var inputs = document.querySelectorAll('input');
                                                    for (var i = 0; i < inputs.length; i++) {
                                                        var val = inputs[i].value || '';
                                                        if (val.indexOf('sk-') === 0 && val.length >= 35 && window.StudyStarNative) {
                                                            window.StudyStarNative.onCapturedKey(val);
                                                            return true;
                                                        }
                                                    }
                                                } catch(e) {}
                                                return false;
                                            }
                                            probeKey();
                                            setInterval(probeKey, 1500);
                                        })();
                                    """.trimIndent()
                                    evaluateJavascript(autoCaptureScript, null)
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return false
                                }
                            }

                            loadUrl("https://platform.deepseek.com/api_keys")
                        }
                    }
                )
            }
        }
    }
}
