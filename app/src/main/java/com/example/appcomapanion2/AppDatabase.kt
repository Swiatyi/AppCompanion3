package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  AppDatabase.kt                              ║
// ║  O banco Room: reúne as @Entity, expõe os    ║
// ║  DAOs e é um SINGLETON (uma única instância  ║
// ║  no app inteiro: companion + @Volatile +     ║
// ║  synchronized, igual ao exemplo de aula)     ║
// ╚══════════════════════════════════════════════╝

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(entities = [Heroi::class, Chefe::class, PartyMembro::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun heroiDAO(): HeroiDAO
    abstract fun chefeDAO(): ChefeDAO
    abstract fun partyDAO(): PartyDAO

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            val temInstance = INSTANCE

            if (temInstance != null) {
                return temInstance
            } else {

                synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "companion_database"
                    ).build()
                    INSTANCE = instance
                    return instance
                }

            }
        }
    }
}
