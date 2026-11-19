package com.davidlg.iptvplayer.ui.ott

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import android.view.ViewGroup
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.davidlg.iptvplayer.player.PlaybackFactory
import com.davidlg.iptvplayer.ui.player.ShakaSurface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.davidlg.iptvplayer.MainViewModel
import com.davidlg.iptvplayer.data.Channel
import com.davidlg.iptvplayer.data.EpgStore
import com.davidlg.iptvplayer.ui.Poppins
import com.davidlg.iptvplayer.ui.dlClickable

private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun OttVod(vm: MainViewModel, kind: String, title: String) {
    val tab = when (kind) {
        "movie" -> com.davidlg.iptvplayer.OttTab.Movie
        "series" -> com.davidlg.iptvplayer.OttTab.Series
        "anime" -> com.davidlg.iptvplayer.OttTab.Anime
        else -> com.davidlg.iptvplayer.OttTab.Fav
    }
    com.davidlg.iptvplayer.ui.vod.VodScreen(vm, tab, false) { vm.ottTab = com.davidlg.iptvplayer.OttTab.Start }
}

/** #liveView — categorías + banner de vista previa + carrusel de canales + guía de programación. */
@Composable
fun OttLive(vm: MainViewModel) {
    val live by vm.live.collectAsState()
    val epgVer by vm.epgVersion.collectAsState()
    val playbackPrefs by vm.playbackPrefs.collectAsState()
    val context = LocalContext.current
    val hostView = LocalView.current
    val showEpgImages = playbackPrefs.str("showEpgImages", "on") != "off"
    val useShaka = playbackPrefs.str("playerEngine", "auto") == "shaka"
    val groups = remember(live) { live.groupBy { it.group.ifBlank { "Sin grupo" } } }
    val names = remember(groups) { listOf("TODOS") + groups.keys.toList() }
    var group by remember { mutableStateOf("TODOS") }
    val list = remember(live, group) { if (group == "TODOS") live else groups[group].orEmpty() }
    val selectedId = vm.selectedLiveChannelId
    val cur = list.firstOrNull { it.id == selectedId } ?: list.firstOrNull()
    DisposableEffect(hostView, cur?.id) {
        hostView.keepScreenOn = cur != null
        onDispose { hostView.keepScreenOn = false }
    }
    LaunchedEffect(cur?.id) { if (cur?.id != selectedId) vm.selectLiveChannel(cur) }
    val prog = remember(cur, epgVer) { cur?.let { EpgStore.now(it) } }
    val next = remember(cur, epgVer) { cur?.let { EpgStore.next(it) } }

    BoxWithConstraints(Modifier.fillMaxSize()) {
      if (maxWidth < 900.dp) {
        var mobileSection by remember { mutableStateOf("channels") }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Filtro de categorías: conserva el lenguaje visual dorado de DavidLG.
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                names.forEach { g ->
                    val active = g == group
                    Box(
                        Modifier.height(36.dp)
                            .dlClickable(RoundedCornerShape(30.dp), Color.White, 2.dp, 1f, null) { group = g }
                            .background(if (active) Color(0x353078C8) else Color(0x18FFFFFF), RoundedCornerShape(30.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(g, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 1)
                    }
                }
            }

            // Reproductor integrado real: mantiene ExoPlayer en la pantalla OTT móvil.
            val inlinePlayer = remember(playbackPrefs.str("bufferMode", "default"), playbackPrefs.str("bitrateMode", "auto")) {
                PlaybackFactory.newPlayer(context, playbackPrefs.str("bufferMode", "default"), playbackPrefs.str("bitrateMode", "auto"))
            }
            DisposableEffect(inlinePlayer) {
                onDispose { inlinePlayer.release() }
            }
            LaunchedEffect(cur?.id, inlinePlayer, useShaka) {
                if (cur != null && !useShaka) PlaybackFactory.play(context, inlinePlayer, cur, 0L)
                else inlinePlayer.stop()
            }
            Box(
                Modifier.fillMaxWidth().height(210.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0x883A9BFF), RoundedCornerShape(17.dp))
            ) {
                if (useShaka && cur != null) {
                    ShakaSurface(cur, Modifier.fillMaxSize())
                } else {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { viewContext ->
                            PlayerView(viewContext).apply {
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                setShutterBackgroundColor(android.graphics.Color.BLACK)
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                this.player = inlinePlayer
                            }
                        },
                        update = { it.player = inlinePlayer }
                    )
                }
                Box(Modifier.align(Alignment.TopStart).padding(9.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xB8000000)).padding(horizontal = 9.dp, vertical = 5.dp)) {
                    Text("EN VIVO", color = OttGold, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = Poppins, letterSpacing = 1.sp)
                }
                Row(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6000000))))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(cur?.name ?: "TV en vivo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(prog?.title ?: "Selecciona un canal", color = OttText, fontSize = 10.sp, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xD9308BFF))
                        .dlClickable(RoundedCornerShape(8.dp), Color.White, 1.dp, 1f, null) { cur?.let { vm.play(it, list) } }
                        .padding(horizontal = 11.dp, vertical = 8.dp)) {
                        Text("⛶", color = Color(0xFF06111F), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
                if (cur == null) {
                    Box(Modifier.fillMaxSize().background(Color(0xAA080808)), contentAlignment = Alignment.Center) {
                        Text("Agrega una lista para comenzar", color = Color.White, fontSize = 12.sp, fontFamily = Poppins)
                    }
                }
            }

            // Pestañas táctiles independientes para canales y programación.
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF101E31)).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("channels" to "Canales", "epg" to "Programación").forEach { (key, label) ->
                    val active = mobileSection == key
                    Box(
                        Modifier.weight(1f).height(42.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (active) Color(0x353078C8) else Color.Transparent)
                            .dlClickable(RoundedCornerShape(9.dp), OttGold, 2.dp, 1f, null) { mobileSection = key },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (active) OttGold else OttText, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                    }
                }
            }

            if (mobileSection == "channels") {
                LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(list, key = { it.id }) { channel ->
                        val currentProgram = remember(channel, epgVer) { EpgStore.now(channel) }
                        val selected = cur?.id == channel.id
                        Row(
                            Modifier.fillMaxWidth().height(76.dp)
                                .dlClickable(RoundedCornerShape(13.dp), OttGold, 2.dp, 1f, null) { vm.selectLiveChannel(channel) }
                                .background(if (selected) Color(0xFF123A60) else Color(0xFF101E31), RoundedCornerShape(13.dp))
                                .then(if (selected) Modifier.border(1.dp, OttGold, RoundedCornerShape(13.dp)) else Modifier)
                                .padding(9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (channel.logo.isNotBlank()) AsyncImage(channel.logo, null, Modifier.size(49.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(channel.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(currentProgram?.title ?: "Sin información EPG", color = OttText, fontSize = 11.sp, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (showEpgImages && currentProgram?.icon?.isNotBlank() == true) AsyncImage(currentProgram.icon, null, Modifier.size(40.dp, 54.dp).clip(RoundedCornerShape(7.dp)), contentScale = ContentScale.Crop)
                            Text(pad3(channel.number), color = OttGold, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = Poppins)
                        }
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(list, key = { it.id }) { channel ->
                        val currentProgram = remember(channel, epgVer) { EpgStore.now(channel) }
                        val nextProgram = remember(channel, epgVer) { EpgStore.next(channel) }
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFF101E31)).padding(11.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                if (channel.logo.isNotBlank()) AsyncImage(channel.logo, null, Modifier.size(38.dp).clip(RoundedCornerShape(7.dp)), contentScale = ContentScale.Fit)
                                Text(channel.name, Modifier.weight(1f), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(pad3(channel.number), color = OttGold, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = Poppins)
                            }
                            if (currentProgram != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                    if (showEpgImages && currentProgram.icon.isNotBlank()) AsyncImage(currentProgram.icon, null, Modifier.size(54.dp, 66.dp).clip(RoundedCornerShape(7.dp)), contentScale = ContentScale.Crop)
                                    Column(Modifier.weight(1f)) {
                                        Text("AHORA", color = OttGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                                        Text(currentProgram.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = Poppins, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text("${fmtAmPm(currentProgram.start)} – ${fmtAmPm(currentProgram.stop)}", color = OttText.copy(alpha = .72f), fontSize = 10.sp, fontFamily = Poppins)
                                    }
                                }
                                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(4.dp)).background(Color(0x332A8FFF))) {
                                    Box(Modifier.fillMaxWidth(EpgStore.progress(currentProgram)).fillMaxHeight().background(OttGold))
                                }
                            }
                            if (nextProgram != null) {
                                Text("SIGUIENTE · ${nextProgram.title}", color = OttText, fontSize = 11.sp, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x25FFFFFF)))
                            Text("REPRODUCIR CANAL", Modifier.dlClickable(RoundedCornerShape(7.dp), OttGold, 1.dp, 1f, null) { vm.play(channel, list) }.padding(vertical = 4.dp), color = OttGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                        }
                    }
                }
            }
        }
      } else {
    // Vista previa de vídeo real para Android TV/tablet; no se limita al logotipo del canal.
    val tvPreviewPlayer = remember(playbackPrefs.str("bufferMode", "default"), playbackPrefs.str("bitrateMode", "auto")) {
        PlaybackFactory.newPlayer(context, playbackPrefs.str("bufferMode", "default"), playbackPrefs.str("bitrateMode", "auto"))
    }
    DisposableEffect(tvPreviewPlayer) { onDispose { tvPreviewPlayer.release() } }
    LaunchedEffect(cur?.id, tvPreviewPlayer, useShaka) {
        if (cur != null && !useShaka) PlaybackFactory.play(context, tvPreviewPlayer, cur, 0L)
        else tvPreviewPlayer.stop()
    }
    Row(Modifier.fillMaxSize().padding(start = 42.dp, end = 42.dp, top = 14.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        // ------------ Categorías ------------
        Column(
            Modifier.width(340.dp).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Color(0xEB0B1728))
                .border(1.dp, Color(0x293078C8), RoundedCornerShape(20.dp)).padding(14.dp)
        ) {
            Text("CATEGORÍAS", color = OttGoldSoft, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, letterSpacing = 3.64.sp, modifier = Modifier.padding(start = 6.dp, top = 6.dp))
            Text("Explora todos nuestros canales por categoría", color = OttText.copy(alpha = 0.6f), fontSize = 13.sp, fontFamily = Poppins, modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(names) { g ->
                    val n = if (g == "TODOS") live.size else groups[g]?.size ?: 0
                    val on = g == group
                    val shape = RoundedCornerShape(14.dp)
                    Row(
                        Modifier.fillMaxWidth().height(54.dp)
                            .dlClickable(shape, Color.White, 3.dp, 1f, null) { group = g }
                            .background(if (on) Color(0x293078C8) else Color(0x0DFFFFFF))
                            .border(1.dp, if (on) OttGold else Color.Transparent, shape)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(g, Modifier.weight(1f), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Box(Modifier.clip(RoundedCornerShape(50)).background(Color(0x26E0C38A)).padding(horizontal = 10.dp, vertical = 2.dp)) {
                            Text(n.toString(), color = OttGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                        }
                    }
                }
            }
        }
        // ------------ Columna derecha ------------
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Banner / vista previa
            Box(
                Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0B1728), Color(0xFF17365A), Color(0xFF0B1728))))
                    .border(1.dp, Color(0x383078C8), RoundedCornerShape(22.dp))
            ) {
                if (cur != null) {
                    // Reproductor en vivo ocupa el lado izquierdo; la información EPG queda a la derecha.
                    if (useShaka) {
                        ShakaSurface(cur, Modifier.align(Alignment.CenterStart).fillMaxWidth(0.43f).fillMaxHeight())
                    } else {
                        AndroidView(
                            modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.43f).fillMaxHeight(),
                            factory = { viewContext -> PlayerView(viewContext).apply {
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                setShutterBackgroundColor(android.graphics.Color.BLACK)
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                player = tvPreviewPlayer
                            } },
                            update = { it.player = tvPreviewPlayer }
                        )
                    }
                    Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.43f).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xE60B1728)))))
                    Row(Modifier.padding(start = 31.dp, top = 28.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.size(68.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x33FFFFFF)), contentAlignment = Alignment.Center) {
                            if (cur.logo.isNotBlank()) AsyncImage(cur.logo, null, Modifier.fillMaxSize().padding(6.dp), contentScale = ContentScale.Fit)
                            else Text("TV", color = OttGold, fontWeight = FontWeight.Black, fontFamily = Poppins, fontSize = 22.sp)
                        }
                        Text(cur.name, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, fontFamily = Poppins, maxLines = 1, modifier = Modifier.width(520.dp), overflow = TextOverflow.Ellipsis)
                    }
                    Box(Modifier.align(Alignment.BottomStart).padding(start = 31.dp, bottom = 26.dp)) {
                        GoldPill("▶  Ver en vivo", 22, 36, 16, Modifier.width(234.dp), 0f) { vm.play(cur, list) }
                    }
                    Column(Modifier.align(Alignment.CenterEnd).padding(end = 30.dp).fillMaxWidth(0.54f)) {
                        Text(prog?.title ?: "Sin información EPG", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 54.sp)
                        Text(
                            (prog?.let { fmtAmPm(it.start) + " – " + fmtAmPm(it.stop) + " · " } ?: "") + cur.group,
                            color = OttCream, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, fontFamily = Poppins, maxLines = 1, modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(prog?.desc.orEmpty(), color = OttText.copy(alpha = 0.78f), fontSize = 24.sp, fontFamily = Poppins, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                    }
                    Row(Modifier.align(Alignment.TopEnd).padding(top = 24.dp, end = 30.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(Color(0xFF3DDC84)))
                        Text("EN VIVO " + pad3(cur.number), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = Poppins, letterSpacing = 2.sp, modifier = Modifier.padding(start = 10.dp))
                    }
                    Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(4.dp).background(Color(0x1FFFFFFF))) {
                        Box(Modifier.fillMaxWidth(EpgStore.progress(prog)).fillMaxHeight().background(OttGold))
                    }
                } else {
                    Text("Sin canales. Agrega una lista en Ajustes.", color = OttText, fontSize = 26.sp, fontFamily = Poppins, modifier = Modifier.align(Alignment.Center))
                }
            }
            // Carrusel de canales
            val rowState = rememberLazyListState()
            LazyRow(Modifier.fillMaxWidth().height(176.dp), state = rowState, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(list.take(400), key = { it.id }) { c ->
                    Column(
                        Modifier.width(190.dp).height(158.dp)
                            .dlClickable(CardShape, OttGold, 3.dp, 1.05f, { if (it) vm.selectLiveChannel(c) }) { vm.play(c, list) }
                            .background(Brush.verticalGradient(listOf(Color(0xFF1F2747), Color(0xFF121731))))
                            .border(1.dp, Color(0x293078C8), CardShape).padding(10.dp)
                    ) {
                        Text(pad3(c.number), color = OttGold, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = Poppins)
                        Box(Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x1FFFFFFF)), contentAlignment = Alignment.Center) {
                            if (c.logo.isNotBlank()) AsyncImage(c.logo, null, Modifier.fillMaxSize().padding(6.dp), contentScale = ContentScale.Fit)
                            else Text("TV", color = OttGold, fontWeight = FontWeight.Black, fontFamily = Poppins, fontSize = 26.sp)
                        }
                        Text(c.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            // Guía de programación
            Column(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp)).background(Color(0xEB0B1728))
                    .border(1.dp, Color(0x293078C8), RoundedCornerShape(20.dp)).padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("▣", color = OttGold, fontSize = 22.sp)
                    Text("Guía de Programación", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black, fontFamily = Poppins)
                    Pill(group)
                    Box(Modifier.weight(1f))
                    Pill("${list.size} CANALES · GUÍA EN VIVO")
                    Pill(esDate("EEE d 'DE' MMM").uppercase())
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
                    Text("CANAL", Modifier.weight(0.31f), color = Color(0xFF8BCBFF), fontSize = 11.sp, fontFamily = Poppins, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Text("AHORA", Modifier.weight(0.36f), color = Color(0xFF8BCBFF), fontSize = 11.sp, fontFamily = Poppins, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Text("SIGUIENTE", Modifier.weight(0.33f), color = Color(0xFF8BCBFF), fontSize = 11.sp, fontFamily = Poppins, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(list.take(300), key = { it.id }) { c -> EpgRow(vm, c, list, epgVer, showEpgImages) { vm.selectLiveChannel(c) } }
                }
            }
        }
    }
      }
    }
}

