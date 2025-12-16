package pl.edu.pwr.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// Facade
public class ClientFacade {
  private Socket socket;
  private Scanner in;
  private PrintWriter out;
  private Thread listenerThread;

  private static final String CLEAR_CONSOLE = "\033[H\033[2J";

  public void connect(String host, int port) throws IOException {
    socket = new Socket(host, port);
    in = new Scanner(socket.getInputStream());
    out = new PrintWriter(socket.getOutputStream(), true);

    listenerThread = new Thread(() -> {
      try {
        while(in.hasNextLine()) {
          String message = in.nextLine();

          if(message.equals("CLS")) {
            clearScreen();
            continue;
          }

          System.out.println(message);          
        }
      } catch(Exception e) {
        System.out.println("\n[KLIENT] Utracono polaczenie z serwerem");
      }
    });
    listenerThread.start();
  }

  private void clearScreen() {
    System.out.print(CLEAR_CONSOLE);
    System.out.flush();
    System.out.println("--- GRA GO ---");
  }

  public void sendMessage(String message) {
    if(out != null)
      out.println(message);
  }

  public void disconnect() {
    try {
      if(socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  public void sendMove(int x, int y) {
      sendMessage("MOVE " + x + " " + y);
  }
}