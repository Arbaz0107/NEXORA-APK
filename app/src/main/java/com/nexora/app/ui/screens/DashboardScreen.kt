package com.nexora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexora.app.MainViewModel
import com.nexora.app.data.CategoryEntity
import com.nexora.app.data.LinkEntity
import com.nexora.app.ui.components.AppMark
import com.nexora.app.ui.components.CategoryIcons
import com.nexora.app.ui.theme.Neon
import com.nexora.app.ui.theme.Radius
import com.nexora.app.util.Platform
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * The Home tab. Editorial layout: a big headline, one search-or-paste bar, quick-start chips,
 * the user's collections (categories) as colourful cards, and a timeline of what was saved today.
 * The full list, filters and bulk actions live on the Links tab.
 */
@Composable
fun DashboardScreen(vm: MainViewModel, navigate: (String) -> Unit, onSearch: () -> Unit) {
    val context = LocalContext.current
    val all by vm.allLinks.collectAsState()
    val categories by vm.categories.collectAsState()
    val palette = listOf(Neon.lime, Neon.coral, Neon.teal, Neon.green)

    val todayStart = remember { startOfToday() }
    val sorted = remember(all) { all.sortedByDescending { it.createdAt } }
    val today = remember(sorted, todayStart) { sorted.filter { it.createdAt >= todayStart } }
    val showToday = today.isNotEmpty()
    val timeline = (if (showToday) today else sorted).take(4)
    val counts = remember(all) { all.groupingBy { it.categoryId }.eachCount() }
    val shownCategories = categories.take(3)

    fun pasteAndSave() {
        val copied = readClipboardForAdd(context)
        navigate(if (copied != null) "add?url=" + android.net.Uri.encode(copied) else "add")
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item(key = "d-brand") {
            Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Nexora", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                AppMark()
            }
        }

        item(key = "d-headline") {
            Text(
                "Save it once.\nFind it forever.",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp)
            )
        }

        item(key = "d-bar") {
            Row(
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 18.dp)
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(Radius.pill)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onSearch)
                    .padding(start = 20.dp, end = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Search or paste a link", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Neon.lime)
                        .clickable { pasteAndSave() }
                        .semantics { contentDescription = "Paste and save a link" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(20.dp), tint = Neon.ink)
                }
            }
        }

        item(key = "d-chips") {
            Row(
                Modifier.padding(horizontal = 20.dp).padding(top = 14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickChip("Video", Icons.Outlined.Movie, Modifier.weight(1f)) { navigate("downloads") }
                QuickChip("PDF", Icons.Outlined.PictureAsPdf, Modifier.weight(1f)) { navigate("pdftools") }
                QuickChip("Image", Icons.Outlined.Image, Modifier.weight(1f)) { navigate("images") }
                QuickChip("Audio", Icons.Outlined.MusicNote, Modifier.weight(1f)) { navigate("downloads") }
            }
        }

        item(key = "d-h-collections") {
            Row(Modifier.padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Collections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { navigate("categories") }) { Text("See all") }
            }
        }
        item(key = "d-collections") {
            if (shownCategories.isEmpty()) {
                Row(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { navigate("manage_categories") }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Create your first collection", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Group your links into categories like Study or Shopping.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    shownCategories.forEachIndexed { i, c ->
                        CollectionCard(
                            c, counts[c.id] ?: 0, palette[i % palette.size],
                            Modifier.weight(1f).padding(top = if (i == 1) 18.dp else 0.dp)
                        ) { navigate("category/${c.id}") }
                    }
                    repeat(3 - shownCategories.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        if (all.isEmpty()) {
            item(key = "d-empty") {
                Text(
                    "Paste a link above to save your first one. Links you save today will show up here.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
                )
            }
        } else {
            item(key = "d-h-timeline") {
                Text(
                    if (showToday) "Today" else "Recently added",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 12.dp)
                )
            }
            items(timeline.size, key = { "dt${timeline[it].id}" }) { i ->
                val link = timeline[i]
                TimelineRow(link, timeLabel(link.createdAt, showToday)) { navigate("detail/${link.id}") }
            }
        }
    }
}

@Composable
private fun QuickChip(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .height(44.dp)
            .clip(Radius.pill)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, Radius.pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CollectionCard(category: CategoryEntity, count: Int, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .height(176.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Neon.ink), contentAlignment = Alignment.Center) {
            Icon(CategoryIcons.get(category.iconKey), null, Modifier.size(20.dp), tint = Color.White)
        }
        Spacer(Modifier.weight(1f))
        Text(category.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Neon.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(if (count == 1) "1 link" else "$count links", style = MaterialTheme.typography.labelMedium, color = Neon.ink.copy(alpha = 0.75f))
    }
}

@Composable
private fun TimelineRow(link: LinkEntity, time: String, onClick: () -> Unit) {
    Row(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, softWrap = false, modifier = Modifier.width(46.dp).padding(top = 15.dp)
        )
        Row(
            Modifier
                .weight(1f)
                .clip(Radius.card)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(link.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(link.domain, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(Platform.fromKey(link.platform).label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
            }
        }
    }
}

private fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun timeLabel(ms: Long, today: Boolean): String =
    SimpleDateFormat(if (today) "HH:mm" else "d MMM", Locale.getDefault()).format(Date(ms))
