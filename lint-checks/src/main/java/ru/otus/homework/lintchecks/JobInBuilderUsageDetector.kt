package ru.otus.homework.lintchecks

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.getUMethod
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getQualifiedName
import org.jetbrains.uast.tryResolveNamed
import org.jetbrains.uast.visitor.AbstractUastVisitor

private const val ID = "JobInBuilderUsage"
private const val BRIEF_DESCRIPTION_JOB = "Job instance should not be passed into coroutine builder."
private const val BRIEF_DESCRIPTION_NON_CANCELLABLE = "NonCancellable instance usage inside " +
        "coroutine builder will brake an exception handling for all coroutines in the hierarchy."
private const val EXPLANATION = "Using Job instance inside coroutine builders has no effect," +
        "it can break expected error handling and coroutines cancellation."
private const val PRIORITY = 7
private const val CATEGORY_PRIORITY = 2
private const val CATEGORY_NAME = "Job in builder usage"
private const val ASYNC_CALL = "async"
private const val LAUNCH_CALL = "launch"
private const val WITH_CONTEXT = "withContext(coroutineContext)"
private const val VIEW_MODEL_CLASS_QUALIFIED_NAME = "androidx.lifecycle.ViewModel"
private const val LIFECYCLE_PACKAGE = "androidx.lifecycle"
private const val VIEW_MODEL_SCOPE_METHOD = "getViewModelScope"
private const val NON_CANCELABLE = "kotlinx.coroutines.NonCancellable"
private const val SUPERVISOR_JOB_QUALIFIED_NAME = "kotlinx.coroutines.SupervisorJob"
private const val SUPERVISOR_JOB_NAME = "SupervisorJob"
private const val JOB = "kotlinx.coroutines.Job"
private const val SUPERVISOR_JOB_FIX_MESSAGE = "Remove SupervisorJob function call"
private const val REPLACE_BY_WITH_CONTEXT_FIX_MESSAGE = "Replace launch/async call by withContext"


