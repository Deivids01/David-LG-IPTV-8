package com.davidlg.iptvplayer.data

/** contentType igual que en la versión web: "mixed" (TV + VOD), "tv" (solo TV en vivo) o "vod" (solo VOD). */
data class Playlist(
    val id: String,
    val name: String,
    val m3uUrl: String,
    val epgUrl: String = "",
    val contentType: String = "mixed",
)

data class Channel(
    val id: String,
    val number: Int,
    val name: String,
    val tvgId: String,
    val tvgName: String,
    val group: String,
    val logo: String,
    val url: String,
    val props: Map<String, String>,
    val isVod: Boolean,
    val sourceId: String,
    val sourceName: String = "",
)

data class Program(
    val title: String,
    val desc: String,
    val start: Long,
    val stop: Long,
    val icon: String = "",
)

data class Profile(val id: String, val name: String, val av: Int)


/** Metadatos VOD normalizados desde atributos EXTINF/JSON conservados por el parser. */
fun Channel.vodMeta(vararg keys: String): String {
    for (key in keys) {
        val value = props.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
        if (!value.isNullOrBlank()) return value.trim()
    }
    return ""
}

fun Channel.vodSynopsis(): String = vodMeta(
    "description", "plot", "synopsis", "summary", "tvg-description", "desc", "description_es"
)
fun Channel.vodBackdrop(): String = vodMeta("backdrop", "fanart", "background", "tvg-background", "poster", "cover")
fun Channel.vodYear(): String = vodMeta("year", "release-year", "releasedate", "date")
fun Channel.vodGenre(): String = vodMeta("genre", "category", "genres")
fun Channel.vodDuration(): String = vodMeta("duration", "runtime", "length")
fun Channel.vodRating(): String = vodMeta("rating", "imdb-rating", "score")
