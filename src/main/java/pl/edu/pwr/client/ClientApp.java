package pl.edu.pwr.client;

import java.io.IOException;
import java.util.Scanner;

public class ClientApp {
  public static void main(String[] args) {
    ClientFacade client = new ClientFacade();
    Scanner keyboard = new Scanner(System.in);

    System.out.print("[KLIENT] Laczenie z serwerem...");

    try {
      client.connect("localhost", 8080);
      
      while(keyboard.hasNextLine()) {
        String input = keyboard.nextLine();

        if(input.equalsIgnoreCase("quit"))
          break;

        client.sendMessage(input);
        
        System.out.print("> ");
      }

    } catch(IOException e) {
      System.out.println("\n[BŁĄD] Nie udalo się polaczyc z serwerem");
    } finally {
      client.disconnect();
      System.out.println("[KLIENT] Klient zakonczyl dzialanie");
      keyboard.close();
    }
  }
}