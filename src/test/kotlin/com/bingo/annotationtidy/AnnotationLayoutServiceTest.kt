package com.bingo.annotationtidy

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class AnnotationLayoutServiceTest : BasePlatformTestCase() {

    fun testAnnotationsAreSplitAndSorted() {
        val file = myFixture.configureByText(
            "Demo.java",
            """
            class Demo {
                @SuppressWarnings("unchecked") @Deprecated @Override public String value() {
                    return "";
                }
            }
            """.trimIndent(),
        )

        val changed = AnnotationLayoutService.tidyJavaFile(file as com.intellij.psi.PsiJavaFile)

        assertEquals(1, changed)
        myFixture.checkResult(
            """
            class Demo {
                @Override
                @Deprecated
                @SuppressWarnings("unchecked")
                public String value() {
                    return "";
                }
            }
            """.trimIndent(),
        )
    }
}
