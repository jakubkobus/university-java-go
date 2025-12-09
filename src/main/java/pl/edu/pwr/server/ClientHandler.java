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

  private Game game;

  public ClientHandler(Socket socket, int playerId) {
    this.socket = socket;
    this.playerId = playerId;
    this.game = game;
  }

  public void setOpponent(ClientHandler opponent) {
    this.opponent = opponent;
  }

  @Override
  public void run() {
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      out.println("CONNECTED AS " + (playerId == 1 ? "BLACK" : "WHITE"));

      String inputLine;
      while((inputLine = in.readLine()) != null) {
        System.out.println("[Gracz " + playerId + "] " + inputLine);

        out.println("[SERWER] Otrzymano '" + inputLine + "'");

        if (inputLine.startsWith("MOVE")) {
            handleMove(inputLine);
            continue;
        }

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

  private void handleMove(String input) {
      try {
          String[] parts = input.split(" ");
          if (parts.length != 3) {
              sendMessage("ERR Wrong MOVE format. Use: MOVE x y");
              return;
          }

          int x = Integer.parseInt(parts[1]);
          int y = Integer.parseInt(parts[2]);

          Stone myColor = (playerId == 1) ? Stone.BLACK : Stone.WHITE;

          if (game.getCurrentPlayer() != myColor) {
              sendMessage("ERR Not your turn");
              return;
          }

          boolean ok;
          synchronized (game) {
              ok = game.makeMove(x, y);
          }

          if (!ok) {
              sendMessage("ERR Invalid move");
              return;
          }

          sendMessage("OK MOVE " + x + " " + y);
          if (opponent != null) {
              opponent.sendMessage("OPPONENT_MOVE " + x + " " + y);
          }

      } catch (NumberFormatException e) {
          sendMessage("ERR MOVE parameters must be numbers");
      }
  }

  public void sendMessage(String message) {
    out.println(message);
  }
}