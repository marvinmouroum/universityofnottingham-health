package de.mouroum.uno_health_app

import retrofit2.http.GET
import retrofit2.http.Path

interface SurveyApi {
    @GET("survey/{id}")
    suspend fun survey(
        @Path("id") id: String
    ): Survey

    @GET("check")
    suspend fun check()
}