package com.itek.rftaar.data.remote

import android.content.Context
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.repositoryImpl.ApiRepositoryImpl
import com.itek.rftaar.domain.repository.ApiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpClientProvider {

    const val TIME_OUT: Long = 40

    @Provides
    @Singleton
    fun provideOkHttpClient(isToken:Boolean): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    return OkHttpClient.Builder().addInterceptor { chain ->
      val accessToken = DataStoreManager.readFromPreferences(ParameterConstants.ACCESS_TOKEN, "")
      val refreshToken = DataStoreManager.readFromPreferences(ParameterConstants.REFRESH_TOKEN, "")
      val original = chain.request()
      val requestBuilder = original.newBuilder();//.header("Content-Type", "application/json")
      if (!isToken && accessToken.isNotEmpty()) requestBuilder.header("Authorization", accessToken)
      else if (isToken && refreshToken.isNotEmpty()) requestBuilder.header("Authorization", refreshToken)
      chain.proceed(requestBuilder.build())
    }
      .addInterceptor(logging)
      .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
      .readTimeout(TIME_OUT, TimeUnit.SECONDS)
      .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
      .build()
  }

    @Provides
    @Singleton
    fun provideApiRepository(
        @ApplicationContext context: Context
    ): ApiRepository {
        return ApiRepositoryImpl(context)
    }


}