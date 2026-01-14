package pl.edu.pwr.server.commands;

import pl.edu.pwr.server.ClientHandler;

/**
 * Interfejs reprezentujący komendę serwera gry w Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Definiowanie kontraktu dla wszystkich komend serwera</li>
 *   <li>Egzekwowanie operacji na grze na podstawie żądania klienta</li>
 *   <li>Komunikację odpowiedzi z klientem poprzez ClientHandler</li>
 * </ul>
 * 
 * Implementuje wzorzec Command, umożliwiając parametryzowanie żądań i klejenie operacji w kolejkę.
 * Każda komenda dziedziczy ten interfejs i implementuje specificzną logikę.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see ClientHandler
 */
public interface Command {
  
  /**
   * Egzekwuje komendę z podanymi argumentami.
   * 
   * Metoda jest wywoływana przez serwer w odpowiedzi na żądanie klienta.
   * Interpretuje argumenty, wykonuje operację na grze i wysyła odpowiedź do klienta.
   * 
   * @param args tablica argumentów komendy (np. współrzędne dla ruchu)
   * @param sender ClientHandler reprezentujący klienta, który wysłał komendę
   */
  void execute(String[] args, ClientHandler sender);
}