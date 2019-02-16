package com.github.artsiomch.kdoccleanread.utils;

import com.intellij.openapi.util.TextRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KdsrStringUtil {
  private static final List<MarkdownTag> EMPTY_LIST = Collections.emptyList();

  @NotNull
  public static List<MarkdownTag> getAllEmphasis(String text) {
    List<MarkdownTag> result = new ArrayList<>();
    for (char emSymbol : MarkdownTag.symbols) {
      result.addAll(getTags(text, emSymbol, false));
    }
    return result;
  }

  @NotNull
  public static List<MarkdownTag> getCodeSpans(String text) {
    return getTags(text, '`', true);
  }

  @NotNull
  private static List<MarkdownTag> getTags(String text, char emSymbol, boolean allowSpaces) {
    List<MarkdownTag> result = new ArrayList<>();
    int fromIndex = 0;
    TextRange start;
    while ((start = getTagStart(text, fromIndex, emSymbol, allowSpaces)) != null) {
      TextRange end = getTagEnd(text, start.getEndOffset(), start.substring(text), allowSpaces);
      if (end != null) {
        result.add(new MarkdownTag(start, end, emSymbol));
        fromIndex = end.getEndOffset();
      } else
        fromIndex = start.getStartOffset() + 1;
    }
    return result;
  }

  @Nullable
  private static TextRange getTagStart(
      @NotNull String text, int fromIndex, char emSymbol, boolean allowSpaces) {
    int index = fromIndex;
    while ((index = text.indexOf(emSymbol, index)) != -1) {
      TextRange emStartRange = getTagRange(text, index, emSymbol);
      if (allowSpaces
          || (emStartRange.getEndOffset() < text.length()
              && text.charAt(emStartRange.getEndOffset()) != ' ')) {
        return emStartRange;
      }
      index = emStartRange.getEndOffset();
    }
    return null;
  }

  @Nullable
  private static TextRange getTagEnd(
      @NotNull String text, int fromIndex, String emStartText, boolean allowSpaces) {
    int index = fromIndex;
    while ((index = text.indexOf(emStartText, index)) > 0) {
      if (allowSpaces || text.charAt(index - 1) != ' ') {
        return new TextRange(index, index + emStartText.length());
      }
      index += emStartText.length();
    }
    return null;
  }

  @NotNull
  private static TextRange getTagRange(@NotNull String text, int index, char emSymbol) {
    int next = index + 1;
    while (next < text.length() && text.charAt(next) == emSymbol) {
      next++;
    }
    return new TextRange(index, next);
  }

  private static final Pattern HTML_TAG_NAME = Pattern.compile("(?<=\\[)[^]]+(?=])");
  @NotNull
  public static TextRange getLastLinkName(String text) {
    Matcher matcher = HTML_TAG_NAME.matcher(text);
    TextRange result = new TextRange(0,0);
    while (matcher.find()) {
      result = new TextRange(matcher.start(), matcher.end());
    }
    return result;
  }
}
