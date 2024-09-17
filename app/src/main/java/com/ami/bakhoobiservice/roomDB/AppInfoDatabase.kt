package com.ami.bakhoobiservice.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AppInformationEntity::class], version = 1)

abstract class AppInfoDatabase : RoomDatabase() {
    abstract fun getContactDao(): InsertAppInfoDao

    companion object {
        @Volatile //@Volatile is used so the every thread get notified if there is any changes
        //found in the instance variable.
        var instance: AppInfoDatabase? = null

        fun getDB(context: Context): AppInfoDatabase {

            if (instance == null) {
                synchronized(this) { //Synchronized block is used so that no two thread
                    // can try to create the instance of the database
                    instance = Room.databaseBuilder(
                        context.applicationContext, AppInfoDatabase::class.java,
                        "AppInfoDB"
                    ).build()
                }
            }
            return instance!!
        }
    }
}