package com.suihan74.satena2.scene.preferences.page.accounts.mastodon

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.suihan74.satena2.R
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Mastodonの認証ページ
 */
@AndroidEntryPoint
class MastodonAuthenticationActivity : ComponentActivity() {
    private val viewModel by viewModels<MastodonAuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 認証終了時にActivityを終了する
        viewModel.completed
            .onEach { finish() }
            .launchIn(lifecycleScope)

        setContent {
            MastodonAuthenticationContent(
                viewModel,
                onCancel = { finish() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

// ------ //

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MastodonAuthenticationContent(
    viewModel: IMastodonAuthenticationViewModel,
    onCancel: ()->Unit
) {
    val backgroundColor = Color(0x1d, 0x20, 0x34)
    val textColor = Color(0xfd, 0xfd, 0xfe)
    val primaryColor = Color(0x43, 0x52, 0xd6)

    // ステータスバーの色
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(backgroundColor)

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val keyboardController = LocalSoftwareKeyboardController.current
        val instanceTextFieldValue = rememberMutableTextFieldValue()
        val focusRequester = focusKeyboardRequester()
        val keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusRequester.freeFocus()
            }
        )

        Image(
            painter = painterResource(R.drawable.mastodon_logo_full),
            contentDescription = "mastodon logo",
            modifier = Modifier
                .width(256.dp)
                .align(Alignment.CenterHorizontally)
        )
        TextField(
            value = instanceTextFieldValue.value,
            onValueChange = { instanceTextFieldValue.value = it },
            maxLines = 1,
            singleLine = true,
            label = { Text(stringResource(R.string.instance)) },
            placeholder = { Text(stringResource(R.string.instance_example)) },
            colors = TextFieldDefaults.textFieldColors(
                textColor = textColor,
                cursorColor = textColor,
                backgroundColor = Color(0, 0, 0, 0x30),
                focusedLabelColor = textColor.copy(alpha = 0.5f),
                unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                placeholderColor = textColor.copy(alpha = 0.3f),
                focusedIndicatorColor = primaryColor
            ),
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp)
                .focusRequester(focusRequester)
        )
        Row(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp)
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = primaryColor
                ),
                elevation = null
            ) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.startAuthorization(
                            context,
                            instanceTextFieldValue.value.text
                        )
                    }
                },
                contentPadding = PaddingValues(horizontal = 40.dp),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = primaryColor,
                    contentColor = Color.White
                ),
                elevation = null,
                modifier = Modifier.padding(start = 32.dp)
            ) {
                Text(stringResource(R.string.start_authorization))
            }
        }
    }
}

// ------ //

@Preview
@Composable
private fun MastodonAuthenticationContentPreview() {
    MastodonAuthenticationContent(
        viewModel = FakeMastodonAuthenticationViewModel(),
        onCancel = {}
    )
}
