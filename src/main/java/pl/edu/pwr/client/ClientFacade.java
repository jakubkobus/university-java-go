package pl.edu.pwr.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Fasada klienta do komunikacji z serwerem gry w Go.
 * Zarządza połączeniem sieciowym, wysyłaniem i odbieraniem wiadomości.
 * Implementuje wzorzec Facade, ukrywając szczegóły komunikacji sieciowej.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public class ClientFacade {
  
  /** Socket do komunikacji z serwerem */
  private Socket socket;
  
  /** Skaner do odczytywania danych ze strumienia wejściowego */
  private Scanner in;
  
  /** Pisarz do wysyłania danych do strumienia wyjściowego */
  private PrintWriter out;
  
  /** Wątek nasłuchujący na wiadomości z serwera */
  private Thread listenerThread;

  /** Widok gry do wyświetlania komunikatów */
  private final GameView view;

  /**
   * Konstruktor ClientFacade.
   * Inicjalizuje fasadę klienta z określonym widokiem gry.
   * 
   * @param view widok gry, który będzie używany do wyświetlania komunikatów
   * @throws NullPointerException jeśli parametr view jest null
   */
  public ClientFacade(GameView view) {
    this.view = view;
  }

  /**
   * Nawiązuje połączenie z serwerem gry.
   * Otwiera socket do komunikacji i uruchamia wątek nasłuchujący na wiadomości.
   * Wątek nasłuchujący odbiera wiadomości i wyświetla je za pośrednictwem widoku gry.
   * Obsługuje specjalną komendę "CLS" do czyszczenia ekranu.
   * 
   * @param host adres hosta serwera (np. "localhost")
   * @param port numer portu, na którym nasłuchuje serwer (np. 8080)
   * @throws IOException jeśli połączenie z serwerem nie powiedzie się
   */
  public void connect(String host, int port) throws IOException {
    socket = new Socket(host, port);
    in = new Scanner(socket.getInputStream());
    out = new PrintWriter(socket.getOutputStream(), true);

    listenerThread = new Thread(() -> {
      try {
        while (in.hasNextLine()) {
          String message = in.nextLine();

          if (message.equals("CLS")) {
            view.clearScreen();
            continue;
          }

          view.displayMessage(message);
        }
      } catch (Exception e) {
        view.displayMessage("[KLIENT] Utracono polaczenie z serwerem");
      }
    });
    listenerThread.start();
  }

  /**
   * Wysyła wiadomość tekstową do serwera.
   * Wiadomość jest wysyłana jedynie jeśli pisarz wyjściowy istnieje.
   * 
   * @param message treść wiadomości do wysłania
   */
  public void sendMessage(String message) {
    if (out != null)
      out.println(message);
  }

  /**
   * Wysyła ruch (umieszczenie kamienia) do serwera.
   * Współrzędne są konwertowane z systemu 0-indeksowanego na 1-indeksowany.
   * Ruch jest wysyłany w formacie: "MOVE x y"
   * 
   * @param x współrzędna x kamienia (0-indeksowana)
   * @param y współrzędna y kamienia (0-indeksowana)
   */
  public void sendMove(int x, int y) {
    sendMessage("MOVE " + (x + 1) + " " + (y + 1));
  }

  /**
   * Zamyka połączenie z serwerem.
   * Zamyka socket i zatrzymuje wątek nasłuchujący.
   * Błędy połączenia są wypisywane na standardowe wyjście błędu.
   */
  public void disconnect() {
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}