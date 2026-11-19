# DavidLG IPTV Player — app nativa Android / Android TV (Kotlin + Jetpack Compose + Media3)

Versión 5.7.1 — Blue OTT: guía EPG, VOD cinematográfico y controles de reproducción.

## Novedades 5.7.1
- Guía EPG para pantallas amplias con columnas proporcionales para canal, programa actual y siguiente; evita anchos fijos que deformaban la guía en diferentes televisores.
- Cada fila EPG presenta horario, imagen de programa cuando existe, progreso de emisión y siguiente programa en tarjetas de alto contraste azul.
- Portada VOD cinematográfica con imagen de fondo obtenida de los metadatos del catálogo, degradados para legibilidad, sinopsis y datos disponibles.
- Ficha VOD mantiene póster, metadatos, sinopsis y acciones de reproducir, continuar y favoritos.
- Reproductor VOD incorpora controles visibles de retroceso 10 s, pausa/reanudar y avance 10 s; el mando conserva los atajos de flechas y OK.
- Se conserva la reproducción nativa Media3/ExoPlayer, la opción experimental Shaka y el comportamiento de pantalla encendida.

## Dispositivos y navegación
- Android TV: navegación con mando y filas EPG horizontales.
- Android móvil: interfaz táctil compacta y controles de reproducción visibles.
- Tablet: distribución adaptable entre móvil y pantalla amplia.

## Compilar
Abre el proyecto en Android Studio con JDK 17 y permite que Gradle descargue la distribución 8.14.3 y las dependencias. También está incluido el workflow `.github/workflows/build-apk.yml` para compilar el APK en GitHub Actions.

## Estado de verificación
Este paquete contiene cambios de código y pasó la validación de integridad ZIP. En el entorno de preparación no hay Android SDK ni distribución Gradle disponible y no se pudo acceder a `services.gradle.org`; por tanto, no se declara compilación APK ni pruebas en dispositivos reales.

## Compatibilidad de reproducción
La reproducción depende del formato de la fuente, cabeceras, conectividad, CORS cuando se usa WebView, soporte del dispositivo y licencias autorizadas. Shaka continúa siendo experimental; no se garantiza que todos los flujos o DRM funcionen en todos los dispositivos.

---

# DavidLG IPTV Player — app nativa Android / Android TV (Kotlin + Jetpack Compose + ExoPlayer)

Versión 4.4.0 · TV en vivo móvil con pestañas Canales / Programación

## Qué incluye
- Interfaz nativa estilo streaming: inicio con "Continúa viendo", favoritos, filas por categoría, tarjetas con EPG y barra de progreso.
- Se adapta solo: **Android TV** (barra superior, foco con control remoto), **tablet/teléfono horizontal** (categorías laterales) y **teléfono vertical** (barra inferior).
- Listas **M3U** (URL), **Xtream Codes** (servidor/usuario/contraseña) y guía **EPG XMLTV** (.xml o .gz).
- **TV en vivo** y **Películas y series (VOD)** separadas (por tipo de lista o detección automática).
- Reproductor **ExoPlayer**: HLS, DASH/MPD, TS directo, **ClearKey** (KODIPROP / kid:key) y **Widevine** (licencia por URL), cabeceras User-Agent/Referer/Origin.
- Control remoto: ↑/↓ y CH+/CH− cambian de canal; ← abre la lista de canales; OK controla el reproductor y permite elegir canales.
- Ajustes desplegables para retardo de zapping (instantáneo hasta 30 s), visibilidad del búfer, calidad real SD/HD/FHD, perfiles de búfer y selección de bitrate.
- Adaptación de inicio, TV en vivo, catálogo VOD, reproductor y ajustes para teléfono/tablet; Android TV conserva la navegación con mando.
- Lectura de imágenes XMLTV por `<icon src="...">`, incluidas rutas relativas a la URL de la guía.
- Táctil: toque muestra controles; botones de favorito, lista de canales, pausa y cerrar.

## Compilar
GitHub Actions (workflow incluido): sube el proyecto, ve a *Actions → Build APK → Run workflow* y descarga el APK.
También en Android Studio: *Build → Build APK(s)*. Se recomienda JDK 17 y Gradle Wrapper incluido.

