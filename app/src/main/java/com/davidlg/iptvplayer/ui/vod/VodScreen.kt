package com.davidlg.iptvplayer.ui.vod

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.davidlg.iptvplayer.MainViewModel
import com.davidlg.iptvplayer.OttTab
import com.davidlg.iptvplayer.data.Channel
import com.davidlg.iptvplayer.data.vodBackdrop
import com.davidlg.iptvplayer.data.vodSynopsis
import com.davidlg.iptvplayer.data.vodYear
import com.davidlg.iptvplayer.data.vodGenre
import com.davidlg.iptvplayer.data.vodDuration
import com.davidlg.iptvplayer.data.vodRating
import com.davidlg.iptvplayer.ui.Poppins
import com.davidlg.iptvplayer.ui.Sans
import com.davidlg.iptvplayer.ui.dlClickable
import org.json.JSONArray

private val SERIES_RX = Regex("""\bs\d{1,2}\s?e\d{1,3}\b|temporada|season|episodio""", RegexOption.IGNORE_CASE)

/** "movie" | "series" | "anime" según grupo y nombre (como ott-vod.js). */
internal fun vodKind(c: Channel): String {
    val g = c.group.lowercase()
    return when {
        g.contains("anime") -> "anime"
        g.contains("serie") || SERIES_RX.containsMatchIn(c.name) || c.url.contains("/series/") -> "series"
        else -> "movie"
    }
}

private fun readFavs(vm: MainViewModel): Set<String> = try {
    val a = JSONArray(vm.prefs.getString("davidlgVodFavorites", "[]")); (0 until a.length()).map { a.getString(it) }.toSet()
} catch (_: Exception) { emptySet() }

private fun writeFavs(vm: MainViewModel, s: Set<String>) = vm.prefs.putString("davidlgVodFavorites", JSONArray(s.toList()).toString())

