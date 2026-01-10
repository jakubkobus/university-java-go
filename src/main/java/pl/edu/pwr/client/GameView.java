package pl.edu.pwr.client;

public interface GameView {
    void displayMessage(String message);
    void clearScreen();

    void startInteraction(ClientFacade clientFacade);
}