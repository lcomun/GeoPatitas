package com.example.geopatitas.ui.screen.comun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geopatitas.utils.obtenerReportePorTipoAnimal
import com.example.geopatitas.utils.obtenerReportesPorMes
import com.example.geopatitas.utils.obtenerResumenAtencion
import com.example.geopatitas.utils.obtenerTodosLosReportes
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.bar.SimpleBarDrawer
import com.github.tehras.charts.bar.renderer.label.LabelDrawer
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer
import com.github.tehras.charts.bar.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.bar.renderer.xaxis.XAxisDrawer
import com.github.tehras.charts.bar.renderer.yaxis.SimpleYAxisDrawer
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.LineShader
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.line.SolidLineShader
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.yaxis.YAxisDrawer
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PieChartScreen(title: String, slices: List<Pair<String, PieChartData.Slice>>) {
    val total = slices.sumOf { it.second.value.toDouble() }.takeIf { it > 0 } ?: 1.0

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PieChart(
            pieChartData = PieChartData(slices.map { it.second }),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            animation = simpleChartAnimation(),
            sliceDrawer = SimpleSliceDrawer()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            slices.forEach { (label, slice) ->
                val porcentaje = (slice.value * 100 / total).roundToInt()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "$label: $porcentaje% (${slice.value.roundToInt()})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}




@Composable
fun BarChartScreen(title: String, bars: List<BarChartData.Bar>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            BarChart(
                barChartData = BarChartData(bars = bars),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.BottomCenter),
                animation = simpleChartAnimation(),
                barDrawer = SimpleBarDrawer(),
                xAxisDrawer = SimpleXAxisDrawer(),
                yAxisDrawer = SimpleYAxisDrawer(),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 75.dp),
            horizontalArrangement = Arrangement.spacedBy(70.dp)
        ) {
            bars.forEach { bar ->
                Text(
                    text = bar.value.toInt().toString(),
                    fontSize = 12.sp,
                    modifier = Modifier.offset(y = (-25).dp),
                    color = Color.Black,
                )
            }
        }
    }
}



@Composable
fun LineChartScreen(title: String, points: List<LineChartData.Point>, labels: List<String>) {
    val lineData = listOf(
        LineChartData(
            points = points,
            lineDrawer = SolidLineDrawer()
        )
    )

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LineChart(
            linesChartData = lineData,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            animation = simpleChartAnimation(),
            pointDrawer = FilledCircularPointDrawer(color = Color(0xFFD7B8FF)),
            horizontalOffset = 5f,
            labels = labels
        )
    }
}


@Composable
fun DashboardScreen() {
    val listState = rememberLazyListState()

    var pieSlices by remember { mutableStateOf<List<Pair<String, PieChartData.Slice>>>(emptyList()) }
    var barBars by remember { mutableStateOf<List<BarChartData.Bar>>(emptyList()) }
    var linePoints by remember { mutableStateOf<List<LineChartData.Point>>(emptyList()) }
    var lineLabels by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        // 1. Pie Chart: Atendidos vs Pendientes
        val (atendidosPair, pendientesPair) = obtenerResumenAtencion()
        val total = (atendidosPair.second + pendientesPair.second).takeIf { it > 0 } ?: 1
        pieSlices = listOf(
            atendidosPair.first to PieChartData.Slice(
                (atendidosPair.second * 100f) / total,
                Color(0xFF87CEFA)
            ),
            pendientesPair.first to PieChartData.Slice(
                (pendientesPair.second * 100f) / total,
                Color(0xFFFFA07A)
            )
        )

        // 2. Bar Chart: Reportes por tipo de animal
        val conteoPorTipo = obtenerReportePorTipoAnimal().map { (tipo, cantidad) ->
            BarChartData.Bar(
                label = if (tipo.length > 5) tipo.take(5) else tipo,
                value = cantidad.toFloat(),
                color = Color(0xFF87CEFA)
            )
        }
        barBars = conteoPorTipo

        // 3. Line Chart: Reportes por mes
        val reportesPorMes = obtenerReportesPorMes()

        val sortedMeses = reportesPorMes.keys.sortedBy { mes ->
            runCatching {
                SimpleDateFormat("MMMM yyyy", Locale("es")).parse(mes)
            }.getOrNull() ?: Date(0)
        }

        lineLabels = sortedMeses

        linePoints = sortedMeses.map { mes ->
            LineChartData.Point(
                value = reportesPorMes[mes]?.toFloat() ?: 0f,
                label = mes
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {

        item {
            PieChartScreen(
                title = "Porcentaje de reportes atendidos",
                slices = pieSlices
            )
        }

        item {
            BarChartScreen(
                title = "Número de reportes por tipo de animal",
                bars = barBars
            )
        }

        item {
            LineChartScreen(
                title = "Reportes por mes",
                points = linePoints,
                labels = lineLabels
            )
        }
    }
}
