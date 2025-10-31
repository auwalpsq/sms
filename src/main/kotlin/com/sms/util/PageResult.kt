package com.sms.util

data class PageResult<T>(
    val items: List<T>,
    val totalCount: Int
)
