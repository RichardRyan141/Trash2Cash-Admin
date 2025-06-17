package com.example.fp_imk_admin.manajemen_lokasi

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fp_imk_admin.LocationSessionManager
import com.example.fp_imk_admin.UserSessionManager
import com.example.fp_imk_admin.data.Location
import kotlin.collections.indexOf

class ListLocationActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationsState = mutableStateListOf<Location>()
        val isLoaded = mutableStateOf(false)

        LocationSessionManager.getAllLocations(
            onSuccess = { locList ->
                locationsState.clear()
                locationsState.addAll(locList)
                isLoaded.value = true
                Log.d("LocationScreen", "Locations: $locList")
            },
            onError = { error ->
                Log.e("LocationScreen", "Database error: ${error.message}")
                isLoaded.value = true
            }
        )

        setContent {
            if (isLoaded.value && locationsState.isNotEmpty()) {
                ListLocationScreen(locationsState)
            } else {
                LoadingScreen() // show loading or fallback UI
            }
        }
    }
}
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("Memuat lokasi...", fontSize = 20.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListLocationScreen(locations: SnapshotStateList<Location>) {
    val context = LocalContext.current
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    var selectedMarker by remember { mutableStateOf<Location?>(null) }
    val listState = rememberLazyListState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedInfo by remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(selectedMarker) {
        val index = locations.indexOf(selectedMarker)
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    val html = remember(locations) {
        if (locations.isEmpty()) return@remember null  // return null if not ready

        val jsArray = locations.joinToString(",") {
            "[${it.lat}, ${it.long}, '${it.namaLokasi}']"
        }

        """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
      <style>
        html, body, #map { height: 100%; margin: 0; padding: 0; }
      </style>
    </head>
    <body>
      <div id="map"></div>
      <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
      <script>
        var map = L.map('map').setView([${locations.first().lat}, ${locations.first().long}], 13);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; OpenStreetMap contributors'
        }).addTo(map);

        var markers = [];
        var locations = [$jsArray];
        locations.forEach(function(loc) {
          var marker = L.marker([loc[0], loc[1]]).addTo(map);
          marker.bindPopup(loc[2]);
          markers.push(marker);
        });

        function moveToLocation(lat, lng) {
          map.setView([lat, lng], 16);
        }
      </script>
    </body>
    </html>
    """.trimIndent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daftar Lokasi",
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = {
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webChromeClient = WebChromeClient()
                        webViewClient = WebViewClient()

                        webViewRef.value = this
                    }
                },
                update = { webView ->
                    html?.let {
                        webView.loadDataWithBaseURL(null, it, "text/html", "utf-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Daftar Lokasi:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    itemsIndexed(locations) { index, loc ->
                        val isSelected = selectedMarker == loc
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFB3E5FC) else Color(0xFFE0E0E0),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxWidth()
                                .clickable {
                                    selectedMarker = loc
                                    selectedInfo = loc
                                    showDialog = true
                                    val js = "moveToLocation(${loc.lat}, ${loc.long});"
                                    webViewRef.value?.evaluateJavascript(js, null)
                                }
                        ) {
                            Text(
                                text = loc.namaLokasi,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                if (showDialog && selectedInfo != null) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = {
                            Text(text = selectedInfo!!.namaLokasi, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        },
                        text = {
                            Text("Alamat: ${selectedInfo!!.alamat}", fontSize = 20.sp)
                        },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Tutup", fontSize = 16.sp)
                            }
                        }
                    )
                }

            }
        }
    }
}