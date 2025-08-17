package com.deadlyord.authease.di

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAccountDatabase(@ApplicationContext)
}