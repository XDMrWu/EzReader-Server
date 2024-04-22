package com.wulinpeng.ezreader.route.model

import kotlinx.serialization.Serializable

@Serializable
data class EzResponse<T>(val code: Int, val msg: String, val data: T? = null)
@Serializable
class EmptyData()