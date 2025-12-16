package pl.edu.pwr.server.commands;

import pl.edu.pwr.server.ClientHandler;

public interface Command {
  void execute(String[] args, ClientHandler sender);
}