/** Catálogo VOD. tab: Movie, Series, Anime o Fav (Mi lista). classic=true usa la paleta azul del modo clásico. */
@Composable
fun VodScreen(vm: MainViewModel, tab: OttTab, classic: Boolean = false, onHome: () -> Unit) {
    val all by vm.vod.collectAsState()
    var favs by remember { mutableStateOf(readFavs(vm)) }
    var q by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf<Channel?>(null) }
    val acc = Color(0xFF168BFF)
    val font: FontFamily = Poppins
    val compact = LocalConfiguration.current.screenWidthDp < 900
    BackHandler(detail != null) { detail = null }

    val items = remember(all, tab, q, favs) {
        all.filter {
            when (tab) { OttTab.Movie -> vodKind(it) == "movie"; OttTab.Series -> vodKind(it) == "series"; OttTab.Anime -> vodKind(it) == "anime"; else -> it.id in favs }
        }.filter { q.isBlank() || it.name.contains(q, true) || it.group.contains(q, true) }
    }
    val rows = remember(items) { items.groupBy { it.group.ifBlank { "Sin categoría" } }.toList() }
    val hero = items.firstOrNull()

    Column(Modifier.fillMaxSize().padding(horizontal = if (compact) 12.dp else 64.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
            var f by remember { mutableStateOf(false) }
            Box(Modifier.width(if (compact) 190.dp else 306.dp).height(if (compact) 44.dp else 56.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x14FFFFFF)).border(if (f) 3.dp else 1.dp, if (f) acc else Color(0x33FFFFFF), RoundedCornerShape(14.dp)).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                BasicTextField(q, { q = it }, singleLine = true, textStyle = TextStyle(color = Color.White, fontSize = if (compact) 13.sp else 18.sp, fontFamily = font), cursorBrush = SolidColor(acc), modifier = Modifier.fillMaxWidth().onFocusChanged { f = it.isFocused })
                if (q.isEmpty()) Text("Buscar título o categoría…", color = Color(0x66FFFFFF), fontSize = if (compact) 13.sp else 18.sp, fontFamily = font)
            }
            Chip("‹ INICIO", font, onHome)
        }
        if (hero == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (tab == OttTab.Fav) "Aún no tienes títulos en Mi lista" else "No hay contenido en esta sección", color = Color(0x99FFFFFF), fontSize = 28.sp, fontFamily = font)
            }
        } else LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Box(
                    Modifier.fillMaxWidth().height(if (compact) 286.dp else 410.dp)
                        .padding(top = if (compact) 12.dp else 30.dp)
                        .clip(RoundedCornerShape(if (compact) 18.dp else 26.dp))
                        .background(Color(0xFF0A1728))
                ) {
                    val backdrop = hero.vodBackdrop().ifBlank { hero.logo }
                    if (backdrop.isNotBlank()) AsyncImage(backdrop, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF5071425), Color(0xC9071425), Color(0x30071425)))))
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF5071425)))))
                    Column(
                        Modifier.align(Alignment.BottomStart).padding(horizontal = if (compact) 17.dp else 38.dp, vertical = if (compact) 17.dp else 30.dp)
                    ) {
                        Text("DESTACADO · TU CATÁLOGO", color = Color(0xFF70BDFF), fontSize = if (compact) 10.sp else 14.sp, fontWeight = FontWeight.Bold, fontFamily = font, letterSpacing = 2.2.sp)
                        Text(hero.name, color = Color.White, fontSize = if (compact) 26.sp else 54.sp, fontWeight = FontWeight.ExtraBold, fontFamily = font, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
                        Text(listOf(vodKind(hero).uppercase().replace("MOVIE", "PELÍCULA").replace("SERIES", "SERIE"), hero.vodYear(), hero.vodGenre(), hero.vodDuration()).filter { it.isNotBlank() }.joinToString("  •  "), color = Color(0xFFD6E8FA), fontSize = if (compact) 11.sp else 17.sp, fontFamily = font, modifier = Modifier.padding(top = 5.dp, bottom = 7.dp))
                        if (hero.vodSynopsis().isNotBlank()) Text(hero.vodSynopsis(), color = Color(0xFFE2ECF7), fontSize = if (compact) 11.sp else 16.sp, lineHeight = if (compact) 15.sp else 23.sp, maxLines = if (compact) 2 else 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = if (compact) 330.dp else 780.dp).padding(bottom = 11.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Btn("▶ REPRODUCIR", true, font) { vm.play(hero, items) }
                            Btn("ⓘ DETALLES", false, font) { detail = hero }
                        }
                    }
                }
            }
            items(rows, key = { it.first }) { (g, list) ->
                Column {
                    Text(g, color = Color.White, fontSize = if (compact) 19.sp else 32.sp, fontWeight = FontWeight.Bold, fontFamily = font, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) {
                        items(list.take(150), key = { it.id }) { c -> Poster(c, acc, font) { detail = c } }
                    }
                }
            }
            item { Box(Modifier.height(40.dp)) }
        }
    }

    detail?.let { c ->
        val fav = c.id in favs
        Box(Modifier.fillMaxSize().background(Color(0xE6000000)), contentAlignment = Alignment.Center) {
            Row(Modifier.fillMaxWidth(0.96f).widthIn(max = 1300.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF0C192A)).border(1.dp, acc.copy(alpha = .3f), RoundedCornerShape(24.dp)).padding(if (compact) 12.dp else 36.dp), horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 36.dp)) {
                Box(Modifier.width(if (compact) 92.dp else 280.dp).height(if (compact) 138.dp else 420.dp).clip(RoundedCornerShape(16.dp))) { PosterArt(c, acc, font, 80) }
                Column(Modifier.weight(1f)) {
                    Text(c.name, color = Color.White, fontSize = if (compact) 20.sp else 46.sp, fontWeight = FontWeight.Bold, fontFamily = font, maxLines = 3)
                    Text(listOf(c.group, c.vodYear(), c.vodGenre(), c.vodDuration(), c.vodRating().let { if (it.isNotBlank()) "★ $it" else "" }).filter { it.isNotBlank() }.joinToString("  •  "), color = acc, fontSize = if (compact) 11.sp else 18.sp, fontFamily = font, modifier = Modifier.padding(vertical = 10.dp))
                    Text(c.vodSynopsis().ifBlank { "La fuente del catálogo no proporciona una sinopsis para este título." }, color = Color(0xFFE0EAF7), fontSize = if (compact) 12.sp else 18.sp, lineHeight = if (compact) 17.sp else 27.sp, maxLines = if (compact) 8 else 7, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                    val prog = vm.vodProgress(c.id)
                    Row(Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Btn("▶ REPRODUCIR", true, font) { detail = null; vm.play(c, items, 0L) }
                        if (prog > 10_000) Btn("↻ CONTINUAR", false, font) { detail = null; vm.play(c, items, prog) }
                        Btn(if (fav) "★ EN MI LISTA" else "☆ MI LISTA", false, font) { favs = if (fav) favs - c.id else favs + c.id; writeFavs(vm, favs) }
                        Btn("CERRAR", false, font) { detail = null }
                    }
                }
            }
        }
    }
}

