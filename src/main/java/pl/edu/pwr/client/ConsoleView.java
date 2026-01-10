package pl.edu.pwr.client;

import java.util.Scanner;

public class ConsoleView implements GameView {
  private static final String CLEAR_CONSOLE = "\033[H\033[2J";

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void clearScreen() {
    System.out.print(CLEAR_CONSOLE);
    System.out.flush();
    System.out.println("------------------------- GRA W GO ------------------------");
  }

  public void displayPrompt() {
    System.out.print("> ");
  }

  @Override
  public void startInteraction(ClientFacade client) {
    Scanner keyboard = new Scanner(System.in);
    System.out.println("[KLIENT KONSOLOWY] Wpisz komendę (np. MOVE x y, PASS, QUIT)");

    try {
      while(keyboard.hasNextLine()) {
        displayPrompt();
        String input = keyboard.nextLine();

        if(input.equalsIgnoreCase("quit"))
          break;

        client.sendMessage(input);
      }
    } finally {
      client.disconnect();
      keyboard.close();
    }
  }
}