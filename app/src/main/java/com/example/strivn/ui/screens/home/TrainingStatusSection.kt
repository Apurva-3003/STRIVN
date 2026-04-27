package com.example.strivn.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.strivn.data.models.UserMetrics
import com.example.strivn.ui.components.MetricWheel
import com.example.strivn.ui.components.MetricWheelSize
import com.example.strivn.ui.components.metricWheelPercentages

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainingStatusSection(
    metrics: UserMetrics,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
) {
    val p = metricWheelPercentages(metrics)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MetricWheel(
            label = "Daily Training Capacity",
            percentage = p.dailyTrainingCapacity,
            size = MetricWheelSize.Large,
            capacityForColor = metrics.dailyCapacity,
        )

        Spacer(Modifier.height(22.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
            maxItemsInEachRow = 3,
        ) {
            MetricWheel(
                label = "Fatigue",
                percentage = p.fatigue,
                size = MetricWheelSize.Medium,
                invertedScale = true,
            )
            MetricWheel(
                label = "Fitness",
                percentage = p.fitness,
                size = MetricWheelSize.Medium,
            )
            MetricWheel(
                label = "Sleep",
                percentage = p.sleep,
                size = MetricWheelSize.Medium,
            )
        }
    }
}
