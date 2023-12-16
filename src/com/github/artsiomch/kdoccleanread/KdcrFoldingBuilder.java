package com.github.artsiomch.kdoccleanread;

import com.github.artsiomch.kdoccleanread.utils.KdsrStringUtil;
import com.github.artsiomch.kdoccleanread.utils.MarkdownTag;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocLink;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.lexer.KtTokens;

public class KdcrFoldingBuilder implements FoldingBuilder {

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        PsiElement root = node.getPsi();
        List<FoldingDescriptor> foldingDescriptors = new ArrayList<>();
        for (KDoc kDoc : PsiTreeUtil.findChildrenOfType(root, KDoc.class)) {
            FoldingGroup foldingGroup = FoldingGroup.newGroup("KDCR fold: " + kDoc.getTextRange().toString());

            for (LeafPsiElement el : PsiTreeUtil.findChildrenOfType(kDoc, LeafPsiElement.class)) {
                final IElementType elementType = el.getNode().getElementType();
                if (elementType == KDocTokens.TEXT) {
                    foldMarkDownTags(el, foldingDescriptors, foldingGroup);
                } else if (elementType == KDocTokens.MARKDOWN_INLINE_LINK) {
                    foldInlineLink(el, foldingDescriptors, foldingGroup);
                }
            }

            PsiTreeUtil.findChildrenOfType(kDoc, KDocLink.class)
                    .forEach(link -> foldLink(link, foldingDescriptors, foldingGroup));
        }
        return foldingDescriptors.toArray(new FoldingDescriptor[0]);
    }

    private void foldInlineLink(
            @NotNull LeafPsiElement element,
            List<FoldingDescriptor> foldingDescriptors,
            FoldingGroup foldingGroup
    ) {
        final String text = element.getText();
        final TextRange linkName = KdsrStringUtil.getLastLinkName(text);
        if (linkName == null) return;
        // `[`
        addFoldingDescriptor(element, new TextRange(0, 1), foldingDescriptors, foldingGroup);
        // `]`
        addFoldingDescriptor(element, new TextRange(linkName.getEndOffset(), linkName.getEndOffset() + 1), foldingDescriptors, foldingGroup);
        // http://...
        final TextRange httpLinkRange = new TextRange(linkName.getEndOffset() + 2, text.length() - 1);
        String placeholderText = httpLinkRange.substring(text);
        final int prefixIndex = placeholderText.indexOf("//") + 2;
        int domainEndIndex = placeholderText.indexOf('/', prefixIndex);
        domainEndIndex =
                (domainEndIndex > 0)
                        ? domainEndIndex
                        : (placeholderText.length() > 10) ? 10 : placeholderText.length();
        placeholderText = placeholderText.substring(prefixIndex, domainEndIndex);
        addFoldingDescriptor(element, httpLinkRange, placeholderText, foldingDescriptors, foldingGroup);
    }

    private void foldLink(
            @NotNull KDocLink link,
            List<FoldingDescriptor> foldingDescriptors,
            FoldingGroup foldingGroup
    ) {
        String leftBracketPlaceholder = "", rightBracketPlaceholder = "";
        PsiElement prevSibling = link.getPrevSibling();
        TextRange linkLabelRange;
        if (prevSibling != null
                && prevSibling.getNode().getElementType() == KDocTokens.TEXT
                && prevSibling.getText().charAt(prevSibling.getTextLength() - 1) == ']'
                && (linkLabelRange = KdsrStringUtil.getLastLinkName(prevSibling.getText())) != null
                && linkLabelRange.getEndOffset() == prevSibling.getTextLength() - 1) {
            // case of [label][link] -> label(link)
            leftBracketPlaceholder = "(";
            rightBracketPlaceholder = ")";
            addFoldingDescriptor(
                    prevSibling,
                    new TextRange(linkLabelRange.getStartOffset() - 1, linkLabelRange.getStartOffset()),
                    foldingDescriptors,
                    foldingGroup
            );
            addFoldingDescriptor(
                    prevSibling,
                    new TextRange(linkLabelRange.getEndOffset(), linkLabelRange.getEndOffset() + 1),
                    foldingDescriptors,
                    foldingGroup
            );
        }
        // fold only `[` and `]`
        PsiElement leftBracket = link.getFirstChild();
        if (leftBracket != null && leftBracket.getNode().getElementType() == KtTokens.LBRACKET) {
            addFoldingDescriptor(leftBracket, leftBracketPlaceholder, foldingDescriptors, foldingGroup);
        }
        PsiElement rightBracket = link.getLastChild();
        if (rightBracket != null && rightBracket.getNode().getElementType() == KtTokens.RBRACKET) {
            addFoldingDescriptor(rightBracket, rightBracketPlaceholder, foldingDescriptors, foldingGroup);
        }
    }

    private void foldMarkDownTags(
            @NotNull LeafPsiElement element,
            List<FoldingDescriptor> foldingDescriptors,
            FoldingGroup foldingGroup
    ) {
        String text = element.getText();
        final List<MarkdownTag> codeSpans = KdsrStringUtil.getCodeSpans(text);
        codeSpans.forEach(codeTag -> foldTag(element, codeTag, foldingDescriptors, foldingGroup));

        final List<TextRange> excludeRanges =
                codeSpans.stream().map(tag -> tag.value).collect(Collectors.toList());
        KdsrStringUtil.getAllEmphasis(text, excludeRanges)
                .forEach(emphasis -> foldTag(element, emphasis, foldingDescriptors, foldingGroup));
    }

    private void foldTag(
            @NotNull PsiElement element,
            @NotNull MarkdownTag tag,
            List<FoldingDescriptor> foldingDescriptors,
            FoldingGroup foldingGroup
    ) {
        addFoldingDescriptor(element, tag.start, foldingDescriptors, foldingGroup);
        addFoldingDescriptor(element, tag.end, foldingDescriptors, foldingGroup);
    }

    private void addFoldingDescriptor(
            @NotNull PsiElement element,
            String placeholderText,
            List<FoldingDescriptor> foldingDescriptors,
            FoldingGroup foldingGroup
    ) {
        addFoldingDescriptor(element, new TextRange(0, element.getTextLength()), placeholderText, foldingDescriptors, foldingGroup);
    }

    private void addFoldingDescriptor(
            @NotNull PsiElement element,
            @NotNull TextRange range,
            List<FoldingDescriptor> foldingDescriptors,
            FoldingGroup foldingGroup
    ) {
        addFoldingDescriptor(element, range, "", foldingDescriptors, foldingGroup);
    }

    private void addFoldingDescriptor(
            @NotNull PsiElement element,
            @NotNull TextRange range,
            String placeholderText,
            List<FoldingDescriptor> foldingDescriptors,
            FoldingGroup foldingGroup
    ) {
        TextRange absoluteNewRange = range.shiftRight(element.getTextRange().getStartOffset());
        foldingDescriptors.add(
                new FoldingDescriptor(
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
