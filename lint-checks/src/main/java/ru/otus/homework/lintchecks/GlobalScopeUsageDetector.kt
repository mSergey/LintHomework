package ru.otus.homework.lintchecks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.getContainingUClass
import org.jline.utils.Log

private const val ID = "GlobalScopeUsage"
private const val BRIEF_DESCRIPTION = "GlobalScope should not use"
private const val EXPLANATION = "GlobalScope usage can lead to excessive resource usage and memory leaks." +
        "That is no recommended to use."
private const val PRIORITY = 1
private const val CATEGORY_PRIORITY = 2
private const val CATEGORY_NAME = "Coroutine scope usage"
private const val VIEW_MODEL_CLASS_QUALIFIED_NAME = "androidx.lifecycle.ViewModel"
private const val FRAGMENT_CLASS_QUALIFIED_NAME = "androidx.fragment.app.Fragment"
private const val LIFECYCLE_VIEW_MODEL_DEPENDENCY = "androidx.lifecycle:lifecycle-viewmodel-ktx"
private const val LIFECYCLE_RUNTIME_DEPENDENCY = "androidx.lifecycle:lifecycle-runtime-ktx"
private const val GLOBAL_SCOPE_CLASS_SIMPLE_NAME = "GlobalScope"
private const val VIEW_MODEL_SCOPE_REFERENCE = "viewModelScope"
private const val LIFECYCLE_SCOPE_REFERENCE = "lifecycleScope"

class GlobalScopeUsageDetector : Detector(), Detector.UastScanner {

    companion object {
        val ISSUE = Issue.create(
            id = ID,
            briefDescription = BRIEF_DESCRIPTION,
            explanation = EXPLANATION,
            category = Category.create(CATEGORY_NAME, CATEGORY_PRIORITY),
            priority = PRIORITY,
            severity = Severity.WARNING,
            implementation = Implementation(
                GlobalScopeUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return GlobalScopeUsageHandler(context = context)
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(USimpleNameReferenceExpression::class.java)
    }

    class GlobalScopeUsageHandler(val context: JavaContext) : UElementHandler() {

        override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression) {
            if (node.identifier == GLOBAL_SCOPE_CLASS_SIMPLE_NAME) {
                val containingClass = node.getContainingUClass() ?: return
                val quickFix: LintFix? = when {
                    containingClass.isSuperclassViewModel()
                            && context.isViewModelArtifactInDependencies() -> {
                        LintFix.create()
                            .replace()
                            .text(GLOBAL_SCOPE_CLASS_SIMPLE_NAME)
                            .with(VIEW_MODEL_SCOPE_REFERENCE)
                            .build()
                    }

                    containingClass.isSuperclassFragment()
                            && context.isLifecycleArtifactInDependencies() -> {
                        LintFix.create()
                            .replace()
                            .text(GLOBAL_SCOPE_CLASS_SIMPLE_NAME)
                            .with(LIFECYCLE_SCOPE_REFERENCE)
                            .build()
                    }
                    else -> null
                }
                context.report(
                    issue = ISSUE,
                    scope = node,
                    location = context.getLocation(node),
                    message = BRIEF_DESCRIPTION,
                    quickfixData = quickFix
                )
            }
        }

        private fun UClass.isSuperclassViewModel(): Boolean {
            return this.supers.any {
                it.qualifiedName == VIEW_MODEL_CLASS_QUALIFIED_NAME
            }
        }

        private fun UClass.isSuperclassFragment(): Boolean {
            return this.supers.any {
                println(it.qualifiedName)
                it.qualifiedName == FRAGMENT_CLASS_QUALIFIED_NAME
            }
        }

        private fun JavaContext.isViewModelArtifactInDependencies(): Boolean {
            return evaluator.dependencies?.getAll()?.any {
                it.identifier.contains(LIFECYCLE_VIEW_MODEL_DEPENDENCY)
            } ?: false
        }

        private fun JavaContext.isLifecycleArtifactInDependencies(): Boolean {
            return evaluator.dependencies?.getAll()?.any {
                it.identifier.contains(LIFECYCLE_RUNTIME_DEPENDENCY)
            } ?: false
        }
    }
}