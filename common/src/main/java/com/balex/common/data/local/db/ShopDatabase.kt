package com.balex.common.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.balex.common.data.local.model.ShopItemDBModel

@Database(entities = [ShopItemDBModel::class], version = 1, exportSchema = false)
abstract class ShopDatabase : RoomDatabase() {

    abstract fun shopItemsDao(): ShopItemsDao

    companion object {

        private const val DB_NAME = "ShopItemsDatabase"
        private var INSTANCE: ShopDatabase? = null
        private val LOCK = Any()

        fun getInstance(context: Context): ShopDatabase {
            INSTANCE?.let { return it }

            synchronized(LOCK) {
                INSTANCE?.let { return it }

                val database = Room.databaseBuilder(
                    context = context,
                    klass = ShopDatabase::class.java,
                    name = DB_NAME
                ).build()

                INSTANCE = database
                return database
            }
        }
    }
}
