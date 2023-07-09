package cz.lastaapps.languagetool.api.model

import arrow.core.some
import cz.lastaapps.languagetool.data.model.MatchedText
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Serializable
internal data class CorrectionDto(
    val softWare: SoftwareDto,
    val warnings: WarningDto?,
    val language: LanguageDto,
    val matches: List<MatchDto>,
)

@Serializable
internal data class SoftwareDto(
    val name: String,
    val version: String,
    val buildDate: String,
    val apiVersion: Int,
    val status: String?,
    val premium: Boolean?,
)

@Serializable
internal data class WarningDto(
    val incompleteResults: Boolean?,
)

@Serializable
internal data class LanguageDto(
    val name: String,
    val code: String,
    val detectedLanguage: DetectedLanguageDto,
)

@Serializable
internal data class DetectedLanguageDto(
    val name: String,
    val code: String,
)

internal fun CorrectionDto.toDomain(
    text: String,
) = MatchedText(
    text = text,
    errors = matches.map { it.toDomain() }.toImmutableList(),
    isComplete = (this.warnings?.incompleteResults ?: true).some(),
)
