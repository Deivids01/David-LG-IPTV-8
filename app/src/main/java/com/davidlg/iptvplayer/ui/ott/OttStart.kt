package com.davidlg.iptvplayer.ui.ott

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.davidlg.iptvplayer.MainViewModel
import com.davidlg.iptvplayer.OttTab
import com.davidlg.iptvplayer.Screen
import com.davidlg.iptvplayer.data.Channel
import com.davidlg.iptvplayer.data.EpgStore
import com.davidlg.iptvplayer.data.vodBackdrop
import com.davidlg.iptvplayer.data.vodGenre
import com.davidlg.iptvplayer.data.vodSynopsis
import com.davidlg.iptvplayer.data.vodYear
import com.davidlg.iptvplayer.ui.Poppins
import com.davidlg.iptvplayer.ui.dlClickable

internal fun greetingNow(): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return if (h in 5..11) "Buenos días" else if (h in 12..18) "Buenas tardes" else "Buenas noches"
}

/** Inicio OTT renovado: portada real del catálogo, sinopsis y carruseles. */
@Composable
fun OttStart(vm: MainViewModel) {
    val live by vm.live.collectAsState()
    val vod by vm.vod.collectAsState()
    val recents by vm.recents.collectAsState()
    val epgVer by vm.epgVersion.collectAsState()
    val last = remember(recents, live) { vm.recentChannels().firstOrNull() }
    val prog = remember(last, epgVer) { last?.let { EpgStore.now(it) } }
    val featured = remember(vod) { vod.firstOrNull() }
    val compact = LocalConfiguration.current.screenWidthDp < 600
    val medium = LocalConfiguration.current.screenWidthDp in 600..899

    if (compact || medium) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("DAVIDLG", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = Poppins, letterSpacing = 2.sp)
                        Text("STREAMING", color = OttGoldDim, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, letterSpacing = 3.sp)
                    }
                    Text("☰", color = Color.White, fontSize = 22.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF14243A)).clickableCompat { vm.screen = Screen.Settings }.padding(10.dp))
                }
            }
            item {
                if (featured != null) MobileFeatured(featured, compact) { vm.play(featured, vod) }
                else {
                    Box(Modifier.fillMaxWidth().height(if (compact) 230.dp else 300.dp).clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFF123A67), Color(0xFF081323))))) {
                        Column(Modifier.align(Alignment.CenterStart).padding(22.dp)) {
                            Text("TU ENTRETENIMIENTO", color = OttGoldDim, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text(greetingNow(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                            Text("Tus canales y contenido en un solo lugar", color = OttText, fontSize = 13.sp)
                        }
                    }
                }
            }
            if (last != null) item { ContinueWatching(last, prog?.title ?: "En vivo") { vm.play(last, live) } }
            if (vod.isNotEmpty()) item {
                ContentRail("Películas y series", "Explora tu catálogo", vod.take(24), compact) { c -> vm.play(c, vod) }
            }
            if (live.isNotEmpty()) item {
                LiveRail(live.take(24), compact) { c -> vm.play(c, live) }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction("TV EN VIVO", "Canales y guía", Modifier.weight(1f)) { vm.ottTab = OttTab.Live }
                    QuickAction("MI LISTA", "Tus favoritos", Modifier.weight(1f)) { vm.ottTab = OttTab.Fav }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 68.dp, end = 68.dp, top = 30.dp, bottom = 50.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item {
                if (featured != null) TvFeatured(featured) { vm.play(featured, vod) }
                else Box(Modifier.fillMaxWidth().height(340.dp).clip(RoundedCornerShape(26.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF102D50), Color(0xFF081323))))) {
                    Column(Modifier.align(Alignment.CenterStart).padding(42.dp)) {
                        Text("DAVIDLG IPTV", color = OttGoldDim, fontSize = 14.sp, letterSpacing = 4.sp)
                        Text(greetingNow(), color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                        Text("Tu entretenimiento en un solo lugar", color = OttText, fontSize = 20.sp)
                    }
                }
            }
            if (last != null) item { ContinueWatching(last, prog?.title ?: "En vivo") { vm.play(last, live) } }
            if (vod.isNotEmpty()) item { ContentRail("Películas y series", "Descubre tu catálogo", vod.take(36), false) { c -> vm.play(c, vod) } }
            if (live.isNotEmpty()) item { LiveRail(live.take(30), false) { c -> vm.play(c, live) } }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAction("TV EN VIVO", "Explora categorías y canales", Modifier.weight(1f)) { vm.ottTab = OttTab.Live }
                    QuickAction("MI LISTA", "Tus canales y títulos guardados", Modifier.weight(1f)) { vm.ottTab = OttTab.Fav }
                    QuickAction("AJUSTES", "Personaliza tu experiencia", Modifier.weight(1f)) { vm.screen = Screen.Settings }
                }
            }
        }
    }
}

