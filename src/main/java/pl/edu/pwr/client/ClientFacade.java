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

  public void connect(String host, int port) throws IOException {
    socket = new Socket(host, port);
    in = new Scanner(socket.getInputStream());
    out = new PrintWriter(socket.getOutputStream(), true);

    listenerThread = new Thread(() -> {
      try {
        while(in.hasNextLine()) {
          String message = in.nextLine();
          System.out.print("\r" + message + "\n> ");
        }
      } catch(Exception e) {
        System.out.println("\n[KLIENT] Utracono polaczenie z serwerem");
      }
    });
    listenerThread.start();
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
}