package com.yrezgui.accessmedialocation

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.yrezgui.accessmedialocation.ui.theme.MediaLocationTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaLocationTheme {
                var logs by remember { mutableStateOf(emptyList<String>()) }
                var toBeRequested by remember { mutableStateOf(emptySet<Permission>()) }
                var permissionState by remember { mutableStateOf(emptyMap<Permission, Boolean>()) }
                val requestPermissions =
                    rememberLauncherForActivityResult(RequestMultiplePermissions()) {
                        // We ignore the callback logic as the LifecycleResumeEffect already handles
                        // checking the permissions
                    }
                val dateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)

                LifecycleResumeEffect(null) {
                    logs = logs + "ON_RESUME: ${LocalTime.now().format(dateTimeFormatter)}"
                    permissionState = checkStoragePermissions()

                    onPauseOrDispose {
                        logs =
                            logs + "ON_PAUSE/DISPOSE: ${LocalTime.now().format(dateTimeFormatter)}"
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.twotone_lock_24),
                                    contentDescription = null
                                )
                            },
                            title = {
                                Text(text = "Storage Permissions")
                            },
                            actions = {
                                Button(onClick = {
                                    requestPermissions.launch(
                                        toBeRequested
                                            .map { "android.permission.${it.name}" }
                                            .toTypedArray()
                                    )
                                }) {
                                    Text("Request")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 8.dp)
                    ) {
                        Permission.entries.forEach { permission ->
                            val isGranted = if (permissionState[permission] != null) {
                                permissionState[permission] == true
                            } else {
                                null
                            }

                            ListItem(
                                headlineContent = {
                                    Text(
                                        permission.name.lowercase(),
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                supportingContent = {
                                    if (isGranted != null) {
                                        Text(if (isGranted) "Granted" else "Denied")
                                    }
                                },
                                trailingContent = {
                                    Switch(
                                        modifier = Modifier.semantics {
                                            contentDescription = permission.name
                                        },
                                        checked = toBeRequested.contains(permission),
                                        onCheckedChange = {
                                            toBeRequested = toBeRequested.toggle(permission)
                                        }
                                    )
                                }
                            )
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        LazyColumn {
                            items(logs) { log ->
                                Text(log)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkStoragePermissions(): Map<Permission, Boolean> {
        return Permission.entries.associateWith { permission ->
            checkSelfPermission("android.permission.${permission.name}") == PackageManager.PERMISSION_GRANTED
        }
    }
}

enum class Permission {
    READ_EXTERNAL_STORAGE,
    READ_MEDIA_IMAGES,
    READ_MEDIA_VIDEOS,
    READ_MEDIA_VISUAL_USER_SELECTED,
    ACCESS_MEDIA_LOCATION
}

private fun <T> Set<T>.toggle(element: T): Set<T> {
    return if (this.contains(element)) {
        this - element
    } else {
        this + element
    }
}