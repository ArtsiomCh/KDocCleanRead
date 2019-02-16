package com.github.artsiomch.kdoccleanread;

import org.jetbrains.kotlin.lexer.KtTokens;

import com.github.artsiomch.kdoccleanread.utils.KdsrStringUtil;
import com.github.artsiomch.kdoccleanread.utils.MarkdownTag;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocLink;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection;

public class KdcrFoldingBuilder implements FoldingBuilder {

  private List<FoldingDescriptor> foldingDescriptors;
  private FoldingGroup foldingGroup;

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    PsiElement root = node.getPsi();
    foldingDescriptors = new ArrayList<>();
    for (KDocSection kdocSection : PsiTreeUtil.findChildrenOfType(root, KDocSection.class)) {
      foldingGroup = FoldingGroup.newGroup("KDCR fold: " + kdocSection.getTextRange().toString());

      PsiTreeUtil.findChildrenOfType(kdocSection, LeafPsiElement.class).stream()
          .filter(el -> el.getNode().getElementType() == KDocTokens.TEXT)
          .forEach(this::foldMarkDownTags);

      PsiTreeUtil.findChildrenOfType(kdocSection, KDocLink.class).forEach(this::foldLink);
    }
    return foldingDescriptors.toArray(new FoldingDescriptor[0]);
  }

  private void foldLink(@NotNull KDocLink link) {
    String leftBracketPlaceholder = "", rightBracketPlaceholder = "";
    PsiElement prevSibling = link.getPrevSibling();
    TextRange linkLabelRange;
    if (prevSibling != null
        && prevSibling.getNode().getElementType() == KDocTokens.TEXT
        && prevSibling.getText().charAt(prevSibling.getTextLength() - 1) == ']'
        && (linkLabelRange = KdsrStringUtil.getLastLinkName(prevSibling.getText())).getLength() != 0
        && linkLabelRange.getEndOffset() == prevSibling.getTextLength() - 1) {
      // case of [label][link] -> label(link)
      leftBracketPlaceholder = "(";
      rightBracketPlaceholder = ")";
      addFoldingDescriptor(prevSibling, new TextRange(linkLabelRange.getStartOffset()-1, linkLabelRange.getStartOffset()));
      addFoldingDescriptor(prevSibling, new TextRange(linkLabelRange.getEndOffset(), linkLabelRange.getEndOffset()+1));
    }
    // fold only `[` and `]`
    PsiElement leftBracket = link.getFirstChild();
    if (leftBracket != null && leftBracket.getNode().getElementType() == KtTokens.LBRACKET) {
      addFoldingDescriptor(leftBracket, leftBracketPlaceholder);
    }
    PsiElement rightBracket = link.getLastChild();
    if (rightBracket != null && rightBracket.getNode().getElementType() == KtTokens.RBRACKET) {
      addFoldingDescriptor(rightBracket, rightBracketPlaceholder);
    }
  }

  private void foldMarkDownTags(@NotNull LeafPsiElement element) {
    String text = element.getText();
    KdsrStringUtil.getAllEmphasis(text).forEach(emphasis -> foldTag(element, emphasis));
    KdsrStringUtil.getCodeSpans(text).forEach(codeTag -> foldTag(element, codeTag));
  }

  private void foldTag(@NotNull PsiElement element, @NotNull MarkdownTag tag) {
    addFoldingDescriptor(element, tag.start);
    addFoldingDescriptor(element, tag.end);
  }

  private void addFoldingDescriptor(@NotNull PsiElement element) {
    addFoldingDescriptor(element, new TextRange(0, element.getTextLength()));
  }

  private void addFoldingDescriptor(@NotNull PsiElement element, String placeholderText) {
    addFoldingDescriptor(element, new TextRange(0, element.getTextLength()), placeholderText);
  }

  private void addFoldingDescriptor(@NotNull PsiElement element, @NotNull TextRange range) {
    addFoldingDescriptor(element, range, "");
  }

  private void addFoldingDescriptor(
      @NotNull PsiElement element, @NotNull TextRange range, String placeholderText) {
    TextRange absoluteNewRange = range.shiftRight(element.getTextRange().getStartOffset());
    foldingDescriptors.add(
        new NamedFoldingDescriptor(
            element.getNode(), absoluteNewRange, foldingGroup, placeholderText));
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    return null;
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return true;
  }
}
