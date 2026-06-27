package com.itek.rftaar.data.model

import androidx.room.Embedded
import com.itek.rftaar.data.entity.MenuNotificationEntity

data class MenuNotificationWithCount(
    @Embedded val notification: MenuNotificationEntity,
    val unreadCount: Int // Room will map the COUNT() result here
)