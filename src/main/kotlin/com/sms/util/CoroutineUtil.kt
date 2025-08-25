package com.sms.util

import com.vaadin.flow.component.UI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Launch coroutine in background scope
fun launchUiCoroutine(block: suspend CoroutineScope.() -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        block()
    }
}

// Suspend coroutine, then run block on Vaadin UI thread safely
suspend fun <T> UI?.withUi(block: () -> T): T? = suspendCancellableCoroutine { cont ->
    if (this == null || !this.isAttached) {
        cont.cancel()
        return@suspendCancellableCoroutine
    }
    this.access {
        try {
            val result = block()
            cont.resume(result)
        } catch (e: Exception) {
            cont.cancel(e)
        }
    }
}