package pl.edu.pwr.server;

public class ServerApp {
  public static void main(String[] args) {
    Server.getInstance().start(8080);
  }
}