@Composable private fun MobileFeatured(c: Channel, compact: Boolean, onPlay: () -> Unit) {
    val art = c.vodBackdrop().ifBlank { c.logo }
    Box(Modifier.fillMaxWidth().height(if (compact) 330.dp else 390.dp).clip(RoundedCornerShape(22.dp)).background(Color(0xFF10243C))) {
        if (art.isNotBlank()) AsyncImage(art, contentDescription = c.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color(0x22000000), .35f to Color(0x88071322), 1f to Color(0xFF07111F))))
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(20.dp)) {
            Text("DESTACADO", color = OttGoldDim, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(c.name, color = Color.White, fontSize = if (compact) 26.sp else 32.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val meta = listOf(c.vodYear(), c.vodGenre()).filter { it.isNotBlank() }.joinToString("  •  ")
            if (meta.isNotBlank()) Text(meta, color = Color(0xFFD0D9E6), fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
            val synopsis = c.vodSynopsis()
            if (synopsis.isNotBlank()) Text(synopsis, color = Color(0xFFE2EAF5), fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
            Text("▶  REPRODUCIR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp).clip(RoundedCornerShape(50)).background(Color(0xFF087BFA)).clickableCompat(onPlay).padding(horizontal = 20.dp, vertical = 11.dp))
        }
    }
}

@Composable private fun TvFeatured(c: Channel, onPlay: () -> Unit) {
    val art = c.vodBackdrop().ifBlank { c.logo }
    Row(Modifier.fillMaxWidth().height(360.dp).clip(RoundedCornerShape(26.dp)).background(Color(0xFF0D1B2D))) {
        Column(Modifier.weight(1f).fillMaxHeight().padding(38.dp), verticalArrangement = Arrangement.Center) {
            Text("DAVIDLG ORIGINAL · DESTACADO", color = OttGoldDim, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text(c.name, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 14.dp))
            val meta = listOf(c.vodYear(), c.vodGenre()).filter { it.isNotBlank() }.joinToString("  •  ")
            if (meta.isNotBlank()) Text(meta, color = OttGoldDim, fontSize = 17.sp, modifier = Modifier.padding(top = 10.dp))
            Text(c.vodSynopsis().ifBlank { "Descubre este título de tu catálogo." }, color = OttText, fontSize = 17.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 12.dp))
            Text("▶  REPRODUCIR", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp).clip(RoundedCornerShape(50)).background(Color(0xFF087BFA)).clickableCompat(onPlay).padding(horizontal = 24.dp, vertical = 13.dp))
        }
        if (art.isNotBlank()) AsyncImage(art, contentDescription = c.name, modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
    }
}

@Composable private fun ContinueWatching(c: Channel, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF101E31)).border(1.dp, Color(0x332A8FFF), RoundedCornerShape(16.dp)).clickableCompat(onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        if (c.logo.isNotBlank()) AsyncImage(c.logo, null, Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Fit)
        Column(Modifier.weight(1f)) {
            Text("CONTINUAR VIENDO", color = OttGoldDim, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(c.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = OttText.copy(alpha = .75f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("▶", color = Color.White, fontSize = 18.sp, modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF087BFA)).padding(horizontal = 13.dp, vertical = 9.dp))
    }
}

@Composable private fun ContentRail(title: String, subtitle: String, list: List<Channel>, compact: Boolean, onSelect: (Channel) -> Unit) {
    Column {
        Column(Modifier.padding(bottom = 12.dp)) {
            Text(title, color = Color.White, fontSize = if (compact) 19.sp else 28.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
            Text(subtitle, color = OttText.copy(alpha = .6f), fontSize = if (compact) 11.sp else 14.sp)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 18.dp)) {
            items(list, key = { it.id }) { c ->
                Column(Modifier.width(if (compact) 116.dp else 170.dp).clickableCompat { onSelect(c) }) {
                    Box(Modifier.fillMaxWidth().height(if (compact) 164.dp else 240.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF172A43))) {
                        val art = c.vodBackdrop().ifBlank { c.logo }
                        if (art.isNotBlank()) AsyncImage(art, c.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text(c.name.take(2).uppercase(), color = OttGoldDim, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                    }
                    Text(c.name, color = Color.White, fontSize = if (compact) 11.sp else 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
                }
            }
        }
    }
}

@Composable private fun LiveRail(list: List<Channel>, compact: Boolean, onSelect: (Channel) -> Unit) {
    Column {
        Text("TV EN VIVO", color = Color.White, fontSize = if (compact) 19.sp else 28.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, modifier = Modifier.padding(bottom = 12.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list, key = { it.id }) { c ->
                Row(Modifier.width(if (compact) 190.dp else 250.dp).height(76.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF101E31)).clickableCompat { onSelect(c) }.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (c.logo.isNotBlank()) AsyncImage(c.logo, null, Modifier.size(48.dp), contentScale = ContentScale.Fit)
                    Column(Modifier.weight(1f)) {
                        Text(c.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("EN VIVO", color = OttGoldDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable private fun QuickAction(title: String, desc: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.height(92.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF101E31)).border(1.dp, Color(0x332A8FFF), RoundedCornerShape(14.dp)).clickableCompat(onClick).padding(14.dp), verticalArrangement = Arrangement.Center) {
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(desc, color = OttText.copy(alpha = .65f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

private fun Modifier.clickableCompat(onClick: () -> Unit): Modifier = this.then(
    Modifier.dlClickable(RoundedCornerShape(12.dp), Color(0xFF58B7FF), 2.dp, 1.02f, null, onClick)
)
