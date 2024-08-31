package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun Greeting(
    stateFlow: StateFlow<Boolean>,
    name: String,
) {
    val state = stateFlow.collectAsState()

    if (true == false) {
        Text(
            text = "Hello, $name!",
        )
    }
}

@Preview(apiLevel = 34)
@Composable
fun PreviewGreeting() {
    val _stateFlow = MutableStateFlow(true)
    val stateFlow: StateFlow<Boolean> = _stateFlow

    Greeting(
        stateFlow,
        name = "World",
    )
}
