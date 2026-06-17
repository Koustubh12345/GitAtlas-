package com.tensei.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.MaterialTheme
import com.tensei.MainRoute

import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid

@Composable
fun AnimatedBottomBar(
    currentRoute: MainRoute,
    onNavigate: (MainRoute) -> Unit,
    liquidState: LiquidState,
    modifier: Modifier = Modifier
) {
    val routes = MainRoute.entries.toTypedArray()
    val selectedIndex = routes.indexOf(currentRoute)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val glassTint = if (isDark) Color(0x33000000) else Color(0x40FFFFFF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(64.dp)
            .liquid(liquidState) {
                frost = 45.dp
                shape = RoundedCornerShape(32.dp)
                tint = glassTint
                refraction = 0.25f
                curve = 0.25f
                edge = 0.1f
            }
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            val itemWidth = maxWidth / routes.size
            val density = LocalDensity.current
            
            val pillWidth = 64.dp
            val pillHeight = 40.dp
            
            var dragX by remember { mutableStateOf<Float?>(null) }
            
            // The fluid slide animation
            val targetOffset by animateDpAsState(
                targetValue = if (dragX != null) {
                    with(density) { dragX!!.toDp() } - (pillWidth / 2)
                } else {
                    (itemWidth * selectedIndex) + (itemWidth / 2) - (pillWidth / 2)
                },
                animationSpec = spring(
                    dampingRatio = 0.6f, // Bouncy bounce
                    stiffness = 300f // Liquid slide
                ),
                label = "indicatorOffset"
            )

            // The animated pill background (The "Highlight")
            Box(
                modifier = Modifier
                    .offset(x = targetOffset)
                    .width(pillWidth)
                    .fillMaxHeight()
                    .wrapContentSize(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = pillWidth, height = pillHeight)
                        .clip(RoundedCornerShape(50)) // Pill shape
                        .background(MaterialTheme.colorScheme.primary) 
                )
            }

            // The Icons and Text
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(routes) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val itemW = size.width.toFloat() / routes.size
                            
                            var dragPosition = down.position.x
                            dragX = dragPosition
                            
                            var currentIndex = (dragPosition / itemW).toInt().coerceIn(0, routes.lastIndex)
                            
                            try {
                                drag(down.id) { change ->
                                    change.consume()
                                    dragPosition = change.position.x.coerceIn(0f, size.width.toFloat())
                                    dragX = dragPosition
                                    currentIndex = (dragPosition / itemW).toInt().coerceIn(0, routes.lastIndex)
                                }
                            } finally {
                                // Navigation triggers on drop/release, triggering the spring bounce
                                dragX = null
                                onNavigate(routes[currentIndex])
                            }
                        }
                    },
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                routes.forEach { route ->
                    val isSelected = currentRoute == route
                    
                    Column(
                        modifier = Modifier
                            .weight(1f) // Ensures perfect geometric alignment 
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = route.icon,
                            contentDescription = route.title,
                            tint = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