## Pendiente (siguientes etapas)
- Pruebas de compatibilidad en dispositivos Android TV, teléfonos y tablets reales; ajustes finos de foco para mandos de distintos fabricantes.
- Pista de audio/subtítulos avanzada y catch-up.
- Panel administrativo: acceso a contenido/paquetes por usuario (requiere definir el servidor).
- Detalle de películas/series (sinopsis, temporadas) y reanudar VOD.


## 4.3.0 – interfaz adaptable
- Android móvil usa diseño nativo vertical sin lienzo 16:9 forzado.
- Android TV conserva el lienzo horizontal 16:9.
- El diálogo Añadir/Editar lista solicita foco inicial en el primer campo y agrupa el recorrido de foco para mando.


## 4.4.0 – experiencia móvil B
- TV en vivo en móvil reorganizada: filtros de categoría, panel superior de canal/previa, y pestañas independientes Canales y Programación.
- Se conserva la identidad DavidLG: fondo oscuro, acentos dorados, tipografía Poppins y tarjetas de alto contraste.
- La vista Android TV y su guía horizontal se mantienen separadas de la composición móvil.
- El panel superior funciona como acceso directo al reproductor nativo; la reproducción sigue usando la pantalla PlayerScreen existente.


## 4.5.0 — experiencia OTT móvil

- Reproductor en vivo integrado en la pantalla de TV en vivo para móviles y tablets compactas, usando Media3/ExoPlayer y el mismo `PlaybackFactory` del reproductor de pantalla completa.
- Seleccionar un canal desde la lista cambia la reproducción en el panel sin abandonar la pantalla.
- Acción de ampliar abre el reproductor de pantalla completa existente.
- Pestañas separadas para Canales y Programación; se conserva el tema oscuro, los acentos dorados, Poppins y los recursos de marca DavidLG.
- El reproductor integrado libera ExoPlayer al salir de la pantalla y respeta los perfiles de búfer y bitrate configurados.

La compilación debe verificarse en un entorno Android/Gradle conectado; no se pudo descargar Gradle en el entorno de preparación por falta de acceso a `services.gradle.org`.


## Motor Shaka experimental (5.0.0)

En Ajustes > Reproductor se puede seleccionar Automático, ExoPlayer/Media3 o Shaka Player. ExoPlayer sigue siendo el motor predeterminado y el único motor nativo. Shaka se ejecuta en un Android WebView con JavaScript habilitado y carga la biblioteca Shaka desde CDN, por lo que requiere conectividad a Internet. En la versión 5.0.0 inicial, la configuración avanzada DRM y las cabeceras todavía no estaban integradas; la versión 5.2.0 añade el envío de metadatos Widevine/ClearKey y cabeceras descrito abajo. La compatibilidad real sigue dependiendo del dispositivo y requiere pruebas. La UI y navegación de la aplicación permanecen en Jetpack Compose.


## 5.1.0 — sesión compartida de reproducción

- La selección del canal en TV en vivo se conserva en el estado compartido del ViewModel al cambiar entre motores y al recomponer la pantalla OTT.
- ShakaSurface detecta cambios de URL en la misma vista WebView y vuelve a cargar el contenido; esto evita que la vista previa conserve la URL del canal anterior.
- ExoPlayer sigue siendo el motor nativo predeterminado; Shaka continúa siendo experimental y requiere conexión para cargar la biblioteca desde CDN.
- La compilación APK debe verificarse en un entorno Android/Gradle con la distribución Gradle 8.14.3 disponible.


## 5.2.0 — metadatos DRM y cabeceras para ambos motores

