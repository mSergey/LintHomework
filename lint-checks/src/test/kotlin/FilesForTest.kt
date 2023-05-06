package ru.otus.homework.lintchecks


import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin

val jobInBuilderTestFile: TestFile = kotlin(
    """
    package ru.otus.homework.lintchecks

        import androidx.lifecycle.ViewModel
        import androidx.lifecycle.viewModelScope
        import kotlinx.coroutines.Dispatchers
        import kotlinx.coroutines.Job
        import kotlinx.coroutines.NonCancellable
        import kotlinx.coroutines.SupervisorJob
        import kotlinx.coroutines.delay
        import kotlinx.coroutines.launch

        class JobInBuilderTestCase(
            private val job: Job
        ) : ViewModel() {
            
            fun case1() {
                viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
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
    """.trimIndent()
)