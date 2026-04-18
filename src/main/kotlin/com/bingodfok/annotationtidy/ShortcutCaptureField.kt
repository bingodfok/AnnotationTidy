package com.bingodfok.annotationtidy

import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.ui.KeyStrokeAdapter
import com.intellij.ui.components.JBTextField
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

class ShortcutCaptureField : JBTextField() {

    private var capturedKeyStroke: KeyStroke? = null

    init {
        columns = 18
        isEditable = false
        focusTraversalKeysEnabled = false
        toolTipText = "聚焦后直接按下新的快捷键，按 Backspace 或 Delete 可恢复默认。"

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                when (event.keyCode) {
                    KeyEvent.VK_BACK_SPACE,
                    KeyEvent.VK_DELETE,
                    -> {
                        setKeyStroke(null)
                        event.consume()
                        return
                    }
                }

                val keyStroke = KeyStrokeAdapter.getDefaultKeyStroke(event)
                if (keyStroke != null && !isModifierOnly(event.keyCode)) {
                    setKeyStroke(keyStroke)
                }
                event.consume()
            }

            override fun keyTyped(event: KeyEvent) {
                event.consume()
            }

            override fun keyReleased(event: KeyEvent) {
                event.consume()
            }
        })
    }

    fun setKeyStroke(keyStroke: KeyStroke?) {
        capturedKeyStroke = keyStroke
        text = keyStroke?.let(KeymapUtil::getKeystrokeText) ?: ""
    }

    fun getKeyStroke(): KeyStroke? = capturedKeyStroke

    private fun isModifierOnly(keyCode: Int): Boolean {
        return keyCode == KeyEvent.VK_SHIFT
            || keyCode == KeyEvent.VK_CONTROL
            || keyCode == KeyEvent.VK_ALT
            || keyCode == KeyEvent.VK_META
    }
}
