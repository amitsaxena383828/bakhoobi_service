package com.ami.bakhoobiservice.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppInformationEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Long,
    val name:String
)
