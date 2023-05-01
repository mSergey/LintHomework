package ru.otus.homework.linthomework.jobinbuilderusage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JobInBuilderTestCase(
    private val job: Job
) : ViewModel() {
    val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        // do something
    }
    val scope = CoroutineScope(Dispatchers.IO)
    fun case1() {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + exceptionHandler) {
            delay(1000)
            println("Hello World")
        }
    }

    fun case2() {
        viewModelScope.launch(Job()) {
            delay(1000)
            println("Hello World")
        }
    }

    fun case3() {
        viewModelScope.launch(job) {
            delay(1000)
            println("Hello World")
        }
    }

    fun case4() {
        viewModelScope.launch(NonCancellable) {
            launch {  }
            delay(1000)
            println("Hello World")
        }
    }
}