@Composable
private fun Pill(t: String) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(Color(0x1F3078C8)).border(1.dp, Color(0x40E0C38A), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 5.dp)) {
        Text(t, color = OttGold, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, letterSpacing = 1.sp, maxLines = 1)
    }
}

@Composable
private fun EpgRow(vm: MainViewModel, c: Channel, list: List<Channel>, ver: Int, showImages: Boolean, onSel: () -> Unit) {
    val p = remember(c, ver) { EpgStore.now(c) }
    val n = remember(c, ver) { EpgStore.next(c) }
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().height(88.dp)
            .dlClickable(shape, Color(0xFF56AAFF), 2.dp, 1.015f, { if (it) onSel() }) { vm.play(c, list) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(0.31f).fillMaxHeight().clip(shape)
                .background(Color(0xFF12243B)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(pad3(c.number), color = Color(0xFF66B7FF), fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = Poppins)
            Box(Modifier.size(62.dp, 58.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x1FFFFFFF)), contentAlignment = Alignment.Center) {
                if (c.logo.isNotBlank()) AsyncImage(c.logo, null, Modifier.fillMaxSize().padding(5.dp), contentScale = ContentScale.Fit)
                else Text("TV", color = Color(0xFF66B7FF), fontWeight = FontWeight.Black, fontFamily = Poppins, fontSize = 19.sp)
            }
            Text(c.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Column(
            Modifier.weight(0.36f).fillMaxHeight().clip(shape)
                .background(Color(0xFF17365A)).padding(horizontal = 13.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (p != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(Color(0xFF55D98A)))
                    Text("AHORA · ${fmtAmPm(p.start)} – ${fmtAmPm(p.stop)}", color = Color(0xFFBFE4FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 1)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (showImages && p.icon.isNotBlank()) AsyncImage(p.icon, null, Modifier.size(38.dp, 42.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                    Text(p.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(Modifier.fillMaxWidth().padding(top = 5.dp).height(3.dp).clip(RoundedCornerShape(3.dp)).background(Color(0x553A9BFF))) {
                    Box(Modifier.fillMaxWidth(EpgStore.progress(p)).fillMaxHeight().background(Color(0xFF58B5FF)))
                }
            } else {
                Text("AHORA", color = Color(0xFF8BCBFF), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                Text("Sin información EPG", color = Color.White.copy(alpha = .72f), fontSize = 15.sp, fontFamily = Poppins)
            }
        }
        Column(
            Modifier.weight(0.33f).fillMaxHeight().clip(shape)
                .background(Color(0xFF101F34)).padding(horizontal = 13.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("SIGUIENTE", color = Color(0xFF8BCBFF), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
            if (n != null) {
                Text("${fmtAmPm(n.start)} – ${fmtAmPm(n.stop)}", color = Color(0xFFB9CBE0), fontSize = 11.sp, fontFamily = Poppins, maxLines = 1)
                Text(n.title, color = Color.White.copy(alpha = .92f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = Poppins, maxLines = 2, overflow = TextOverflow.Ellipsis)
            } else Text("Sin siguiente programa", color = Color.White.copy(alpha = .58f), fontSize = 14.sp, fontFamily = Poppins)
        }
    }
}
