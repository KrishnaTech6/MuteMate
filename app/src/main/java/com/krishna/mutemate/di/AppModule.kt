package com.krishna.mutemate.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.krishna.mutemate.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app.applicationContext,
            AppDatabase::class.java,
            "mute_schedule_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMuteScheduleDao(db: AppDatabase) = db.muteScheduleDao()

    @Provides
    @Singleton
    fun provideLocationMuteDao(db: AppDatabase) = db.locationMuteDao()

    @Provides
    @Singleton
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager{
        return WorkManager.getInstance(context)
    }

}