class JobInBuilderUsageDetector : Detector(), Detector.UastScanner {
    companion object {
        val ISSUE = Issue.create(
            id = ID,
            briefDescription = BRIEF_DESCRIPTION_JOB,
            explanation = EXPLANATION,
            category = Category.create(CATEGORY_NAME, CATEGORY_PRIORITY),
            priority = PRIORITY,
            severity = Severity.WARNING,
            implementation = Implementation(
                JobInBuilderUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun getApplicableMethodNames(): List<String> {
        return listOf(LAUNCH_CALL, ASYNC_CALL)
    }

    override fun visitMethodCall(
        context: JavaContext,
        metodCallRootNode: UCallExpression,
        method: PsiMethod
    ) {
        metodCallRootNode.accept(
            visitor = object : AbstractUastVisitor() {
                override fun visitSimpleNameReferenceExpression(
                    node: USimpleNameReferenceExpression
                ): Boolean {
                    if (node.isNonCancellable()) {
                        metodCallRootNode.accept(
                            visitor = object : AbstractUastVisitor() {
                                override fun visitLambdaExpression(
                                    node: ULambdaExpression
                                ): Boolean {
                                    node.accept(
                                        visitor = object : AbstractUastVisitor() {
                                            override fun visitCallExpression(node: UCallExpression): Boolean {
                                                if (node.isLaunchOrAsync()) {
                                                    val fix = createReplaceByWithContextFix(
                                                        context = context,
                                                        node = node
                                                    )
                                                    showReport(
                                                        context = context,
                                                        node = node,
                                                        message = BRIEF_DESCRIPTION_NON_CANCELLABLE,
                                                        fix = fix)
                                                }
                                                return super.visitCallExpression(node)
                                            }
                                        }
                                    )
                                    return super.visitLambdaExpression(node)
                                }
                            }
                        )
                    }
                    return super.visitSimpleNameReferenceExpression(node)
                }
            }
        )
        metodCallRootNode.accept(
            visitor = object : AbstractUastVisitor() {
                override fun visitExpression(node: UExpression): Boolean {
                    when {
                        node.isSupervisorJob(context = context)
                                && metodCallRootNode.isReceiverViewModelScope(context)
                                && metodCallRootNode.isContainingClassViewModel() -> {
                            val fix = createRemoveSupervisorJobFix(
                                context = context,
                                node = node
                            )
                            showReport(
                                context = context,
                                node = node,
                                message = BRIEF_DESCRIPTION_JOB,
                                fix = fix) }
                    }
                    return super.visitExpression(node)
                }

            }
        )
        metodCallRootNode.valueArguments.forEach { argNode ->
            if (argNode.isJob(context)) {
                showReport(context = context, node = argNode, message = BRIEF_DESCRIPTION_JOB)
            }
        }
    }

    private fun showReport(
        context: JavaContext,
        node: UElement,
        message: String,
        fix: LintFix? = null) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = message,
            quickfixData = fix
        )
    }

    private fun UCallExpression.isLaunchOrAsync(): Boolean {
        return methodName == LAUNCH_CALL || methodName == ASYNC_CALL
    }

    private fun UCallExpression.isReceiverViewModelScope(
        context: JavaContext,
    ): Boolean {
        val resolvedReceiver = receiver?.tryResolveNamed()
        val expressionName = resolvedReceiver?.name
        val packageName = resolvedReceiver?.let { context.evaluator.getPackage(it) }?.qualifiedName
        return packageName == LIFECYCLE_PACKAGE && expressionName == VIEW_MODEL_SCOPE_METHOD
    }

    private fun UExpression.isJob(
        context: JavaContext,
    ): Boolean {
        val launchOrAsyncArg = context.evaluator.getTypeClass(
            psiType = getExpressionType()
        )
        return context.evaluator.inheritsFrom(launchOrAsyncArg, JOB, false)
    }

    private fun UExpression.isSupervisorJob(
        context: JavaContext
    ): Boolean {
        return if (this is UCallExpression) {
            val uMethod = resolve()?.getUMethod()
            val packageName = context.evaluator.getPackage(uMethod!!)?.qualifiedName
            val methodName = methodName
            "$packageName.$methodName" == SUPERVISOR_JOB_QUALIFIED_NAME && isJob(context)
        } else false
    }

    private fun UExpression.isContainingClassViewModel(): Boolean {
        val containingClass = getContainingUClass()
        return containingClass?.supers?.any {
            it.qualifiedName == VIEW_MODEL_CLASS_QUALIFIED_NAME
        } ?: false
    }

    private fun UExpression.isNonCancellable(): Boolean {
        return if (this is USimpleNameReferenceExpression) {
            getQualifiedName() == NON_CANCELABLE
        } else false
    }

    private fun createRemoveSupervisorJobFix(
        context: JavaContext,
        node: UExpression
    ): LintFix {
        val nodeToFix = if (node.uastParent is UBinaryExpression) {
            node.uastParent as UBinaryExpression
        } else {
            node
        }
        val oldText = nodeToFix.sourcePsi?.text
        val operands = oldText?.split("+")?.toMutableList()
        operands?.removeIf {
            it.contains(SUPERVISOR_JOB_NAME)
        }
        val newTextStringBuilder = StringBuilder()
        operands?.forEachIndexed() { index, string ->
            if (index == 0) {
                newTextStringBuilder.append(string.trim())
            } else {
                newTextStringBuilder.append(" + " + string.trim())
            }
        }
        val newText = newTextStringBuilder.toString().trim()
        return LintFix.create()
            .replace()
            .range(context.getLocation(nodeToFix))
            .all()
            .with(newText)
            .name(SUPERVISOR_JOB_FIX_MESSAGE)
            .build()
    }

    private fun createReplaceByWithContextFix(
        context: JavaContext,
        node: UExpression
    ): LintFix {
        node as UCallExpression
        return LintFix.create()
            .replace()
            .range(context.getLocation(node))
            .text(node.methodName)
            .with(WITH_CONTEXT)
            .name(REPLACE_BY_WITH_CONTEXT_FIX_MESSAGE)
            .build()
    }

}