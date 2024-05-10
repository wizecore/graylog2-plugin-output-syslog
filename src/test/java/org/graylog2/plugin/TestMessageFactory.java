package org.graylog2.plugin;

public class TestMessageFactory {
  public static MessageFactory create() {
    return new DefaultMessageFactory();
  }
}