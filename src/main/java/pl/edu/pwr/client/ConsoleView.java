package pl.edu.pwr.client;

public class ConsoleView {
  private static final String CLEAR_CONSOLE = "\033[H\033[2J";

  public void displayMessage(String message) {
    System.out.println(message);
  }

  public void clearScreen() {
    System.out.print(CLEAR_CONSOLE);
    System.out.flush();
    System.out.println("--- GRA GO ---");
  }

  public void displayPrompt() {
    System.out.print("> ");
  }
}