- Shaka recibe cabeceras de flujo desde `inputstream.adaptive.stream_headers`, `referer`, `origin` y `user_agent`; las cabeceras se aplican a manifiestos y segmentos.
- Para Widevine, Shaka configura `com.widevine.alpha` con la URL de licencia y añade las cabeceras indicadas en el formato Kodi `URL|cabeceras|...`, además de las cabeceras generales del canal.
- Para ClearKey, Shaka convierte pares `kid:key` y los campos `kid`/`key` a claves JWK en formato Base64URL, sin enviar claves locales a un servidor externo.
- ExoPlayer conserva su configuración DRM Media3: Widevine con URL/cabeceras de licencia y ClearKey local o mediante URL de licencia.
- La compatibilidad Widevine de Shaka depende del soporte EME/CDM que ofrezca la versión de Android System WebView y el dispositivo. Esta integración no puede garantizar reproducción de contenido protegido en todos los equipos. DRM, licencias y fuentes deben estar autorizadas por el proveedor.
- No se implementa evasión de DRM, extracción de claves ni descifrado de contenido protegido.

## 5.3.0 — Playback reliability and keep-awake

- Shaka WebView now tries two CDN endpoints for its JavaScript library and displays actionable startup/playback errors instead of leaving a silent black screen.
- The Shaka document uses the stream's origin as its base URL rather than always using `https://localhost/`, which can avoid unnecessary cross-origin/CORS rejection when the stream host serves its own manifests and segments.
- WebView playback settings explicitly allow network loading, hardware rendering, inline playback, and compatibility-mode mixed content. Console and main-frame load errors are logged with the `DavidLG-Shaka` tag.
- The app requests that Android keep the display on while the full-screen player is open. The OTT live preview also requests keep-awake while a channel is selected; the normal system timeout is restored when leaving those screens.
- This is a code-level correction, not a claim that every Shaka stream/DRM combination is supported. The stream host must still permit the WebView origin through CORS where required, and Android System WebView must support the necessary MSE/EME capabilities. Widevine playback depends on the device's supported DRM/CDM and an authorized license.

## 5.4.0 — Responsive classic interface

- Adapted the classic navigation header for narrow screens; tabs scroll horizontally instead of overflowing.
- Added a dedicated compact live-TV layout for phones and portrait tablets, with channel categories, logos, current programme and progress.
- Reworked the classic EPG into a time-based programme grid on wide screens and a compact schedule on narrow screens.
- Made the profile carousel horizontally scrollable on TV so the Add Profile card remains reachable when there are several profiles.
- Added unique profile IDs and visible feedback after successful or failed profile creation.
- Settings options now use clearer card surfaces and focus feedback for remote navigation.

The changes are source-level updates. APK compilation and device testing still need to be completed in an Android build environment.


## 5.5.0 — LG-inspired live player information panel

- Reworked the full-screen player information overlay to resemble the reference LG presentation: channel logo and number, channel name, LIVE/VOD and stream-quality badges, current and next EPG programme, current-programme progress, and clock.
- The overlay now uses separate sizing rules for phones, intermediate/tablet widths, and Android TV / wide screens. Phone mode keeps the information compact; TV mode gives the programme information more room and adds remote-control hints.
- The panel appears automatically when a channel is opened or changed and then hides after the existing timeout; touching the screen or using the remote can show it again. Playback remains started by the playback engine and does not depend on pressing OK to start.
- The existing ExoPlayer / Shaka selection, stream playback pipeline, and keep-screen-on behavior are retained.
- This is a source update. ZIP integrity can be checked here, but APK compilation and real-device playback still require an Android build environment and authorized test streams.


## DavidLG IPTV 5.7.0 — rediseño azul OTT (avance)

- Inicio móvil y tablet con portada VOD, sinopsis disponible y carruseles horizontales.
- Inicio para Android TV con portada destacada, información editorial y carruseles.
- Identidad OTT unificada en negro azulado y azul eléctrico.
- Metadatos M3U EXTINF preservados para sinopsis, fondo/poster, año, género, duración y rating.
- Fichas VOD muestran los metadatos que existan en la fuente y no inventan descripciones.
- Vista previa de TV en vivo con reproducción inline en Android TV/tablet y datos EPG.
- Tarjetas de guía móvil muestran horarios y progreso del programa actual.

**Estado de verificación:** ZIP y revisión estática realizados. La compilación Android no pudo ejecutarse en este entorno porque `services.gradle.org` no resolvió por DNS; no se declara APK compilado ni prueba en dispositivo.
