package com.bingo.annotationtidy

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class AnnotationLayoutServiceTest : BasePlatformTestCase() {

    fun testAnnotationsAreSplitAndSorted() {
        val file = myFixture.configureByText(
            "Demo.java",
            """
            import org.jetbrains.annotations.NotNull;

            class Demo {
                @Deprecated @NotNull @Override public String value() {
                    return "";
                }
            }
            """.trimIndent(),
        )

        val changed = AnnotationLayoutService.tidyJavaFile(file as com.intellij.psi.PsiJavaFile)

        assertEquals(1, changed)
        myFixture.checkResult(
            """
            import org.jetbrains.annotations.NotNull;

            class Demo {
                @Override
                @Deprecated
                @NotNull
                public String value() {
                    return "";
                }
            }
            """.trimIndent(),
        )
    }
}
