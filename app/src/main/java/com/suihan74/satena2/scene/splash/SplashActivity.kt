package com.suihan74.satena2.scene.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.preferences.page.theme.ThemeViewModelImpl
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.Satena2Theme
import com.suihan74.satena2.utility.copyrightYearStr
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    private val viewModel by viewModels<SplashViewModelImpl>()

    private val themeViewModel by viewModels<ThemeViewModelImpl>()

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreateActivity(activityResultRegistry, lifecycle)

        setContent {
            val theme by themeViewModel.currentThemeFlow.collectAsState()
            Satena2Theme(theme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SplashScene(viewModel.versionName)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onStart(this@SplashActivity)
            }
        }
    }
}

// ------ //

@Composable
private fun SplashScene(
    versionName : String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CurrentTheme.primary),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "app logo",
                colorFilter = ColorFilter.tint(CurrentTheme.onPrimary),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                color = CurrentTheme.onPrimary,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = "version $versionName",
                color = CurrentTheme.onPrimary,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        top = 0.dp,
                        bottom = 12.dp,
                        start = 0.dp,
                        end = 0.dp
                    )
            )
            Text(
                text = stringResource(
                    id = R.string.copyright,
                    copyrightYearStr()
                ),
                color = CurrentTheme.onPrimary,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// ------ //

@Preview
@Composable
private fun SplashScenePreview() {
    Box(Modifier.fillMaxSize()) {
        SplashScene(versionName = "x.x.x")
    }
}
