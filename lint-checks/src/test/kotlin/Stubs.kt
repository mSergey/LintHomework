package ru.otus.homework.lintchecks

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin

val androidxLifecyclePackageStub: TestFile = kotlin(
    """
    package androidx.lifecycle
    
    import kotlinx.coroutines.StubCoroutineScope

    public open class ViewModel
    
    public val ViewModel.viewModelScope: StubCoroutineScope
        get() = StubCoroutineScope()
    """.trimIndent()
)


val kotlinxCoroutinesPackageStub: TestFile = kotlin(
    """
    package kotlinx.coroutines


    public open class StubCoroutineScope

    public fun delay(time: Int) {
        // STUB!!!
    }

    public open class StubCoroutineContext {
        public operator fun plus(): StubCoroutineContext {
            return StubCoroutineContext()
            // STUB!!!
        }
    }
   



    interface Job
    interface SupervisorJob

    public interface JobStub : StubCoroutineContext, Job

    public interface SupervisorJobStub : StubCoroutineContext, SupervisorJob

    public fun StubCoroutineScope.launch(
            context: StubCoroutineContext = StubCoroutineContext(),
            block: StubCoroutineScope.() -> Unit
        ) {
            // STUB!!!
        }
    
    public fun Job(): JobStub {
        return JobStub()
    }

    public fun SupervisorJob(): SupervisorJob {
        return SupervisorJobStub()
    }

    public object Dispatchers {
        val IO: StubCoroutineContext = StubCoroutineContext()
    }
    
    public object NonCancellable : StubCoroutineContext
""".trimIndent())