package com.suihan74.satena2.scene.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.hatena.model.account.Tag
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.VerticalScrollableIndicator
import com.suihan74.satena2.ui.theme.CurrentTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsSheet(
    tags: List<Tag>,
    textFieldValue: MutableState<TextFieldValue>,
    tagTextFieldValue: MutableState<TextFieldValue>
) {
    val tagsListScrollState = rememberScrollState()

    Column(Modifier.padding(top = 8.dp)) {
        Box(
            Modifier.weight(1f)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .verticalScroll(state = tagsListScrollState)
            ) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
                for (tag in tags) {
                    TagItem(tag = tag) {
                        tagTextFieldValue.value = tagTextFieldValue.value.copy(
                            text = tag.text,
                            selection = TextRange(start = 0, end = tag.text.length)
                        )
                    }
                }
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
            }
            VerticalScrollableIndicator(
                scrollState = tagsListScrollState,
                gradientColor = CurrentTheme.background
            )
        }

        Spacer(Modifier.height(4.dp))
        TextField(
            value = tagTextFieldValue.value,
            onValueChange = { tagTextFieldValue.value = it },
            maxLines = 1,
            singleLine = true,
            label = { Text(stringResource(R.string.tag)) },
            placeholder = { Text(stringResource(R.string.post_placeholder_tag_text_field)) },
            colors = TextFieldDefaults.textFieldColors(
                textColor = CurrentTheme.onBackground,
                placeholderColor = CurrentTheme.grayTextColor,
                cursorColor = CurrentTheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                trailingIconColor = CurrentTheme.primary,
                focusedLabelColor = CurrentTheme.grayTextColor,
                unfocusedLabelColor = CurrentTheme.grayTextColor,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun TagItem(tag: Tag, onClick: (Tag)->Unit) {
    Box(
        Modifier
            .padding(horizontal = 2.dp)
            .background(
                color = CurrentTheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick(tag) }
    ) {
        Text(
            text = tag.text,
            fontSize = 13.sp,
            color = CurrentTheme.onPrimary,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        )
    }
}
