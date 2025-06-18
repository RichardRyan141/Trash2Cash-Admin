package com.example.fp_imk_admin

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fp_imk_admin.data.User


fun saveStringLocally(context: Context, tag: String, str: String) {
    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    prefs.edit().putString(tag, str).apply()
}

fun getStringLocally(context: Context, tag: String): String? {
    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return prefs.getString(tag, null)
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("Mengambil data...", fontSize = 20.sp)
    }
}

@Composable
fun UserSearchDropdown(
    userList: List<User>,
    onUserSelected: (User?) -> Unit
) {
    var queryValue by remember { mutableStateOf(TextFieldValue("")) }

    val filteredUsers = userList.filter {
        it.role == "user" && it.username.contains(queryValue.text, ignoreCase = true)
    }.take(5)

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = queryValue,
            onValueChange = { queryValue = it },
            label = { Text("Search Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (queryValue.text.isNotEmpty()) {
                    IconButton(onClick = {
                        queryValue = TextFieldValue("")
                        onUserSelected(null)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        )

        if (queryValue.text.isNotBlank() && filteredUsers.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .background(Color.White)
            ) {
                filteredUsers.forEach { user ->
                    Text(
                        text = user.username,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newText = user.username
                                queryValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newText.length)
                                )
                                onUserSelected(user)
                            }
                            .padding(12.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChartSection(title: String, data: Map<String, Int>, chartType: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    when (chartType) {
        "VertBar" -> SimpleVertiBarChart(data)
        "HorizBar" -> SimpleHorizBarChart(data)
        "Pie" -> SimplePieChart(data)
        "Line" -> SimpleLineChart(data)
    }
}

@Composable
fun SimpleVertiBarChart(data: Map<String, Int>) {
    val maxVal = data.values.maxOrNull()?.toFloat() ?: 1f
    val barWidth = 40.dp
    val spacing = 16.dp
    val barColor = Color(0xFF4CAF50)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomStart),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                data.forEach { (label, value) ->
                    val barHeightRatio = value / maxVal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .height((barHeightRatio * 200f).dp)
                                .width(barWidth)
                                .background(barColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 16.sp, maxLines = 1)
                        Text("Rp$value", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}


@Composable
fun SimpleHorizBarChart(data: Map<String, Int>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        data.forEach { (label, value) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(label, modifier = Modifier.width(120.dp), fontSize = 14.sp)
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width((value / 1000).coerceAtMost(300).dp)
                        .background(Color(0xFF2196F3))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rp$value", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SimplePieChart(data: Map<String, Int>) {
    val total = data.values.sum().toFloat()
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan)

    Canvas(modifier = Modifier
        .size(200.dp)
        .padding(8.dp)) {
        var startAngle = -90f
        data.entries.forEachIndexed { index, entry ->
            val sweep = (entry.value / total) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
    }

    Column {
        data.entries.forEachIndexed { index, entry ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colors[index % colors.size])
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${entry.key}: Rp${entry.value}")
            }
        }
    }
}

@Composable
fun SimpleLineChart(data: Map<String, Int>) {
    val sortedData = data.toList().sortedBy { it.first }
    val maxVal = sortedData.maxOfOrNull { it.second }?.toFloat() ?: 1f
    val points = sortedData.mapIndexed { index, entry ->
        val x = index.toFloat() * 60f
        val y = 200f - (entry.second / maxVal * 200f)
        Offset(x, y)
    }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color.Blue,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f
            )
        }
        points.forEach {
            drawCircle(Color.Red, radius = 6f, center = it)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        sortedData.forEach {
            Text(it.first, fontSize = 10.sp)
        }
    }
}