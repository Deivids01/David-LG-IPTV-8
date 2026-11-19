package com.davidlg.iptvplayer.ui.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.davidlg.iptvplayer.MainViewModel
import com.davidlg.iptvplayer.Screen
import com.davidlg.iptvplayer.data.Channel
import com.davidlg.iptvplayer.data.EpgStore
import com.davidlg.iptvplayer.ui.Sans
import com.davidlg.iptvplayer.ui.dlClickable
import com.davidlg.iptvplayer.ui.profiles.ProfileAvatar
import com.davidlg.iptvplayer.ui.vod.ClassicVodTab
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class CTab(val label: String) { Live("TV en vivo"), Guide("Guía"), Vod("Películas y series"), Settings("Ajustes") }

/** Modo clásico (azul, estilo TV tradicional). */
@Composable
fun ClassicShell(vm: MainViewModel) {
    var tab by remember { mutableStateOf(CTab.Live) }
    val profile by vm.activeProfile.collectAsState()
    val playbackPrefs by vm.playbackPrefs.collectAsState()
    SideEffect { ClassicTimePrefs.use24h = playbackPrefs.str("timeFormat", "12") == "24" }
    val live by vm.live.collectAsState()
    val ids by vm.activeIds.collectAsState()
    val src by vm.sourceFilter.collectAsState()
    var clock by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { while (true) { clock = SimpleDateFormat(if (ClassicTimePrefs.use24h) "HH:mm" else "h:mm a", Locale("es", "MX")).format(Date()); delay(1000) } }

    val compact = LocalConfiguration.current.screenWidthDp < 900
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B1E33), Color(0xFF070F1B), Color(0xFF050A12))))) {
        if (compact) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp)) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    CTab.values().forEach { t ->
                        val on = t == tab
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).dlClickable(RoundedCornerShape(8.dp), Color.White, 2.dp, 1f) { if (t == CTab.Settings) vm.screen = Screen.Settings else tab = t }
                            .background(if (on) Color(0xFF173D63) else Color.Transparent).padding(horizontal = 9.dp, vertical = 8.dp)) {
                            Text(t.label, color = if (on) Color.White else Color(0xFFA0AEBF), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1)
                        }
                    }
                    Text(clock, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, modifier = Modifier.padding(horizontal = 7.dp))
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).dlClickable(RoundedCornerShape(8.dp), Color.White, 2.dp, 1f) { vm.showProfileSwitcher() }.background(Color(0x1AFFFFFF)).padding(horizontal = 8.dp, vertical = 6.dp)) {
                        Text(profile.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                    }
                }
                Text(if (src == "all") "Fuente: Todas las listas · ${ids.size}" else "Fuente: ${vm.playlists.value.firstOrNull { it.id == src }?.name ?: "Lista"}",
                    color = ClassicDim, fontSize = 10.sp, fontFamily = Sans, modifier = Modifier.padding(top = 3.dp).dlClickable(RoundedCornerShape(6.dp), Color.White, 1.dp, 1f) { vm.cycleSource() })
            }
        } else {
            Row(Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                CTab.values().forEach { t ->
                    val on = t == tab
                    Column(Modifier.padding(end = 28.dp).dlClickable(RoundedCornerShape(10.dp), Color.White, 3.dp, 1f) { if (t == CTab.Settings) vm.screen = Screen.Settings else tab = t }.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text(t.label, color = if (on) Color.White else Color(0xFFA0AEBF), fontSize = 34.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                        Box(Modifier.padding(top = 4.dp).width(if (on) 152.dp else 0.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(ClassicAcc))
                    }
                }
                Box(Modifier.weight(1f))
                Box(Modifier.dlClickable(RoundedCornerShape(14.dp), Color.White, 3.dp, 1f) { vm.cycleSource() }.background(Color(0x1AFFFFFF)).padding(horizontal = 24.dp, vertical = 10.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FUENTE", color = ClassicDim, fontSize = 13.sp, fontFamily = Sans, letterSpacing = 3.sp)
                        Text(if (src == "all") "Todas las listas · ${ids.size}" else (vm.playlists.value.firstOrNull { it.id == src }?.name ?: "Lista"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                    }
                }
                Text(clock, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, modifier = Modifier.padding(horizontal = 30.dp))
                Row(Modifier.dlClickable(RoundedCornerShape(14.dp), Color.White, 3.dp, 1f) { vm.showProfileSwitcher() }.background(Color(0x1AFFFFFF)).padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileAvatar(profile.av, 44.dp)
                    Text(profile.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                }
            }
        }
        Box(Modifier.fillMaxSize()) {
            when (tab) {
                CTab.Live -> ClassicLive(vm, live)
                CTab.Guide -> ClassicGuide(vm, live)
                CTab.Vod -> ClassicVodTab(vm)
                CTab.Settings -> {}
            }
        }
    }
}

@Composable
private fun ClassicLive(vm: MainViewModel, live: List<Channel>) {
    if (LocalConfiguration.current.screenWidthDp < 900) {
        ClassicLiveCompact(vm, live)
        return
    }
    val epgVer by vm.epgVersion.collectAsState()
    val groups = remember(live) { live.groupBy { it.group.ifBlank { "Sin grupo" } } }
    var group by remember { mutableStateOf("") }
    val list = remember(live, group) { if (group.isEmpty()) live else groups[group].orEmpty() }
    var sel by remember { mutableStateOf<Channel?>(null) }
    val cur = sel?.takeIf { c -> list.any { it.id == c.id } } ?: list.firstOrNull()
    val now = remember(cur, epgVer) { cur?.let { EpgStore.now(it) } }
    val next = remember(cur, epgVer) { cur?.let { EpgStore.next(it) } }

    Column(Modifier.fillMaxSize().padding(horizontal = 56.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(Modifier.fillMaxWidth().height(315.dp), horizontalArrangement = Arrangement.spacedBy(46.dp)) {
            Box(
                Modifier.width(560.dp).fillMaxHeight().clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(cur?.let { classicHue(it.name) } ?: Color(0xFF334), Color(0xFF1A0F1E))))
            ) {
                if (cur != null) {
                    ClassicLogo(cur.logo, cur.name, 210.dp, 170.dp, 72f, Modifier.align(Alignment.Center))
                    Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0x99040B14)).padding(22.dp)) {
                        Text("EN VIVO", color = ClassicAcc, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                        Text(cur.name, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Selecciona para reproducir", color = Color(0xFFCCD6E2), fontSize = 16.sp, fontFamily = Sans)
                    }
                } else Text("Agrega una lista para comenzar", color = Color.White, fontSize = 22.sp, fontFamily = Sans, modifier = Modifier.align(Alignment.Center))
            }
            if (cur != null) Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(classicPad(cur.number), color = ClassicAcc, fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, modifier = Modifier.border(2.dp, ClassicAcc, RoundedCornerShape(10.dp)).padding(horizontal = 18.dp, vertical = 4.dp))
                    Text(cur.name, color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                    Text(cur.group, color = Color(0xFFB5C2D3), fontSize = 22.sp, fontFamily = Sans, modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0x1AFFFFFF)).padding(horizontal = 14.dp, vertical = 4.dp))
                }
                Text(now?.title ?: "Sin información EPG", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, modifier = Modifier.padding(top = 22.dp), maxLines = 1)
                if (now != null) {
                    Text(classicHm(now.start) + " – " + classicHm(now.stop), color = Color(0xFFB5C2D3), fontSize = 26.sp, fontFamily = Sans, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                    ClassicBar(classicProgress(now.start, now.stop), 8.dp, Modifier.width(760.dp))
                    Text(now.desc, color = Color(0xFFB5C2D3), fontSize = 24.sp, fontFamily = Sans, maxLines = 2, modifier = Modifier.padding(top = 14.dp))
                }
                next?.let { Text("A continuación · ${classicHm(it.start)} ${it.title}", color = ClassicAcc, fontSize = 26.sp, fontFamily = Sans, modifier = Modifier.padding(top = 16.dp)) }
            }
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            LazyColumn(Modifier.width(410.dp).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(Color(0x990A1424))) {
                itemsIndexed(listOf("") + groups.keys.toList()) { _, g ->
                    val on = g == group
                    Row(
                        Modifier.fillMaxWidth().height(74.dp).dlClickable(RoundedCornerShape(0.dp), Color.White, 3.dp, 1f, { if (it) { group = g; sel = null } }) { group = g }
                            .background(if (on) Color(0xFF16324F) else Color.Transparent).padding(horizontal = 30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (g.isEmpty()) "Todos los canales" else g, Modifier.weight(1f), color = if (on) Color.White else Color(0xFFA9B6C6), fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text((if (g.isEmpty()) live.size else groups[g]?.size ?: 0).toString(), color = ClassicDim, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                    }
                }
            }
            val st = rememberLazyListState()
            LazyColumn(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(Color(0x66081019)), state = st) {
                itemsIndexed(list, key = { _, c -> c.id }) { _, c ->
                    val p = remember(c, epgVer) { EpgStore.now(c) }
                    val on = c.id == cur?.id
                    Row(
                        Modifier.fillMaxWidth().height(102.dp).dlClickable(RoundedCornerShape(0.dp), ClassicAcc, 3.dp, 1f, { if (it) sel = c }) { vm.play(c, list) }
                            .background(if (on) Color(0xFF16324F) else Color.Transparent).padding(horizontal = 30.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(30.dp),
                    ) {
                        Text(classicPad(c.number), Modifier.width(72.dp), color = if (on) Color.White else Color(0xFFA9B6C6), fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                        ClassicLogo(c.logo, c.name, 92.dp, 70.dp, 36f)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(p?.let { classicHm(it.start) + " " + it.title } ?: "Sin información EPG", color = Color(0xFFA9B6C6), fontSize = 24.sp, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (p != null) ClassicBar(classicProgress(p.start, p.stop), 5.dp, Modifier.width(560.dp).padding(top = 4.dp))
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x14FFFFFF)))
                }
            }
        }
    }
}

@Composable
private fun ClassicLiveCompact(vm: MainViewModel, live: List<Channel>) {
    val epgVer by vm.epgVersion.collectAsState()
    val groups = remember(live) { live.groupBy { it.group.ifBlank { "Sin grupo" } } }
    var group by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Channel?>(null) }
    val list = remember(live, group) { if (group.isBlank()) live else groups[group].orEmpty() }
    val current = selected?.takeIf { c -> list.any { it.id == c.id } } ?: list.firstOrNull()
    val now = remember(current, epgVer) { current?.let { EpgStore.now(it) } }
    val next = remember(current, epgVer) { current?.let { EpgStore.next(it) } }

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(0.9f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(current?.let { classicHue(it.name) } ?: Color(0xFF243B58), Color(0xFF111827))))) {
                if (current != null) {
                    ClassicLogo(current.logo, current.name, 88.dp, 76.dp, 34f, Modifier.align(Alignment.Center))
                    Text("EN VIVO · ${current.name}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Sans,
                        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0x99040B14)).padding(9.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (current != null) Column(Modifier.weight(1.1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text("${classicPad(current.number)} · ${current.name}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(now?.title ?: "Sin información EPG", color = Color.White, fontSize = 13.sp, fontFamily = Sans, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                if (now != null) {
                    Text("${classicHm(now.start)} – ${classicHm(now.stop)}", color = ClassicDim, fontSize = 11.sp, fontFamily = Sans)
                    ClassicBar(classicProgress(now.start, now.stop), 5.dp, Modifier.fillMaxWidth().padding(top = 5.dp))
                }
                next?.let { Text("Sigue: ${it.title}", color = ClassicAcc, fontSize = 11.sp, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp)) }
            }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            (listOf("" to "Todos (${live.size})") + groups.map { it.key to "${it.key} (${it.value.size})" }).forEach { (key, label) ->
                val active = group == key
                Box(Modifier.clip(RoundedCornerShape(18.dp)).background(if (active) Color(0xFF267CCB) else Color(0xFF172A43)).dlClickable(RoundedCornerShape(18.dp), Color.White, 2.dp, 1f) { group = key; selected = null }.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1)
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(Color(0x66101D30))) {
            itemsIndexed(list, key = { _, c -> c.id }) { _, c ->
                val program = remember(c, epgVer) { EpgStore.now(c) }
                val active = c.id == current?.id
                Row(Modifier.fillMaxWidth().height(72.dp).dlClickable(RoundedCornerShape(0.dp), ClassicAcc, 2.dp, 1f, { if (it) selected = c }) { vm.play(c, list) }
                    .background(if (active) Color(0xFF174B79) else Color.Transparent).padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(classicPad(c.number), color = ClassicDim, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, modifier = Modifier.width(34.dp))
                    ClassicLogo(c.logo, c.name, 42.dp, 42.dp, 18f)
                    Column(Modifier.weight(1f)) {
                        Text(c.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(program?.let { "${classicHm(it.start)}  ${it.title}" } ?: "Sin información EPG", color = ClassicDim, fontSize = 10.sp, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (program != null) ClassicBar(classicProgress(program.start, program.stop), 3.dp, Modifier.fillMaxWidth().padding(top = 3.dp))
                    }
                    Text("▶", color = ClassicAcc, fontSize = 16.sp)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x18FFFFFF)))
            }
        }
    }
}

