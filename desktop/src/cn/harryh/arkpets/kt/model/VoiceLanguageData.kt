package cn.harryh.arkpets.kt.model

data class VoiceLanguageData(
    val size: Int,
    val duration: Double,
    val clips: List<VoiceChip>
)
