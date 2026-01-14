package pl.edu.pwr.client;

/**
 * Interfejs definiujący kontrakt dla widoków gry w Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Wyświetlanie komunikatów do użytkownika</li>
 *   <li>Czyszczenie interfejsu użytkownika</li>
 *   <li>Zarządzanie interakcją z graczem</li>
 * </ul>
 * 
 * Implementacje tego interfejsu mogą udostępniać różne warianty interfejsu użytkownika,
 * takie jak: konsola tekstowa (ConsoleView) lub interfejs graficzny (GuiView).
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see ConsoleView
 * @see GuiView
 * @see ClientFacade
 */
public interface GameView {
  
  /**
   * Wyświetla wiadomość do użytkownika.
   * 
   * Metoda odpowiada za wyświetlenie komunikatów wysłanych przez serwer
   * w odpowiednim formacie dla danego typu widoku.
   * 
   * @param message wiadomość do wyświetlenia
   */
  void displayMessage(String message);
  
  /**
   * Czyści interfejs użytkownika.
   * 
   * Metoda usuwa poprzednią zawartość ekranu i przygotowuje go
   * do wyświetlenia nowego stanu gry (np. nowa pozycja na planszy).
   */
  void clearScreen();

  /**
   * Rozpoczyna interakcję z graczem poprzez interfejs użytkownika.
   * 
   * Metoda powinna:
   * <ol>
   *   <li>Wyświetlić instrukcje dostępnych poleceń</li>
   *   <li>Oczekiwać na wejście od gracza</li>
   *   <li>Wysyłać polecenia przez fasadę klienta do serwera</li>
   *   <li>Obsługiwać koniec sesji (np. komenda QUIT)</li>
   * </ol>
   * 
   * @param clientFacade fasada klienta do komunikacji z serwerem
   * @see ClientFacade
   */
  void startInteraction(ClientFacade clientFacade);
}