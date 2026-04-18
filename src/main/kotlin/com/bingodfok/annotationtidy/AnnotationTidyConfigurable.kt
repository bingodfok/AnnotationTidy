package com.bingodfok.annotationtidy

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class AnnotationTidyConfigurable : Configurable {

    private var rootPanel: JPanel? = null
    private var keymapLabel: JBLabel? = null
    private var shortcutField: ShortcutCaptureField? = null
    private var reformatCodeCheckBox: JBCheckBox? = null

    override fun getDisplayName(): String = "Annotation Tidy"

    override fun createComponent(): JComponent {
        if (rootPanel == null) {
            keymapLabel = JBLabel()
            shortcutField = ShortcutCaptureField()
            reformatCodeCheckBox = JBCheckBox("整理注解后继续整理代码并优化 imports")

            val restoreDefaultButton = JButton("恢复默认").apply {
                addActionListener {
                    shortcutField?.setKeyStroke(null)
                }
            }

            val shortcutRow = JPanel(BorderLayout(JBUI.scale(8), 0)).apply {
                add(shortcutField, BorderLayout.CENTER)
                add(restoreDefaultButton, BorderLayout.EAST)
            }

            val hintLabel = JBLabel(
                "<html>聚焦输入框后直接按下新的组合键。设置会写入当前活动 Keymap；" +
                    "如果当前 Keymap 不可编辑，插件会自动创建一份可编辑副本。留空会恢复默认快捷键 " +
                    AnnotationTidyShortcutManager.getDefaultShortcutLabel() +
                    "。关闭“整理代码”后，插件只会调整注解布局，不会继续格式化代码或优化 imports" +
                    "。</html>",
            ).apply {
                border = JBUI.Borders.emptyTop(4)
            }

            rootPanel = FormBuilder.createFormBuilder()
                .setVerticalGap(JBUI.scale(10))
                .addLabeledComponent("当前 Keymap", keymapLabel!!)
                .addLabeledComponent("整理快捷键", shortcutRow)
                .addComponent(reformatCodeCheckBox!!)
                .addComponent(hintLabel)
                .addComponentFillVertically(JPanel(), 0)
                .panel
        }

        reset()
        return rootPanel!!
    }

    override fun isModified(): Boolean {
        val currentField = shortcutField ?: return false
        return currentField.getKeyStroke() != AnnotationTidyShortcutManager.getCurrentKeyStroke()
            || reformatCodeCheckBox?.isSelected != AnnotationTidySettings.getInstance().reformatCodeAfterTidying
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val selectedKeyStroke = shortcutField?.getKeyStroke()
        if (selectedKeyStroke != AnnotationTidyShortcutManager.getCurrentKeyStroke()) {
            AnnotationTidyShortcutManager.applyShortcut(selectedKeyStroke)
        }
        AnnotationTidySettings.getInstance().reformatCodeAfterTidying = reformatCodeCheckBox?.isSelected ?: true
        reset()
    }

    override fun reset() {
        shortcutField?.setKeyStroke(AnnotationTidyShortcutManager.getCurrentKeyStroke())
        reformatCodeCheckBox?.isSelected = AnnotationTidySettings.getInstance().reformatCodeAfterTidying
        refreshUiState()
    }

    override fun disposeUIResources() {
        rootPanel = null
        keymapLabel = null
        shortcutField = null
        reformatCodeCheckBox = null
    }

    private fun refreshUiState() {
        keymapLabel?.text = AnnotationTidyShortcutManager.getActiveKeymapName()
    }
}
