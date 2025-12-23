package com.thellex.pay.network.services

import com.thellex.pay.core.utils.Constants
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.data.model.AppVersionResponseDto
import com.thellex.pay.data.model.ChainInfo
import retrofit2.http.GET
import retrofit2.http.Query

interface AppService {
    @GET(Constants.Endpoints.CHECK_APP_VERSION)
    suspend fun checkAppVersion(
        @Query("platform") platform: String = "android",
        @Query("currentVersion") currentVersion: String
    ): ApiResponse<AppVersionResponseDto>

    @GET(Constants.Endpoints.GET_APP_SETTINGS)
    suspend fun getAppSettings(): ApiResponse<List<ChainInfo>>
}