@Composable private fun Chip(t: String, font: FontFamily, onClick: () -> Unit) {
    val compact = LocalConfiguration.current.screenWidthDp < 900
    val s = RoundedCornerShape(50)
    Text(t, color = Color.White, fontSize = if (compact) 12.sp else 20.sp, fontWeight = FontWeight.Bold, fontFamily = font,
        modifier = Modifier.dlClickable(s, Color.White, 3.dp, 1.03f, null, onClick).background(Color(0x14FFFFFF)).border(1.dp, Color(0x33FFFFFF), s).padding(horizontal = if (compact) 12.dp else 24.dp, vertical = if (compact) 9.dp else 14.dp))
}

@Composable private fun Btn(t: String, primary: Boolean, font: FontFamily, onClick: () -> Unit) {
    val compact = LocalConfiguration.current.screenWidthDp < 900
    val s = RoundedCornerShape(8.dp)
    Text(t, color = if (primary) Color.Black else Color.White, fontSize = if (compact) 11.sp else 22.sp, fontWeight = FontWeight.Bold, fontFamily = font,
        modifier = Modifier.dlClickable(s, Color(0xFF168BFF), 3.dp, 1.04f, null, onClick).background(if (primary) Color.White else Color(0xFF183A60)).padding(horizontal = if (compact) 10.dp else 26.dp, vertical = if (compact) 8.dp else 16.dp))
}

@Composable private fun PosterArt(c: Channel, acc: Color, font: FontFamily, fs: Int) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF17395F), Color(0xFF081323)))), contentAlignment = Alignment.Center) {
        val art = c.vodBackdrop().ifBlank { c.logo }
        if (art.isNotBlank()) AsyncImage(art, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text(c.name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }.uppercase(), color = acc, fontSize = fs.sp, fontWeight = FontWeight.Black, fontFamily = font)
    }
}

@Composable private fun Poster(c: Channel, acc: Color, font: FontFamily, onClick: () -> Unit) {
    val compact = LocalConfiguration.current.screenWidthDp < 900
    val s = RoundedCornerShape(12.dp)
    Box(Modifier.size(if (compact) 116.dp else 210.dp, if (compact) 174.dp else 315.dp).dlClickable(s, acc, 3.dp, 1.06f, null, onClick)) {
        PosterArt(c, acc, font, 72)
        Text(vodKind(c).uppercase().replace("MOVIE", "PELÍCULA").replace("SERIES", "SERIE"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = font,
            modifier = Modifier.padding(10.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xCC000000)).padding(horizontal = 8.dp, vertical = 3.dp))
        Text(c.name, color = Color.White, fontSize = if (compact) 10.sp else 16.sp, fontFamily = font, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))).padding(10.dp))
    }
}
