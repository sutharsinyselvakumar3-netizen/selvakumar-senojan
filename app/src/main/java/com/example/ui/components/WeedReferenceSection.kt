package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.WeedReferenceSlot
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe

@Composable
fun WeedReferenceSection(
    slots: List<WeedReferenceSlot>,
    onUploadUri: (Int, Uri) -> Unit,
    onUploadBitmap: (Int, Bitmap) -> Unit,
    onDelete: (Int) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = "WEED REFERENCE IMAGES (EXACTLY 3 SLOTS)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = accentColor
                )
                Text(
                    text = "Original natural colors preserved. Used by AI for multi-reference matching.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Exactly 3 slots: WEED 1, WEED 2, WEED 3
            slots.take(3).forEach { slot ->
                WeedSlotCard(
                    slot = slot,
                    accentColor = accentColor,
                    onUploadUri = { uri -> onUploadUri(slot.slotNumber, uri) },
                    onUploadBitmap = { bmp -> onUploadBitmap(slot.slotNumber, bmp) },
                    onDelete = { onDelete(slot.slotNumber) }
                )
            }
        }
    }
}

@Composable
private fun WeedSlotCard(
    slot: WeedReferenceSlot,
    accentColor: Color,
    onUploadUri: (Uri) -> Unit,
    onUploadBitmap: (Bitmap) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onUploadUri(uri)
        }
    }

    // Camera Capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onUploadBitmap(bitmap)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FBF9), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E7E4), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image Preview (BoxFit.contain / ContentScale.Fit - ORIGINAL COLORS PRESERVED)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEEEEEE))
                .testTag("weed_slot_${slot.slotNumber}_preview"),
            contentAlignment = Alignment.Center
        ) {
            if (slot.isValid && slot.file != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(slot.file)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Weed ${slot.slotNumber} Reference",
                    contentScale = ContentScale.Fit, // Preserve original natural colors & aspect ratio
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Empty",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("SLOT ${slot.slotNumber}", fontSize = 9.sp, color = Color.Gray)
                }
            }
        }

        // Slot Info & Actions
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "WEED ${slot.slotNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = accentColor
                )
                if (slot.isValid) {
                    Text(
                        text = "• ${slot.width}x${slot.height}px",
                        fontSize = 10.sp,
                        color = StatusSafe,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (slot.errorMessage != null) {
                    Text(
                        text = slot.errorMessage,
                        fontSize = 10.sp,
                        color = StatusCritical,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Buttons: Replace (Gallery / Camera) & Delete
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.height(28.dp).testTag("weed_${slot.slotNumber}_gallery")
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.size(2.dp))
                    Text("GALLERY", fontSize = 9.sp)
                }

                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.height(28.dp).testTag("weed_${slot.slotNumber}_camera")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.size(2.dp))
                    Text("CAMERA", fontSize = 9.sp)
                }

                if (slot.isValid) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCritical),
                        modifier = Modifier.height(28.dp).testTag("weed_${slot.slotNumber}_delete")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                        Text("DEL", fontSize = 9.sp)
                    }
                }
            }
        }
    }
}
