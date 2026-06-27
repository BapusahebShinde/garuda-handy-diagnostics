package com.itek.rftaar.core.common.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.itek.rftaar.R
import kotlinx.coroutines.delay

object DialogUtils {

  @Composable
  fun ShowCustomAlertDialog(
    openDialog: MutableState<Boolean>,
    title: String? = null,
    msg: String? = null,
    customView: @Composable (() -> Unit)? = null,
    isSuccess: Boolean?=null,
    isAutoDismiss: Boolean = false,
    posKey: String? = null,
    posClick: (() -> Unit)? = null,
    negKey: String? = null,
    negClick: (() -> Unit)? = null,
    neuKey: String? = null,
    neuClick: (() -> Unit)? = null,
    isSelectionDialog: Boolean = false
  )
  {
    if (openDialog.value) {

      // Auto dismiss logic
      if (isAutoDismiss && !posKey.isNullOrEmpty() && negKey.isNullOrEmpty() && neuKey.isNullOrEmpty() && posKey.equals("OK", true)) {
        LaunchedEffect(Unit) {
          delay(5500)
          posClick?.invoke()
          openDialog.value = false
        }
      }

      Dialog(
        onDismissRequest = { openDialog.value = false },
        properties = DialogProperties(
          dismissOnBackPress = false,
          dismissOnClickOutside = false,
          usePlatformDefaultWidth = false
        )
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {

            // Success/Error Icon if title is null
            if (isSuccess != null && title.isNullOrEmpty()) {
              Image(
                painter = painterResource(
                  id = if (isSuccess) R.drawable.property_1_success else R.drawable.property_1_error
                ),
                contentDescription = "Dialog Icon",
                modifier = Modifier
                  .size(64.dp)
                  .padding(bottom = 16.dp),
                alignment = Alignment.Center
              )
            }

            // Title
            title?.let {
              Text(
                text = it,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 4.dp)
              )
            }

            // Message
            msg?.let {
              Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Center
              )
            }

            // Custom Composable view
            customView?.invoke()

            Spacer(modifier = Modifier.height(12.dp))

            // Buttons Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              negKey?.let {
                Button(
                  onClick = {
                    negClick?.invoke()
                    openDialog.value = false
                  },
                  modifier = Modifier.height(38.dp),
                  shape = RoundedCornerShape(6.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelectionDialog) Color.Gray else Color.LightGray,
                    contentColor = Color.White
                  )
                ) {
                  Text(text = it, fontSize = 14.sp)
                }
              }

              posKey?.let {
                Button(
                  onClick = {
                    posClick?.invoke()
                    openDialog.value = false
                  },
                  modifier = Modifier.height(38.dp),
                  shape = RoundedCornerShape(6.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelectionDialog) Color.Red else Color(0xFFFF5252),
                    contentColor = Color.White
                  )
                ) {
                  Text(text = it, fontSize = 14.sp)
                }
              }

              neuKey?.let {
                Button(
                  onClick = {
                    neuClick?.invoke()
                    openDialog.value = false
                  },
                  modifier = Modifier.height(38.dp),
                  shape = RoundedCornerShape(6.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelectionDialog) Color.Gray else Color.LightGray,
                    contentColor = Color.White
                  )
                ) {
                  Text(text = it, fontSize = 14.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}