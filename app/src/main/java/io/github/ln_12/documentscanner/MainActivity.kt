package io.github.ln_12.documentscanner

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import io.github.ln_12.documentscanner.ui.theme.DocumentScannerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class MainActivity : ComponentActivity() {

    private var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private val resultInfo = MutableStateFlow<String?>(null)
    private val pageUris = MutableStateFlow<List<Uri>>(emptyList())
    private val shareIntent = MutableStateFlow<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            scannerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = ::handleActivityResult
            )

            val previewUris by pageUris.collectAsStateWithLifecycle()
            val resultText by resultInfo.collectAsStateWithLifecycle()

            Content(
                previewUris = previewUris,
                resultText = resultText,
                onScanClicked = ::onScanButtonClicked,
                onShareClicked = ::onShareButtonClicked,
            )
        }
    }

    private fun onScanButtonClicked() {
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF,
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
            )
            .setGalleryImportAllowed(true)

        GmsDocumentScanning.getClient(options.build())
            .getStartScanIntent(this)
            .addOnSuccessListener { intentSender: IntentSender ->
                scannerLauncher?.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e: Exception ->
                resultInfo.update { e.message }
            }
    }

    private fun onShareButtonClicked() {
        shareIntent.value?.let { intent ->
            startActivity(Intent.createChooser(intent, "Share scan"))
        }
    }

    private fun handleActivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            resultInfo.update { null }

            val pages = result.pages
            if (!pages.isNullOrEmpty()) {
                pageUris.update { pages.map { it.imageUri } }
            }

            result.pdf?.uri?.path?.let { path ->
                val externalUri = FileProvider.getUriForFile(
                    this,
                    "$packageName.provider",
                    File(path)
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, externalUri)
                    type = "application/pdf"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                shareIntent.update { intent }
                startActivity(Intent.createChooser(intent, "Share scan"))
            }
        } else {
            pageUris.update { emptyList() }
            resultInfo.update { "error" }
        }
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun Content(
    previewUris: List<Uri>,
    resultText: String?,
    onShareClicked: () -> Unit,
    onScanClicked: () -> Unit,
) {
    DocumentScannerTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (previewUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(horizontal = 10.dp),
                        ) {
                            items(previewUris) { uri ->
                                GlideImage(
                                    model = uri,
                                    contentDescription = "preview",
                                    contentScale = ContentScale.Inside,
                                    modifier = Modifier.height(200.dp)
                                )
                            }
                        }

                        Button(onClick = onShareClicked) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "share",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 5.dp),
                            )

                            Text(text = "Share")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (resultText != null) {
                        Text(text = resultText)
                    }

                    Button(
                        onClick = onScanClicked,
                        modifier = Modifier.padding(bottom = 50.dp)
                    ) {
                        Text(text = "Start scan")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ContentEmptyPreview() {
    Content(
        previewUris = listOf(Uri.parse("")),
        resultText = null,
        onScanClicked = {},
        onShareClicked = {}
    )
}
