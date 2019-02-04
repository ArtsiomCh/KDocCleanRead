package com.github.artsiomch.kdoccleanread;

//import com.github.artsiomch.kdoccleanread.utils.JdcrPsiTreeUtils;
//import com.github.artsiomch.kdoccleanread.utils.JdcrStringUtils;
import com.github.artsiomch.kdoccleanread.utils.Emphasis;
import com.github.artsiomch.kdoccleanread.utils.KdsrStringUtil;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaDocTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens;

public class KdcrAnnotator implements Annotator {

  private AnnotationHolder holder;
  private PsiElement element;
  private List<TextRange> foundEmphasisTags = EMPTY_LIST;

  private static final List<TextRange> EMPTY_LIST = Collections.emptyList();

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    this.holder = holder;
    this.element = element;

    if ( element.getNode().getElementType() == KDocTokens.TEXT) {
//        && !JdcrPsiTreeUtils.isInsideCodeOrLiteralTag((PsiDocToken) element))

        // Annotate Font style HTML tags
      KdsrStringUtil.getEmphasis(element.getText()).forEach(this::annotateEmphasisText);
//      doAnnotate(BOLD_TAG, KdcrColorSettingsPage.BOLD_FONT);
//      doAnnotate(ITALIC_TAG, KdcrColorSettingsPage.ITALIC_FONT);

    }
    this.holder = null;
    this.element = null;
    foundEmphasisTags = EMPTY_LIST;
  }

  private void annotateEmphasisText(Emphasis em) {

  }

  private static int countAnnotation = 0;

  private void doAnnotate(
      @NotNull TextRange rangeInElement, @NotNull TextAttributesKey textAttributesKey) {
    if (rangeInElement.getLength() == 0) return;

    Annotation annotation = holder.createInfoAnnotation(
        rangeInElement.shiftRight(element.getTextRange().getStartOffset()),
        textAttributesKey.getExternalName());
    annotation.setTooltip(null);
    annotation.setTextAttributes(textAttributesKey);
    /*
        countAnnotation++;
        if (countAnnotation % 1000 == 1)
          System.out.printf("%s  %6d annotations\n",
              LocalDateTime.now(),
              countAnnotation
          );
    */
  }
}
