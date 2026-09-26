package com.example.videoplayer.presentation.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.videoplayer.presentation.components.GlassSurface
import com.example.videoplayer.presentation.theme.AuroraCyan
import com.example.videoplayer.presentation.theme.DeepSpace
import com.example.videoplayer.presentation.theme.TextPrimary
import com.example.videoplayer.presentation.theme.TextSecondary

@Composable
fun GallerySearchBar(
    visible: Boolean,
    query: String,
    isSearchFiltering: Boolean,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = visible) {
        onClose()
    }

    LaunchedEffect(visible) {
        if (visible) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(180)) +
            expandVertically(tween(220)) +
            slideInVertically(tween(220)) { fullHeight -> -fullHeight / 4 },
        exit = fadeOut(tween(140)) +
            shrinkVertically(tween(180)) +
            slideOutVertically(tween(180)) { fullHeight -> -fullHeight / 4 },
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            cornerRadius = 20.dp,
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        "Search photos & videos…",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close search",
                            tint = TextPrimary,
                        )
                    }
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        if (isSearchFiltering && query.isNotEmpty()) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(20.dp),
                                strokeWidth = 2.dp,
                                color = AuroraCyan,
                            )
                        }
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = AuroraCyan,
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.padding(end = 12.dp),
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AuroraCyan,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}
