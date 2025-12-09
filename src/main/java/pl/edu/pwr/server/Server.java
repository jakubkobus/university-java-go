package pl.edu.pwr.server;

import pl.edu.pwr.logic.Game;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  private static Server instance;
  private volatile boolean isRunning = true;

  private Server() {
  }

  public static Server getInstance() {
    if (instance == null)
      synchronized (Server.class) {
        if (instance == null)
          instance = new Server();
      }
    return instance;
  }

  public void start(int port) {
    System.out.println("Serwer uruchamia sie na porcie " + port);
    try(ServerSocket listener = new ServerSocket(port)) {

      while(isRunning) {
        Socket player1 = listener.accept();
        System.out.println("[Gracz 1] dolaczyl");
        
        PrintWriter tempOut1 = new PrintWriter(player1.getOutputStream(), true);
        tempOut1.println("[SERWER] Polaczono jako Gracz 1 (CZARNY). Czekanie na przeciwnika...");

        Socket player2 = listener.accept();
        System.out.println("[Gracz 2] dolaczyl");
        PrintWriter tempOut2 = new PrintWriter(player2.getOutputStream(), true);
        tempOut2.println("[SERWER] Polaczono jako Gracz 2 (BIALY). Gra sie rozpoczyna");
        
        tempOut1.println("[SERWER] Przeciwnik dolaczyl. Gra sie rozpoczyna!");

        Game game = new Game(19);

        ClientHandler handler1 = new ClientHandler(player1, 1);
        ClientHandler handler2 = new ClientHandler(player2, 2);

        handler1.setOpponent(handler2);
        handler2.setOpponent(handler1);

        new Thread(handler1).start();
        new Thread(handler2).start();
      }

    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}