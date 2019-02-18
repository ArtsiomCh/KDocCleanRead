package com.github.artsiomch.kdoccleanread;

// import com.github.artsiomch.kdoccleanread.utils.JdcrPsiTreeUtils;
// import com.github.artsiomch.kdoccleanread.utils.JdcrStringUtils;
import com.github.artsiomch.kdoccleanread.utils.MarkdownTag;
import com.github.artsiomch.kdoccleanread.utils.KdsrStringUtil;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens;

public class KdcrAnnotator implements Annotator {

  private AnnotationHolder holder;
  private PsiElement element;

  private static final List<TextRange> EMPTY_LIST = Collections.emptyList();

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    this.holder = holder;
    this.element = element;

    IElementType elementType = element.getNode().getElementType();
    if (elementType == KDocTokens.TEXT) {
      String text = element.getText();

      final List<MarkdownTag> codeSpans = KdsrStringUtil.getCodeSpans(text);
      codeSpans.forEach(this::annotateCodeTag);

      final List<TextRange> excludeRanges =
          codeSpans.stream().map(tag -> tag.value).collect(Collectors.toList());
      KdsrStringUtil.getAllEmphasis(text, excludeRanges).forEach(this::annotateEmphasis);

    } else if (elementType == KDocTokens.CODE_BLOCK_TEXT) {
      doAnnotate(
          new TextRange(0, element.getTextLength()), KdcrColorSettingsPage.CODE_TAG);
      // Not really correct, but people misuse spaces for text formatting...
      // doAnnotate(KdsrStringUtil.getLastLinkName(element.getText()), KdcrColorSettingsPage.HTML_LINK_TAG);

    } else if (elementType == KDocTokens.MARKDOWN_INLINE_LINK) {
      doAnnotate(
          KdsrStringUtil.getLastLinkName(element.getText()), KdcrColorSettingsPage.HTML_LINK_TAG);
    }
    this.holder = null;
    this.element = null;
  }

  private void annotateEmphasis(MarkdownTag em) {
    // annotate em tags
    doAnnotate(em.start, KdcrColorSettingsPage.BORDERED);
    doAnnotate(em.end, KdcrColorSettingsPage.BORDERED);
    // annotate em text
    if (em.length >= 2) doAnnotate(em.value, KdcrColorSettingsPage.BOLD_FONT);
    if (em.length % 2 == 1) doAnnotate(em.value, KdcrColorSettingsPage.ITALIC_FONT);
  }

  private void annotateCodeTag(MarkdownTag codeTag) {
    doAnnotate(codeTag.start, KdcrColorSettingsPage.BORDERED);
    doAnnotate(codeTag.end, KdcrColorSettingsPage.BORDERED);
    doAnnotate(codeTag.value, KdcrColorSettingsPage.CODE_TAG);
  }

  private static int countAnnotation = 0;

  private void doAnnotate(
      TextRange rangeInElement, @NotNull TextAttributesKey textAttributesKey) {
    if (rangeInElement == null) return;

    Annotation annotation =
        holder.createInfoAnnotation(
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
