package com.example.ui.components

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.AppTheme

private val AVATAR_COLORS = listOf(
    "#3B82F6", // Blue
    "#8B5CF6", // Purple
    "#EC4899", // Pink
    "#10B981", // Emerald
    "#F59E0B", // Amber
    "#EF4444", // Red
    "#6366F1", // Indigo
    "#14B8A6"  // Teal
)

@Composable
fun EditProfileDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onSaveProfile: (name: String, photoUri: String?, dateOfBirth: String?, bio: String?, schoolOrGrade: String?, avatarColorHex: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    var name by remember { mutableStateOf(currentUser.name) }
    var photoUri by remember { mutableStateOf(currentUser.photoUri) }
    var dateOfBirth by remember { mutableStateOf(currentUser.dateOfBirth.orEmpty()) }
    var bio by remember { mutableStateOf(currentUser.bio.orEmpty()) }
    var schoolOrGrade by remember { mutableStateOf(currentUser.schoolOrGrade.orEmpty()) }
    var selectedColorHex by remember { mutableStateOf(currentUser.avatarColorHex) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistentPath = saveImageToInternalStorage(context, uri, currentUser.id)
            photoUri = persistentPath ?: uri.toString()
        }
    }

    val loadedBitmap = remember(photoUri) {
        photoUri?.let { loadBitmapFromPathOrUri(context, it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = colors.cardBackground,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Edit Profile 👤",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Photo & Color Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(90.dp)
                    ) {
                        // Avatar image or color circle
                        if (loadedBitmap != null) {
                            Image(
                                bitmap = loadedBitmap,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, parseHexColor(selectedColorHex), CircleShape)
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(selectedColorHex))
                            ) {
                                Text(
                                    text = name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                            }
                        }

                        // Upload Camera Button Overlay
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                                .clickable { photoPickerLauncher.launch("image/*") }
                                .testTag("btn_upload_profile_photo")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Upload Photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Tap camera icon to change profile photo",
                    fontSize = 11.sp,
                    color = colors.textTertiary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Theme Color Picker
                Text(
                    text = "Avatar Theme Color",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AVATAR_COLORS) { colorHex ->
                        val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(colorHex))
                                .clickable { selectedColorHex = colorHex }
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) colors.textPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_profile_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email Address (Read only)
                OutlinedTextField(
                    value = currentUser.email,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Email Address (Registered)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = colors.textTertiary
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date of Birth
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    placeholder = { Text("e.g. 2002-08-15") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_profile_dob")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // School / Grade / Institution
                OutlinedTextField(
                    value = schoolOrGrade,
                    onValueChange = { schoolOrGrade = it },
                    label = { Text("Institution / Grade / Major") },
                    placeholder = { Text("e.g. Dhaka University / BCS Prep") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_profile_school")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bio / Goal
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio & Target Goal") },
                    placeholder = { Text("e.g. Preparing for Cadre Exam & Top Marks 🎓") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_profile_bio")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveProfile(
                        name,
                        photoUri,
                        dateOfBirth,
                        bio,
                        schoolOrGrade,
                        selectedColorHex
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier.testTag("btn_save_profile")
            ) {
                Text("Save Profile ✨", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_cancel_profile")
            ) {
                Text("Cancel", color = colors.textSecondary)
            }
        },
        modifier = modifier.testTag("edit_profile_dialog")
    )
}

private fun parseHexColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        Color(0xFF3B82F6)
    }
}

fun saveImageToInternalStorage(context: Context, sourceUri: Uri, userId: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val profileDir = java.io.File(context.filesDir, "profile_pictures")
        if (!profileDir.exists()) {
            profileDir.mkdirs()
        }
        val destinationFile = java.io.File(profileDir, "avatar_${userId}.jpg")
        destinationFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        destinationFile.absolutePath
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

fun loadBitmapFromPathOrUri(context: Context, pathOrUriStr: String): ImageBitmap? {
    return try {
        val file = java.io.File(pathOrUriStr)
        if (file.exists() && file.isFile) {
            android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } else {
            val uri = Uri.parse(pathOrUriStr)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source).asImageBitmap()
            } else {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                bitmap.asImageBitmap()
            }
        }
    } catch (_: Throwable) {
        null
    }
}
