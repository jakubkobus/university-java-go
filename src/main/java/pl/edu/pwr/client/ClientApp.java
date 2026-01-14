package pl.edu.pwr.client;

import java.io.IOException;

/**
 * Punkt wejścia aplikacji klienta gry w Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Inicjalizację widoku gry (interfejs graficzny)</li>
 *   <li>Utworzenie i konfigurację fasady klienta</li>
 *   <li>Nawiązanie połączenia z serwerem</li>
 *   <li>Uruchomienie interakcji z graczem</li>
 * </ul>
 * 
 * Używa GUI (GuiView) do wyświetlania stanu gry i komunikacji z użytkownikiem.
 * Serwer musi być dostępny na localhost:8080.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see ClientFacade
 * @see GameView
 * @see GuiView
 */
public class ClientApp {
  
  /**
   * Metoda główna aplikacji klienckiej.
   * 
   * Przepływ:
   * <ol>
   *   <li>Tworzy GUI do interakcji z graczem</li>
   *   <li>Inicjalizuje fasadę klienta z widokiem gry</li>
   *   <li>Próbuje nawiązać połączenie z serwerem na localhost:8080</li>
   *   <li>Jeśli połączenie się powiedzie, uruchamia interfejs interakcji</li>
   *   <li>W przypadku błędu wyświetla komunikat błędu</li>
   * </ol>
   * 
   * @param args argumenty wiersza poleceń (nieużywane)
   */
  public static void main(String[] args) {
    // Inicjalizacja interfejsu graficznego (GUI)
    GameView view = new GuiView();

    // Utworzenie fasady klienta do komunikacji z serwerem
    ClientFacade client = new ClientFacade(view);

    System.out.print("[KLIENT] Laczenie z serwerem...");
    try {
      // Nawiązanie połączenia z serwerem na localhost, port 8080
      client.connect("localhost", 8080);
      
      // Uruchomienie interfejsu interakcji z graczem
      view.startInteraction(client);
      
    } catch(IOException e) {
      // Obsługa błędu połączenia
      System.out.println("\n[BŁĄD] Nie udalo się polaczyc z serwerem");
    }
  }
}