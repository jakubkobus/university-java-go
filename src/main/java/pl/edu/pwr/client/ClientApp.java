package pl.edu.pwr.client;

import java.io.IOException;

public class ClientApp {
  public static void main(String[] args) {
    GameView view = new GuiView();

    ClientFacade client = new ClientFacade(view);

    System.out.print("[KLIENT] Laczenie z serwerem...");
    try {
      client.connect("localhost", 8080);
      
      view.startInteraction(client);
      
    } catch(IOException e) {
      System.out.println("\n[BŁĄD] Nie udalo się polaczyc z serwerem");
    }
  }
}