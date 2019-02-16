package com.github.artsiomch.kdoccleanread.utils;

import com.intellij.openapi.util.TextRange;

public class MarkdownTag {

  public TextRange start, end, value;
  public int length;
  public char symbol;

  public final static char[] symbols = {'*', '_'};

  public MarkdownTag(TextRange start, TextRange end, char symbol){
    super();
    assert start.getLength() == end.getLength() : start + " " + end;
    this.start = start;
    this.end = end;
    value = new TextRange(start.getEndOffset(), end.getStartOffset());
    this.symbol = symbol;
    length = start.getLength();
  }
}
