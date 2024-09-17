package com.ami.bakhoobiservice.roomDB

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface InsertAppInfoDao {
    @Insert
    suspend fun insertAppInfo(appInfo: AppInformationEntity)

}

