package net.nixill.dicebot;

public class WrappedException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public WrappedException(Throwable inner) {
    super(inner);
  }
}