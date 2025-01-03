package cz.lastaapps.languagetool.ui.features.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import cz.lastaapps.languagetool.R
import cz.lastaapps.languagetool.core.error.DomainError
import cz.lastaapps.languagetool.core.error.getMessage
import cz.lastaapps.languagetool.ui.features.home.components.ActionChips
import cz.lastaapps.languagetool.ui.features.home.components.ErrorSuggestionRow
import cz.lastaapps.languagetool.ui.features.home.components.HomeBottomAppBar
import cz.lastaapps.languagetool.ui.features.home.components.TextCorrectionField
import cz.lastaapps.languagetool.ui.util.hasInternetConnection
import kotlinx.coroutines.launch


@Composable
internal fun HomeDest(
    viewModel: HomeViewModel,
    toAbout: () -> Unit,
    toHelp: () -> Unit,
    toLanguage: () -> Unit,
    toSettings: () -> Unit,
    toSpellCheck: () -> Unit,
    modifier: Modifier = Modifier,
    hostState: SnackbarHostState =
        remember { SnackbarHostState() },
) {
    val state by viewModel.flowState
    var cursorPosition by remember { mutableStateOf(0) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.onAppear()
    }

    suspend fun onError(error: DomainError) {
        val message = if (hasInternetConnection(context))
            error.getMessage() else context.getString(R.string.no_internet_connection)
        hostState.showSnackbar(message)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch {
                onError(it)
            }
            viewModel.dismissError()
        }
    }

    val textBlock: @Composable (Modifier) -> Unit = { localModifier ->
        TextCorrectionField(
            progress = state.progress,
            matched = state.matched,
            onText = viewModel::onTextChanged,
            onCursor = { cursorPosition = it },
            charLimit = state.maxChars,
            modifier = localModifier,
        )
    }
    val errorSuggestions: @Composable () -> Unit = {
        ErrorSuggestionRow(
            progress = state.progress,
            cursorPosition = cursorPosition,
            matched = state.matched,
            onApplySuggestion = viewModel::applySuggestion,
            onDetail = { viewModel.selectMatchError(it) },
            onSkip = viewModel::skipSuggestion,
        )
    }
    val chipsBlock: @Composable () -> Unit = {
        ActionChips(
            matched = state.matched,
            onPasteText = {
                viewModel.onTextChanged(it)
                viewModel.onCheckRequest()
            },
            onClear = { viewModel.onTextChanged("") },
            onError = {
                scope.launch {
                    onError(it)
                }
            },
            isPicky = state.isPicky,
            onPickyClick = { viewModel.setIsPicky(!state.isPicky) },
            selectedLanguage = state.language?.name,
            onLanguageClick = toLanguage,
            hasPremium = false,
            onPremiumClick = {
                uriHandler.openUri("https://languagetool.org/premium_new")
            },
        )
    }
    val appBarBlock: @Composable () -> Unit = {
        HomeBottomAppBar(
            progress = state.progress,
            onCheck = { viewModel.onCheckRequest() },
            onSystemSpellCheck = toSpellCheck,
            onHelpClick = toHelp,
            onSettings = toSettings,
            onAbout = toAbout,
        )
    }

    HomeScreenScaffold(
        snackbarHostState = hostState,
        text = textBlock,
        actionChips = chipsBlock,
        errorSuggestions = errorSuggestions,
        appBar = appBarBlock,
        modifier = modifier,
    )

    state.selectedMatch?.let { error ->
        val dismiss = { viewModel.selectMatchError(null) }
        MatchDetailDialog(
            error = error,
            onApplySuggestion = {
                viewModel.applySuggestion(error, it)
                dismiss()
            },
            onSkip = {
                viewModel.skipSuggestion(error)
                dismiss()
            },
            onDismiss = dismiss,
        )
    }
}

