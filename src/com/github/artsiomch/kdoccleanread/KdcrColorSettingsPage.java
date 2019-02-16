package com.github.artsiomch.kdoccleanread;

import com.intellij.lang.java.JavaLanguage;
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

public class KdcrColorSettingsPage implements ColorSettingsPage {
  public static final TextAttributesKey CODE_TAG =
      TextAttributesKey.createTextAttributesKey(
          "KDCR_CODE", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
  public static final TextAttributesKey LINK_TAG =
      TextAttributesKey.createTextAttributesKey(
          "link tag", DefaultLanguageHighlighterColors.CLASS_NAME);
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
          new AttributesDescriptor("Link name: [link name](https://...)", HTML_LINK_TAG),
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
    return JavaSyntaxHighlighterFactory.getSyntaxHighlighter(JavaLanguage.INSTANCE, null, null);
  }

  @NotNull
  @Override
  public String getDemoText() {
    return "/**\n"
        + " * To convert any <_bor><tt></_bor><_code>object</_code><_bor></tt></_bor> of "
        + "{@code <_code>Object</_code>} class to "
        + "<_bor><code></_bor><_code>String</_code><_bor></code></_bor> use \n"
        + " * {@link java.lang.Object#<_link>toString()</_link> toString()} method.\n"
        + " * html link <_bor><a href=\"http://www.jetbrains.org\"></_bor>"
        + "<_a>JetBrains</_a><_bor></a></_bor>.\n"
        + " * <_bor><b></_bor><_b>bold text</_b><_bor></b></_bor> "
        + "and <_bor><i></_bor><_i>italic text</_i><_bor></i></_bor>.\n"
        + " */";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ContainerUtil.newHashMap(
        Arrays.asList("_code", "_link", "_a", "_b", "_i", "_bor"),
        Arrays.asList(CODE_TAG, LINK_TAG, HTML_LINK_TAG, BOLD_FONT, ITALIC_FONT, BORDERED));
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
