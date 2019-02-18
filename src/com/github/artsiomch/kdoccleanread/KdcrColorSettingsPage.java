package com.github.artsiomch.kdoccleanread;

import com.intellij.lang.java.JavaSyntaxHighlighterFactory;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import org.jetbrains.kotlin.idea.KotlinLanguage;

public class KdcrColorSettingsPage implements ColorSettingsPage {
  public static final TextAttributesKey CODE_TAG =
      TextAttributesKey.createTextAttributesKey(
          "KDCR_CODE", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
  public static final TextAttributesKey HTML_LINK_TAG =
      TextAttributesKey.createTextAttributesKey(
          "KDCR_HTML_LINK", DefaultLanguageHighlighterColors.NUMBER);

  public static final TextAttributesKey BOLD_FONT =
      TextAttributesKey.createTextAttributesKey("KDCR_BOLD");
  public static final TextAttributesKey ITALIC_FONT =
      TextAttributesKey.createTextAttributesKey("KDCR_ITALIC");
  public static final TextAttributesKey BORDERED =
      TextAttributesKey.createTextAttributesKey("KDCR_BORDERED");

  private static final AttributesDescriptor[] DESCRIPTORS =
      new AttributesDescriptor[] {
          new AttributesDescriptor("Tag value of `code` or \"    code\"", CODE_TAG),
          new AttributesDescriptor("Link name: [link name](https...)", HTML_LINK_TAG),
          new AttributesDescriptor("Emphasis strong: **abc** | __abc__", BOLD_FONT),
          new AttributesDescriptor("Emphasis regular: *abc* | _abc_", ITALIC_FONT),
          new AttributesDescriptor("Markdown tags: * | _ | ` ", BORDERED)
      };

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return JavaSyntaxHighlighterFactory.getSyntaxHighlighter(KotlinLanguage.INSTANCE, null, null);
  }

  @NotNull
  @Override
  public String getDemoText() {
    return "/**\n"
        + " * <_bor>`</_bor><_code>code</_code><_bor>`</_bor> \n"
        + " *     <_code>code</_code>\n"
        + " * [<_link>JetBrains</_link>](http://www.jetbrains.org)\n"
        + " * <_bor>**</_bor><_b>strong emphasis</_b><_bor>**</_bor> <_bor>__</_bor><_b>strong emphasis</_b><_bor>__</_bor> \n"
        + " * <_bor>*</_bor><_i>regular emphasis</_i><_bor>*</_bor> <_bor>_</_bor><_i>regular emphasis</_i><_bor>_</_bor> \n"
        + " */";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ContainerUtil.newHashMap(
        Arrays.asList("_code", "_link", "_b", "_i", "_bor"),
        Arrays.asList(CODE_TAG, HTML_LINK_TAG, BOLD_FONT, ITALIC_FONT, BORDERED));
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "KDoc Clean Read";
  }
}