@Composable
private fun ClassicGuide(vm: MainViewModel, live: List<Channel>) {
    val epgVer by vm.epgVersion.collectAsState()
    val compact = LocalConfiguration.current.screenWidthDp < 900
    val now = System.currentTimeMillis()
    val from = now - 30 * 60_000L
    val until = now + 3 * 60 * 60_000L
    val channels = remember(live) { live.take(250) }
    if (compact) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
            itemsIndexed(channels, key = { _, c -> c.id }) { _, c ->
                val programs = remember(c, epgVer, from / 60_000L) { EpgStore.schedule(c, from, until) }
                Column(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF10243C)).padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(classicPad(c.number), color = ClassicAcc, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                        ClassicLogo(c.logo, c.name, 34.dp, 30.dp, 14f)
                        Text(c.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (programs.isEmpty()) Text("Sin información EPG", color = ClassicDim, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp))
                    else programs.forEach { pg ->
                        Text("${classicHm(pg.start)}–${classicHm(pg.stop)}  ${pg.title}", color = if (now in pg.start until pg.stop) Color.White else ClassicDim, fontSize = 11.sp, fontFamily = Sans, maxLines = 2, modifier = Modifier.fillMaxWidth().dlClickable(RoundedCornerShape(6.dp), ClassicAcc, 1.dp, 1f) { vm.play(c, live) }.padding(vertical = 5.dp))
                    }
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("CANAL", color = ClassicDim, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, modifier = Modifier.width(230.dp))
                Row(Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                    for (i in 0..6) Text(classicHm(from + i * 30 * 60_000L), color = ClassicDim, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, modifier = Modifier.width(180.dp))
                }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(channels, key = { _, c -> c.id }) { _, c ->
                    val programs = remember(c, epgVer, from / 60_000L) { EpgStore.schedule(c, from, until) }
                    Row(Modifier.fillMaxWidth().height(78.dp).padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.width(230.dp).fillMaxHeight().dlClickable(RoundedCornerShape(7.dp), ClassicAcc, 2.dp, 1f) { vm.play(c, live) }.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(classicPad(c.number), color = ClassicDim, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Sans)
                            ClassicLogo(c.logo, c.name, 42.dp, 42.dp, 18f)
                            Text(c.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Row(Modifier.weight(1f).fillMaxHeight().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            if (programs.isEmpty()) Box(Modifier.width(180.dp).fillMaxHeight().clip(RoundedCornerShape(7.dp)).background(Color(0xFF1A3554)).padding(8.dp), contentAlignment = Alignment.CenterStart) { Text("Sin información", color = ClassicDim, fontSize = 12.sp) }
                            else programs.forEach { pg ->
                                val mins = ((pg.stop - pg.start).coerceAtLeast(15 * 60_000L) / 60_000L).toInt()
                                val w = (mins * 3.0f).coerceIn(90f, 420f).dp
                                Box(Modifier.width(w).fillMaxHeight().clip(RoundedCornerShape(7.dp)).background(if (now in pg.start until pg.stop) Color(0xFF167FC5) else Color(0xFF29496E))
                                    .dlClickable(RoundedCornerShape(7.dp), Color.White, 2.dp, 1f) { vm.play(c, live) }.padding(7.dp), contentAlignment = Alignment.CenterStart) {
                                    Column {
                                        Text(pg.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Sans, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text("${classicHm(pg.start)} – ${classicHm(pg.stop)}", color = Color(0xFFD4E2F0), fontSize = 10.sp, fontFamily = Sans)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
