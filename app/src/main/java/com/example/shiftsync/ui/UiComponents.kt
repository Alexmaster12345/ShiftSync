package com.example.shiftsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftsync.ui.theme.*

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) { Column(Modifier.padding(14.dp), content = content) }
}

@Composable
fun SectionTitle(text: String, trailing: String? = null, onTrailingClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        if (trailing != null) {
            Text(
                trailing,
                color = ShiftBlue,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(enabled = onTrailingClick != null) { onTrailingClick?.invoke() }
            )
        }
    }
}

@Composable
fun HeaderWithBack(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .size(36.dp)
                .clip(CircleShape)
                .background(CardBackground)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppText)
        }
        Text(title, modifier = Modifier.align(Alignment.Center), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppText)
    }
}

@Composable
fun SimpleRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(enabled = onClick != null) { onClick?.invoke() }.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = AppText, fontWeight = FontWeight.SemiBold)
            subtitle?.let { Text(it, color = TextSecondary, fontSize = 12.sp) }
        }
        trailing?.invoke()
    }
}

@Composable
fun Stepper(value: String, onMinus: () -> Unit, onPlus: () -> Unit, tint: Color = ShiftBlue) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    SmallFloatingActionButton(onClick = onMinus, containerColor = tint, contentColor = Color.White, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null) }
        Text(value, color = AppText, fontWeight = FontWeight.SemiBold)
    SmallFloatingActionButton(onClick = onPlus, containerColor = tint, contentColor = Color.White, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null) }
    }
}

@Composable
fun BottomNavBar(selected: NavItem, onNavigate: (NavItem) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(30.dp), color = CardBackground, shadowElevation = 10.dp) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(NavItem.Home, NavItem.Calendar).forEach { item -> NavButton(item, selected == item, onNavigate) }
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(ShiftBlue).clickable { onNavigate(NavItem.Add) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Add, null, tint = Color.White) }
                }
                listOf(NavItem.Workplace, NavItem.Profile).forEach { item -> NavButton(item, selected == item, onNavigate) }
            }
        }
    }
}

@Composable
private fun NavButton(item: NavItem, active: Boolean, onNavigate: (NavItem) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp).clickable { onNavigate(item) }) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(if (active) ShiftBlue.copy(.14f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) { Icon(item.icon, null, tint = if (active) ShiftBlue else TextSecondary, modifier = Modifier.size(22.dp)) }
    }
}

enum class NavItem(val icon: ImageVector) { Home(Icons.Default.Home), Calendar(Icons.Default.CalendarMonth), Add(Icons.Default.Add), Workplace(Icons.Default.LocationOn), Profile(Icons.Default.Person) }

@Composable
fun SegmentedOption(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, selectedColor: Color = ShiftBlue) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        color = if (selected) selectedColor else CardBackgroundAlt,
        shape = RoundedCornerShape(16.dp)
    ) {
    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        icon?.let { Icon(it, null, tint = if (selected) Color.White else AppText, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)) }
        Text(text, color = if (selected) Color.White else AppText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
fun AppScaffold(bottomNav: NavItem? = null, onNavigate: ((NavItem) -> Unit)? = null, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            if (bottomNav != null && onNavigate != null) BottomNavBar(selected = bottomNav, onNavigate = onNavigate)
        }
    ) { content(it) }
}

@Composable
fun SaveChangesButton(saved: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (saved) GreenAccent else ShiftBlue)
    ) {
        Text(if (saved) "Saved!" else "Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
    }
}
