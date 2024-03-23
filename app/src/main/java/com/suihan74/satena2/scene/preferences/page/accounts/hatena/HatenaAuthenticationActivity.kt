package com.suihan74.satena2.scene.preferences.page.accounts.hatena

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.suihan74.satena2.scene.preferences.page.theme.ThemeViewModelImpl
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.Satena2Theme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hatenaのログインページ
 */
@AndroidEntryPoint
class HatenaAuthenticationActivity : ComponentActivity() {
    private val viewModel by viewModels<HatenaAuthenticationViewModelImpl>()
    
    private val themeViewModel by viewModels<ThemeViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setOnErrorListener {
            Toast.makeText(this, "サインイン失敗", Toast.LENGTH_SHORT).show()
            finish()
        }

        viewModel.setOnFinishListener {
            finish()
        }

        setContent {
            val theme by themeViewModel.currentThemeFlow.collectAsState()
            Satena2Theme(theme) {
                HatenaAuthorizationContent(viewModel)
            }
        }
    }
}

// ------ //

@Composable
private fun HatenaAuthorizationContent(viewModel: HatenaAuthenticationViewModel) {
    val loading = viewModel.loading.collectAsState().value
    val webViewState = rememberWebViewState(url = viewModel.signInPageUrl)
    val interactionSource = remember { MutableInteractionSource() }

    Box(Modifier.fillMaxSize()) {
        WebView(
            state = webViewState,
            client = viewModel.webViewClient,
            onCreated = { viewModel.onCreated(it) }
        )

        if (loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(CurrentTheme.tapGuard)
                    .hoverable(interactionSource)
            ) {
                CircularProgressIndicator(
                    color = CurrentTheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

// ------ //

@Preview
@Composable
private fun HatenaAuthorizationContentPreview() {
    HatenaAuthorizationContent(
        viewModel = FakeHatenaAuthenticationViewModel(
            coroutineScope = rememberCoroutineScope()
        )
    )
}
