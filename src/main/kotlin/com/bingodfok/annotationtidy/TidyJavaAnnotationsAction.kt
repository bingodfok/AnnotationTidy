package com.bingodfok.annotationtidy

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiJavaFile

class TidyJavaAnnotationsAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        event.presentation.isEnabledAndVisible = file is PsiJavaFile
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = event.getData(CommonDataKeys.PSI_FILE) as? PsiJavaFile ?: return

        val changedCount = WriteCommandAction.writeCommandAction(project, file)
            .withName("Tidy Java Annotations")
            .compute<Int, RuntimeException> {
                AnnotationLayoutService.tidyJavaFile(file)
            }

        val title = "Annotation Tidy"
        val content = if (changedCount == 0) {
            "当前文件没有需要整理的 Java 注解。"
        } else {
            "已整理 $changedCount 处注解声明。"
        }

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Annotation Tidy")
            .createNotification(title, content, NotificationType.INFORMATION)
            .notify(project)
    }
}
