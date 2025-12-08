package pl.edu.pwr.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private int playerId;
  private ClientHandler opponent;

  public ClientHandler(Socket socket, int playerId) {
    this.socket = socket;
    this.playerId = playerId;
  }

  public void setOpponent(ClientHandler opponent) {
    this.opponent = opponent;
  }

  @Override
  public void run() {
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      String inputLine;
      while((inputLine = in.readLine()) != null) {
        System.out.println("[Gracz " + playerId + "] " + inputLine);

        out.println("[SERWER] Otrzymano '" + inputLine + "'");

        if(opponent != null) {
          opponent.sendMessage("[PRZECIWNIK] Otrzymano '" + inputLine + "'");
        }
      }
    } catch(IOException e) {
      System.out.println("[Gracz " + playerId + "] rozlaczyl sie");
    } finally {
      try {
        socket.close();
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void sendMessage(String message) {
    out.println(message);
  }
}