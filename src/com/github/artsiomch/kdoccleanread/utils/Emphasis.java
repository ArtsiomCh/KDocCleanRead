package com.github.artsiomch.kdoccleanread.utils;

import com.intellij.openapi.util.TextRange;

public class Emphasis {

  public TextRange start, end;

  public Emphasis(TextRange start, TextRange end){
    super();
    this.start = start;
    this.end = end;
  }
}
