package pl.edu.pwr.client;

import java.util.Scanner;

/**
 * Implementacja interfejsu GameView dla konsoli tekstowej.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Wyświetlanie komunikatów gry w konsoli</li>
 *   <li>Czyszczenie ekranu konsoli</li>
 *   <li>Odczytywanie poleceń od gracza z klawiatury</li>
 *   <li>Komunikację z fasadą klienta</li>
 * </ul>
 * 
 * Obsługuje polecenia:
 * <ul>
 *   <li>MOVE x y - umieszczenie kamienia</li>
 *   <li>PASS - spasowanie tury</li>
 *   <li>SURRENDER - poddanie się</li>
 *   <li>QUIT - wyjście z gry</li>
 * </ul>
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see GameView
 * @see ClientFacade
 */
public class ConsoleView implements GameView {
  
  /** Sekwencja ANSI do czyszczenia ekranu konsoli */
  private static final String CLEAR_CONSOLE = "\033[H\033[2J";

  /**
   * Wyświetla wiadomość w konsoli.
   * 
   * @param message wiadomość do wyświetlenia
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Czyści ekran konsoli i wyświetla nagłówek gry.
   * 
   * Używa sekwencji ANSI do czyszczenia terminala.
   * Po wyczyszczeniu wyświetla dekoracyjny nagłówek.
   */
  @Override
  public void clearScreen() {
    System.out.print(CLEAR_CONSOLE);
    System.out.flush();
    System.out.println("------------------------- GRA W GO ------------------------");
  }

  /**
   * Wyświetla prompt czekający na wejście użytkownika.
   * 
   * Prompt jest wyświetlony bez przejścia do nowej linii,
   * aby pozwolić użytkownikowi na wpisanie komendy.
   */
  public void displayPrompt() {
    System.out.print("> ");
  }

  /**
   * Rozpoczyna interakcję z graczem w konsoli.
   * 
   * Przepływ:
   * <ol>
   *   <li>Wyświetla instrukcje dostępnych poleceń</li>
   *   <li>Wchodzi w pętlę odbierania komend od gracza</li>
   *   <li>Dla każdej komendy wyświetla prompt i czyta wejście</li>
   *   <li>Polecenie "QUIT" powoduje wyjście z gry</li>
   *   <li>Pozostałe komendy są wysyłane do serwera</li>
   *   <li>Po wyjściu rozłącza się z serwerem</li>
   * </ol>
   * 
   * @param client fasada klienta do wysyłania komend do serwera
   */
  @Override
  public void startInteraction(ClientFacade client) {
    // Utworzenie skanera do odczytywania wejścia z klawiatury
    Scanner keyboard = new Scanner(System.in);
    System.out.println("[KLIENT KONSOLOWY] Wpisz komendę (np. MOVE x y, PASS, QUIT)");

    try {
      // Pętla odbierania komend od gracza
      while(keyboard.hasNextLine()) {
        displayPrompt();
        String input = keyboard.nextLine();

        // Sprawdzenie warunku wyjścia z gry
        if(input.equalsIgnoreCase("quit"))
          break;

        // Wysłanie komendy do serwera
        client.sendMessage(input);
      }
    } finally {
      // Czyszczenie zasobów - rozłączenie i zamknięcie skanera
      client.disconnect();
      keyboard.close();
    }
  }
}