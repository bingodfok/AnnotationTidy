package com.bingo.annotationtidy

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierList
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleManager

object AnnotationLayoutService {

    private val modifierOrder = listOf(
        PsiModifier.PUBLIC,
        PsiModifier.PROTECTED,
        PsiModifier.PRIVATE,
        PsiModifier.ABSTRACT,
        PsiModifier.STATIC,
        PsiModifier.FINAL,
        PsiModifier.TRANSIENT,
        PsiModifier.VOLATILE,
        PsiModifier.SYNCHRONIZED,
        PsiModifier.NATIVE,
        PsiModifier.STRICTFP,
        PsiModifier.DEFAULT,
        PsiModifier.SEALED,
        PsiModifier.NON_SEALED,
    )

    private val groupedAnnotations = listOf(
        0 to setOf("Override", "Deprecated", "SafeVarargs", "FunctionalInterface"),
        1 to setOf("NotNull", "Nullable", "NonNull", "CheckForNull", "Contract", "Range"),
        2 to setOf("Valid", "NotBlank", "NotEmpty", "Size", "Min", "Max", "Pattern"),
        3 to setOf("Inject", "Autowired", "Resource", "Qualifier", "Value"),
        4 to setOf("RequestMapping", "GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping"),
        5 to setOf("Entity", "Table", "Id", "GeneratedValue", "Column", "Enumerated"),
    )

    fun tidyJavaFile(file: PsiJavaFile): Int {
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return 0
        val edits = collectEdits(file, document)
        if (edits.isEmpty()) {
            return 0
        }

        edits.sortedByDescending { it.range.startOffset }
            .forEach { edit -> document.replaceString(edit.range.startOffset, edit.range.endOffset, edit.replacement) }

        PsiDocumentManager.getInstance(file.project).commitDocument(document)
        CodeStyleManager.getInstance(file.project).reformat(file)
        return edits.size
    }

    private fun collectEdits(file: PsiJavaFile, document: Document): List<AnnotationEdit> {
        val edits = mutableListOf<AnnotationEdit>()

        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is PsiModifierListOwner && supportsTidying(element)) {
                    buildEdit(element, document)?.let(edits::add)
                }
                super.visitElement(element)
            }
        })

        return edits
    }

    private fun buildEdit(owner: PsiModifierListOwner, document: Document): AnnotationEdit? {
        val modifierList = owner.modifierList ?: return null
        val annotations = modifierList.annotations.toList()
        if (annotations.isEmpty()) {
            return null
        }

        val firstAnnotation = annotations.first()
        val firstSignificantSibling = nextSignificantSibling(modifierList) ?: return null

        val indent = lineIndent(document, firstAnnotation.textRange.startOffset)
        val sortedAnnotations = annotations
            .sortedWith(compareBy<PsiAnnotation>({ annotationPriority(it) }, { annotationName(it) }, { it.text }))
            .map { it.text.trim() }

        val modifiersText = modifierOrder
            .filter(modifierList::hasModifierProperty)
            .joinToString(" ")

        val rebuiltPrefix = buildString {
            append(sortedAnnotations.joinToString(separator = "\n$indent"))
            append("\n")
            append(indent)
            if (modifiersText.isNotEmpty()) {
                append(modifiersText)
                append(" ")
            }
        }

        val targetRange = TextRange(firstAnnotation.textRange.startOffset, firstSignificantSibling.textRange.startOffset)
        val currentText = document.getText(targetRange)
        if (currentText == rebuiltPrefix) {
            return null
        }

        return AnnotationEdit(targetRange, rebuiltPrefix)
    }

    private fun nextSignificantSibling(modifierList: PsiModifierList): PsiElement? {
        var sibling = modifierList.nextSibling
        while (sibling != null) {
            if (sibling !is PsiWhiteSpace) {
                return sibling
            }
            sibling = sibling.nextSibling
        }
        return null
    }

    private fun lineIndent(document: Document, offset: Int): String {
        val line = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(line)
        val text = document.charsSequence
        var cursor = lineStart
        while (cursor < offset && (text[cursor] == ' ' || text[cursor] == '\t')) {
            cursor++
        }
        return text.subSequence(lineStart, cursor).toString()
    }

    private fun annotationPriority(annotation: PsiAnnotation): Int {
        val shortName = annotationName(annotation)
        return groupedAnnotations.firstOrNull { (_, names) -> shortName in names }?.first ?: 99
    }

    private fun annotationName(annotation: PsiAnnotation): String {
        return annotation.qualifiedName?.substringAfterLast('.') ?: annotation.nameReferenceElement?.referenceName ?: annotation.text
    }

    private fun supportsTidying(owner: PsiModifierListOwner): Boolean {
        return owner is PsiClass || owner is PsiMethod || owner is PsiField
    }
}

private data class AnnotationEdit(
    val range: TextRange,
    val replacement: String,
)
