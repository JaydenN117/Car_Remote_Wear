package com.example.carremote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

    @Composable
    fun LockUnlockButtons(lockOnClick: () -> Unit, unlockOnClick: () -> Unit) {
        //Lock icon on top unlock icon below. Fills screen, and centered
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center,modifier = Modifier.weight(1f).fillMaxSize().background(
                Color.Gray).clickable { lockOnClick()  } ) {
                //lock icon
                //lock painter
                val lockIcon = painterResource(id = R.drawable.ic_lock)
                Image(painter = lockIcon, contentDescription = "Lock", modifier = Modifier.padding(10.dp).fillMaxSize())
            }
            //line separator
            Divider(thickness = 4.dp, color = Color.Black)

            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxSize().background(
                Color.Gray).clickable { unlockOnClick()  }) {
                //unlock icon
                //unlock painter
                val unlockIcon = painterResource(id = R.drawable.ic_unlock)
                Image(painter = unlockIcon, contentDescription = "Unlock",modifier = Modifier.padding(10.dp).fillMaxSize())
            }
        }
    }
    @Composable
    fun Divider(color: Color, thickness: Dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness)
                .background(color)
        )
    }