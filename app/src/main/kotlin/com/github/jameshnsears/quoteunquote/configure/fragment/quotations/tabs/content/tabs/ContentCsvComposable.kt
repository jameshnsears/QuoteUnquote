package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun Greeting(
    stateFlow: StateFlow<Boolean>,
    name: String,
) {
    val state = stateFlow.collectAsState()

    val isDarkTheme = isSystemInDarkTheme()

    val darkColors = CardDefaults.cardColors(
        containerColor = Color(0xFF25222d),
        contentColor = Color.White,
    )

    val lightColors = CardDefaults.cardColors(
        containerColor = Color(0xFFefe7f6),
        contentColor = Color.Black,
    )

    if (state.value) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp, start = 5.dp, end = 5.dp),
        ) {
            OutlinedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                ),
                colors = if (isDarkTheme) darkColors else lightColors,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(3.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    text = "Hello, $name!",
                    modifier = Modifier
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(apiLevel = 34)
@Composable
fun PreviewGreeting() {
    val _stateFlow = MutableStateFlow(true)
    val stateFlow: StateFlow<Boolean> = _stateFlow

    MaterialTheme {
        Greeting(
            stateFlow,
            name = "World",
        )
    }
}

@Preview(
    apiLevel = 34,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
fun PreviewGreetingDarkTheme() {
    val _stateFlow = MutableStateFlow(true)
    val stateFlow: StateFlow<Boolean> = _stateFlow

    MaterialTheme {
        Greeting(
            stateFlow,
            name = "World",
        )
    }
}
