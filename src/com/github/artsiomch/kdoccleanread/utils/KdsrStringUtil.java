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
  public static List<MarkdownTag> getAllEmphasis(
      String text, @NotNull List<TextRange> excludeRanges) {
    List<MarkdownTag> result = EMPTY_LIST;
    for (char emSymbol : MarkdownTag.symbols) {
      final List<MarkdownTag> tags = getTags(text, emSymbol, false, excludeRanges);
      if (tags != EMPTY_LIST) {
        if (result == EMPTY_LIST) result = new ArrayList<>();
        result.addAll(tags);
      }
    }
    return result;
  }

  @NotNull
  public static List<MarkdownTag> getCodeSpans(String text) {
    return getTags(text, '`', true, Collections.emptyList());
  }

  @NotNull
  private static List<MarkdownTag> getTags(
      String text, char emSymbol, boolean allowSpaces, @NotNull List<TextRange> excludeRanges) {
    List<MarkdownTag> result = EMPTY_LIST;
    int fromIndex = 0;
    TextRange start;
    while ((start = getTagStart(text, fromIndex, emSymbol, allowSpaces, excludeRanges)) != null) {
      TextRange end =
          getTagEnd(text, start.getEndOffset(), start.substring(text), allowSpaces, excludeRanges);
      if (end != null) {
        if (result == EMPTY_LIST) result = new ArrayList<>();
        result.add(new MarkdownTag(start, end, emSymbol));
        fromIndex = end.getEndOffset();
      } else fromIndex = start.getStartOffset() + 1;
    }
    return result;
  }

  private static int indexOutOfExcluded(
      int ptevFromIndex, int checkedIndex, @NotNull List<TextRange> excludeRanges) {
    return excludeRanges.stream()
        .filter(er -> er.contains(checkedIndex))
        .findAny()
        .map(TextRange::getEndOffset)
        .orElse(ptevFromIndex);
  }

  @Nullable
  private static TextRange getTagStart(
      @NotNull String text,
      int fromIndex,
      char emSymbol,
      boolean allowSpaces,
      @NotNull List<TextRange> excludeRanges) {
    int index;
    while ((index = text.indexOf(emSymbol, fromIndex)) != -1) {
      if ((fromIndex = indexOutOfExcluded(fromIndex, index, excludeRanges)) <= index) {
        TextRange emStartRange = getTagRange(text, index, emSymbol);
        if (allowSpaces
            || (emStartRange.getEndOffset() < text.length()
                && text.charAt(emStartRange.getEndOffset()) != ' ')) {
          return emStartRange;
        }
        fromIndex = emStartRange.getEndOffset();
      }
    }
    return null;
  }

  @Nullable
  private static TextRange getTagEnd(
      @NotNull String text,
      int fromIndex,
      String emStartText,
      boolean allowSpaces,
      @NotNull List<TextRange> excludeRanges) {
    int index;
    while ((index = text.indexOf(emStartText, fromIndex)) > 0) {
      if ((fromIndex = indexOutOfExcluded(fromIndex, index, excludeRanges)) <= index) {
        if (allowSpaces || text.charAt(index - 1) != ' ') {
          return new TextRange(index, index + emStartText.length());
        }
        fromIndex += emStartText.length();
      }
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

  @Nullable
  public static TextRange getLastLinkName(String text) {
    Matcher matcher = HTML_TAG_NAME.matcher(text);
    TextRange result = null;
    while (matcher.find()) {
      result = new TextRange(matcher.start(), matcher.end());
    }
    return result;
  }
}
