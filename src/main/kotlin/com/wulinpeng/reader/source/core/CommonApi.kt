package com.wulinpeng.reader.source.core

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Url

interface CommonApi {

    @GET
    suspend fun getUrlContent(@Url url: String): String

    @POST
    suspend fun postUrlContent(@Url url: String,  @Body body: Any): String
}