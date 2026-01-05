package pl.edu.pwr.server;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.commands.Command;
import pl.edu.pwr.server.commands.MoveCommand;
import pl.edu.pwr.server.commands.PassCommand;
import pl.edu.pwr.server.commands.SurrenderCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private int playerId;
  private ClientHandler opponent;
  private Stone myColor;
  private Game game;

  private Map<String, Command> commands = new HashMap<>();

  public ClientHandler(Socket socket, int playerId, Game game) {
    this.socket = socket;
    this.playerId = playerId;
    this.game = game;
    this.myColor = (playerId == 1) ? Stone.BLACK : Stone.WHITE;

    commands.put("MOVE", new MoveCommand(game));
    commands.put("PASS", new PassCommand(game));
    commands.put("SURRENDER", new SurrenderCommand(game));
  }

  public void setOpponent(ClientHandler opponent) {
    this.opponent = opponent;
  }

  public ClientHandler getOpponent() {
    return opponent;
  }

  public Stone getMyColor() {
    return myColor;
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

        String[] parts = inputLine.split(" ");
        String commandName = parts[0].toUpperCase();

        if(commands.containsKey(commandName)) {
          commands.get(commandName).execute(parts, this);
        } else {
          sendMessage("ERR Nieznana komenda. Dostepne: MOVE x y, PASS, SURRENDER");
        }
      }
    } catch (IOException e) {
      System.out.println("[Gracz " + playerId + "] rozlaczyl sie");
    } finally {
      try {
        socket.close();
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void sendBoard() {
    out.println("CLS");
    out.println(game.getBoard().toString());

    if(game.getCurrentPlayer() == myColor) {
      out.println("--- TWOJA TURA (" + (myColor == Stone.BLACK ? "CZARNY" : "BIALY") + ") ---");
      out.println("Wpisz ruch (np. MOVE 1 1) >");
    } else {
      out.println("--- TURA PRZECIWNIKA ---");
      out.println("Czekaj...");
    }
  }

  public void sendMessage(String message) {
    out.println(message);
  }
}