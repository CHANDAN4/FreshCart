package com.app.freshcart.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentStatus
import com.app.freshcart.ui.theme.StatusError
import com.app.freshcart.ui.theme.StatusErrorContainer
import com.app.freshcart.ui.theme.StatusInfo
import com.app.freshcart.ui.theme.StatusInfoContainer
import com.app.freshcart.ui.theme.StatusSuccess
import com.app.freshcart.ui.theme.StatusSuccessContainer
import com.app.freshcart.ui.theme.StatusWarning
import com.app.freshcart.ui.theme.StatusWarningContainer

@Composable
fun OrderStatusBadge(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> StatusWarningContainer to StatusWarning
        OrderStatus.ACCEPTED -> StatusInfoContainer to StatusInfo
        OrderStatus.COMPLETED -> StatusSuccessContainer to StatusSuccess
        OrderStatus.REJECTED -> StatusErrorContainer to StatusError
        OrderStatus.CANCELLED -> Color(0xFFF0F0F0) to Color(0xFF757575)
    }

    StatusBadge(
        text = status.name,
        backgroundColor = bgColor,
        textColor = textColor,
        modifier = modifier
    )
}

@Composable
fun PaymentStatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        PaymentStatus.PENDING -> StatusWarningContainer to StatusWarning
        PaymentStatus.CONFIRMED -> StatusSuccessContainer to StatusSuccess
        PaymentStatus.FAILED -> StatusErrorContainer to StatusError
    }

    StatusBadge(
        text = status.name,
        backgroundColor = bgColor,
        textColor = textColor,
        modifier = modifier
    )
}

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}
