package pl.edu.pwr.server;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.Stone;

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
  private Stone myColor;
  private Game game;

  public ClientHandler(Socket socket, int playerId, Game game) {
    this.socket = socket;
    this.playerId = playerId;
    this.game = game;
    this.myColor = (playerId == 1) ? Stone.BLACK : Stone.WHITE;
  }

  public void setOpponent(ClientHandler opponent) {
    this.opponent = opponent;
  }

  @Override
  public void run() {
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      sendBoard();
      out.println("Jestes graczem: " + (playerId == 1 ? "CZARNYM (B)" : "BIALYM (W)"));

      String inputLine;
      while((inputLine = in.readLine()) != null) {
        System.out.println("[Gracz " + playerId + "] " + inputLine);

        if(inputLine.startsWith("MOVE")) {
          handleMove(inputLine);
          continue;
        }

        if(opponent != null) {
          opponent.sendMessage("CHAT: " + inputLine);
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

  private void handleMove(String input) {
    try {
      String[] parts = input.split(" ");
      if(parts.length != 3) {
        sendMessage("ERR Zly format. Uzyj: MOVE x y");
        return;
      }

      int x = Integer.parseInt(parts[1]) - 1;
      int y = Integer.parseInt(parts[2]) - 1;

      if(game.getCurrentPlayer() != myColor) {
        sendMessage("ERR To nie Twoja tura!");
        return;
      }

      boolean ok;
      synchronized (game) {
        ok = game.makeMove(x, y);
      }

      if(!ok) {
        sendMessage("ERR Ruch niedozwolony (zajete lub poza plansza)");
        return;
      }

      sendBoard();
      if(opponent != null) {
        opponent.sendBoard();
        opponent.sendMessage("INFO: Przeciwnik wykonal ruch: " + (x + 1) + " " + (y + 1));
      }
      sendMessage("INFO: Wykonano ruch: " + (x + 1) + " " + (y + 1));

    } catch(NumberFormatException e) {
      sendMessage("ERR Wspolrzedne musza byc liczbami");
    }
  }

  public void sendBoard() {
    out.println("CLS");
    out.println(game.getBoard().toString());
  }

  public void sendMessage(String message) {
    out.println(message);
  }
}