package cz.lastaapps.languagetool.ui.features.language

import arrow.core.Either
import cz.lastaapps.languagetool.core.StateViewModel
import cz.lastaapps.languagetool.core.VMState
import cz.lastaapps.languagetool.core.error.DomainError
import cz.lastaapps.languagetool.core.launchVM
import cz.lastaapps.languagetool.data.LangToolRepository
import cz.lastaapps.languagetool.domain.model.Language
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal class LanguageDialogViewModel(
    private val repo: LangToolRepository,
) : StateViewModel<LanguageState>(LanguageState()) {

    fun onAppear() = runOnlyOnce {
        launchVM {
            withLoading({
                copy(isLoading = it)
            }) {
                when (val res = repo.getLanguages()) {
                    is Either.Right -> updateState { copy(languageList = res.value) }
                    is Either.Left -> updateState { copy(error = res.value) }
                }
            }
        }
    }

    fun onLanguageSelected(lang: Language) = launchVM {
        withLoading({
            copy(isLoading = it)
        }) {
            // TODO store selected lang
        }
        updateState { copy(canLeave = true) }
    }
}

internal data class LanguageState(
    val isLoading: Boolean = false,
    val languageList: ImmutableList<Language> = persistentListOf(),
    val error: DomainError? = null,
    val canLeave: Boolean = false,
) : VMState
