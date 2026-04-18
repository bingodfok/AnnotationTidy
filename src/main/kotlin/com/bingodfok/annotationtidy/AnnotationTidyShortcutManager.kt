package com.bingodfok.annotationtidy

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.KeyStrokeAdapter
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

object AnnotationTidyShortcutManager {

    const val actionId = "AnnotationTidy.TidyJavaAnnotationsAction"

    private const val defaultShortcutText = "ctrl alt shift A"
    private const val managedKeymapSuffix = " + Annotation Tidy"

    private val defaultKeyStroke: KeyStroke = KeyStrokeAdapter.getKeyStroke(defaultShortcutText)
        ?: error("Failed to parse default shortcut: $defaultShortcutText")

    fun getDefaultShortcutLabel(): String = KeymapUtil.getKeystrokeText(defaultKeyStroke)

    fun getActiveKeymapName(): String = KeymapManagerEx.getInstanceEx().activeKeymap.presentableName

    fun getCurrentKeyStroke(): KeyStroke {
        val currentShortcut = findPrimaryKeyboardShortcut(KeymapManagerEx.getInstanceEx().activeKeymap)
        return currentShortcut ?: defaultKeyStroke
    }

    @Throws(ConfigurationException::class)
    fun applyShortcut(keyStroke: KeyStroke?) {
        validateKeyStroke(keyStroke)

        val keymapManager = KeymapManagerEx.getInstanceEx()
        val writableKeymap = ensureWritableKeymap(keymapManager)
        val targetShortcut = KeyboardShortcut(keyStroke ?: defaultKeyStroke, null)

        validateConflicts(writableKeymap, targetShortcut)

        writableKeymap.getShortcuts(actionId)
            .filterIsInstance<KeyboardShortcut>()
            .forEach { writableKeymap.removeShortcut(actionId, it) }

        writableKeymap.addShortcut(actionId, targetShortcut)

        ApplicationManager.getApplication().executeOnPooledThread {
            keymapManager.schemeManager.save()
        }
    }

    private fun ensureWritableKeymap(keymapManager: KeymapManagerEx): Keymap {
        val activeKeymap = keymapManager.activeKeymap
        if (activeKeymap.canModify()) {
            return activeKeymap
        }

        val preferredName = "${activeKeymap.presentableName}$managedKeymapSuffix"
        val existing = keymapManager.getKeymap(preferredName)
        if (existing != null && existing.canModify() && existing.parent?.name == activeKeymap.name) {
            keymapManager.setActiveKeymap(existing)
            return existing
        }

        val derivedName = generateSequence(0) { it + 1 }
            .map { index -> if (index == 0) preferredName else "$preferredName $index" }
            .first { keymapManager.getKeymap(it) == null }

        val derivedKeymap = activeKeymap.deriveKeymap(derivedName)
        keymapManager.schemeManager.addScheme(derivedKeymap)
        keymapManager.setActiveKeymap(derivedKeymap)
        return derivedKeymap
    }

    @Throws(ConfigurationException::class)
    private fun validateKeyStroke(keyStroke: KeyStroke?) {
        if (keyStroke == null) {
            return
        }

        if (isModifierOnly(keyStroke.keyCode)) {
            throw ConfigurationException("快捷键不能只包含修饰键。")
        }

        val modifiers = keyStroke.modifiers and modifierMask
        val isFunctionKey = keyStroke.keyCode in KeyEvent.VK_F1..KeyEvent.VK_F24
        if (modifiers == 0 && !isFunctionKey) {
            throw ConfigurationException("快捷键至少包含一个修饰键，或者使用 F 键。")
        }
    }

    @Throws(ConfigurationException::class)
    private fun validateConflicts(keymap: Keymap, shortcut: KeyboardShortcut) {
        val conflictingIds = keymap.getConflicts(actionId, shortcut)
            .keys
            .filterNot { it == actionId }

        if (conflictingIds.isEmpty()) {
            return
        }

        val readableConflicts = conflictingIds.joinToString(separator = "、") { actionId ->
            ActionManager.getInstance().getAction(actionId)?.templateText?.takeIf { it.isNotBlank() } ?: actionId
        }

        throw ConfigurationException(
            "快捷键 ${KeymapUtil.getShortcutText(shortcut)} 已被以下动作占用：$readableConflicts。请换一个组合键。",
        )
    }

    private fun findPrimaryKeyboardShortcut(keymap: Keymap): KeyStroke? {
        return keymap.getShortcuts(actionId)
            .filterIsInstance<KeyboardShortcut>()
            .firstOrNull { it.secondKeyStroke == null }
            ?.firstKeyStroke
    }

    private fun isModifierOnly(keyCode: Int): Boolean {
        return keyCode == KeyEvent.VK_SHIFT
            || keyCode == KeyEvent.VK_CONTROL
            || keyCode == KeyEvent.VK_ALT
            || keyCode == KeyEvent.VK_META
    }

    private const val modifierMask =
        InputEvent.CTRL_DOWN_MASK or
            InputEvent.ALT_DOWN_MASK or
            InputEvent.SHIFT_DOWN_MASK or
            InputEvent.META_DOWN_MASK
}
