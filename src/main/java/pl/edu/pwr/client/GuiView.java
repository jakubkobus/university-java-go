package pl.edu.pwr.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import pl.edu.pwr.logic.Stone;

public class GuiView implements GameView {

  private static ClientFacade clientRef;
  private static GuiView instance;
  private AppWindow currentWindow;

  @Override
  public void startInteraction(ClientFacade client) {
    clientRef = client;
    instance = this;
    new Thread(() -> Application.launch(AppWindow.class)).start();
  }

  @Override
  public void displayMessage(String message) {
    if (currentWindow != null) {
      Platform.runLater(() -> currentWindow.appendLog(message));
    }
  }

  @Override
  public void clearScreen() {
    if (currentWindow != null) {
      Platform.runLater(() -> currentWindow.resetBoardBuffer());
    }
  }

  public static class AppWindow extends Application {
    private static final int BOARD_SIZE = 19;
    private static final int CELL_SIZE = 40;
    private static final double STONE_SIZE = 18.0;

    private TextArea logArea;
    private StackPane[][] cells = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private boolean isReadingBoard = false;
    private StringBuilder boardBuffer = new StringBuilder();

    private boolean isCleanupPhase = false;

    private VBox cleanupPanel;
    private ToggleGroup cleanupGroup;
    private RadioButton rbRemove;
    private RadioButton rbFill;

    @Override
    public void start(Stage stage) {
      if (GuiView.instance != null) {
        GuiView.instance.currentWindow = this;
      }

      BorderPane root = new BorderPane();
      root.setStyle("-fx-background-color: #333333;");

      StackPane boardContainer = new StackPane();
      boardContainer.setPadding(new Insets(20));
      boardContainer.setStyle(
        "-fx-background-color: #e3c476; " +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 20, 0, 0, 0);"
      );

      GridPane boardGrid = createBoard();
      boardContainer.getChildren().add(boardGrid);

      VBox centerBox = new VBox(boardContainer);
      centerBox.setAlignment(Pos.CENTER);
      root.setCenter(centerBox);

      VBox sidePanel = new VBox(10);
      sidePanel.setPadding(new Insets(15));
      sidePanel.setPrefWidth(300);
      sidePanel.setStyle("-fx-background-color: #444444;");

      Label titleLabel = new Label("Gra w Go");
      titleLabel.setTextFill(Color.WHITE);
      titleLabel.setFont(new Font("Arial", 24));

      logArea = new TextArea();
      logArea.setEditable(false);
      logArea.setWrapText(true);
      logArea.setPrefHeight(400);
      logArea.setStyle(
          "-fx-control-inner-background: #222222; -fx-text-fill: #00ff00; -fx-font-family: 'Consolas', monospace;");

      HBox actionButtons = new HBox(10);
      actionButtons.setAlignment(Pos.CENTER);

      Button passBtn = createStyledButton("PASS", "#d35400");
      passBtn.setOnAction(e -> clientRef.sendMessage("PASS"));

      Button surrenderBtn = createStyledButton("SURRENDER", "#c0392b");
      surrenderBtn.setOnAction(e -> clientRef.sendMessage("SURRENDER"));

      actionButtons.getChildren().addAll(passBtn, surrenderBtn);

      cleanupPanel = new VBox(10);
      cleanupPanel.setStyle(
          "-fx-padding: 10; -fx-border-color: #f1c40f; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #555;");
      cleanupPanel.setVisible(false);

      Label cleanupLabel = new Label("Faza Cleanup:");
      cleanupLabel.setTextFill(Color.GOLD);
      cleanupLabel.setFont(Font.font("Arial", 16));

      cleanupGroup = new ToggleGroup();
      rbRemove = new RadioButton("Usuń martwy kamień (REMOVE)");
      rbRemove.setTextFill(Color.WHITE);
      rbRemove.setToggleGroup(cleanupGroup);
      rbRemove.setSelected(true);

      rbFill = new RadioButton("Postaw jeńca (FILL)");
      rbFill.setTextFill(Color.WHITE);
      rbFill.setToggleGroup(cleanupGroup);

      cleanupPanel.getChildren().addAll(cleanupLabel, rbRemove, rbFill);

      sidePanel.getChildren().addAll(titleLabel, logArea, actionButtons, cleanupPanel);
      root.setRight(sidePanel);

      Scene scene = new Scene(root, 1100, 850);
      stage.setTitle("Go Game Client - JavaFX");
      stage.setOnCloseRequest(e -> {
        clientRef.disconnect();
        Platform.exit();
        System.exit(0);
      });
      stage.setScene(scene);
      stage.show();
    }

    private Button createStyledButton(String text, String colorHex) {
      Button btn = new Button(text);
      btn.setStyle(
          "-fx-background-color: " + colorHex + "; " +
              "-fx-text-fill: white; " +
              "-fx-font-weight: bold; " +
              "-fx-cursor: hand;");
      btn.setPrefWidth(120);
      return btn;
    }

    private GridPane createBoard() {
      GridPane grid = new GridPane();
      grid.setAlignment(Pos.CENTER);
      grid.setHgap(0);
      grid.setVgap(0);

      for (int y = 0; y < BOARD_SIZE; y++) {
        for (int x = 0; x < BOARD_SIZE; x++) {
          StackPane cell = new StackPane();
          cell.setPrefSize(CELL_SIZE, CELL_SIZE);

          Line hLine = new Line();
          hLine.setStroke(Color.BLACK);
          hLine.setStrokeWidth(1.0);
          double startX = (x == 0) ? CELL_SIZE / 2.0 : 0;
          double endX = (x == BOARD_SIZE - 1) ? CELL_SIZE / 2.0 : CELL_SIZE;
          hLine.setStartX(startX);
          hLine.setEndX(endX);
          hLine.setStartY(CELL_SIZE / 2.0);
          hLine.setEndY(CELL_SIZE / 2.0);

          Line vLine = new Line();
          vLine.setStroke(Color.BLACK);
          vLine.setStrokeWidth(1.0);
          double startY = (y == 0) ? CELL_SIZE / 2.0 : 0;
          double endY = (y == BOARD_SIZE - 1) ? CELL_SIZE / 2.0 : CELL_SIZE;
          vLine.setStartX(CELL_SIZE / 2.0);
          vLine.setEndX(CELL_SIZE / 2.0);
          vLine.setStartY(startY);
          vLine.setEndY(endY);

          Pane linesPane = new Pane(hLine, vLine);
          cell.getChildren().add(linesPane);

          if (isHoshi(x, y)) {
            Circle hoshi = new Circle(4.0, Color.BLACK);
            cell.getChildren().add(hoshi);
          }

          Rectangle clickArea = new Rectangle(CELL_SIZE, CELL_SIZE, Color.TRANSPARENT);
          cell.getChildren().add(clickArea);

          int finalX = x;
          int finalY = y;

          cell.setOnMouseClicked(e -> handleBoardClick(finalX, finalY));

          cells[x][y] = cell;
          grid.add(cell, x, y);
        }
      }
      return grid;
    }

    private void handleBoardClick(int x, int y) {
      if (!isCleanupPhase) {
        clientRef.sendMove(x, y);
      } else {
        if (rbRemove.isSelected()) {
          clientRef.sendMessage("REMOVE " + (x + 1) + " " + (y + 1));
        } else if (rbFill.isSelected()) {
          clientRef.sendMessage("FILL " + (x + 1) + " " + (y + 1));
        }
      }
    }

    private boolean isHoshi(int x, int y) {
      boolean xMatch = (x == 3 || x == 9 || x == 15);
      boolean yMatch = (y == 3 || y == 9 || y == 15);
      return xMatch && yMatch;
    }

    public void resetBoardBuffer() {
      isReadingBoard = true;
      boardBuffer.setLength(0);
    }

    public void appendLog(String msg) {
      checkGameState(msg);

      if (isReadingBoard) {
        if (msg.startsWith("===") || msg.startsWith("INFO") || msg.startsWith("ERR") ||
            msg.startsWith("GAME_OVER") || msg.startsWith("Wpisz:") || msg.startsWith("---")) {

          isReadingBoard = false;
          parseAndDrawBoard(boardBuffer.toString());
          logArea.appendText(msg + "\n");
        } else {
          boardBuffer.append(msg).append("\n");
        }
      } else {
        logArea.appendText(msg + "\n");
      }
    }

    private void checkGameState(String msg) {
      if (msg.contains("=== FAZA USUWANIA MARTWYCH KAMIENI ===")) {
        isCleanupPhase = true;
        cleanupPanel.setVisible(true);
      } else if (msg.contains("=== GRA ZAKONCZONA ===")) {
        isCleanupPhase = false;
        cleanupPanel.setVisible(false);
      }
    }

    private void parseAndDrawBoard(String fullBoardStr) {
      String[] lines = fullBoardStr.split("\n");
      int y = 0;
      for (String line : lines) {
        if (line.contains("(B)") || line.contains("(W)") || line.contains("+")) {
          drawRow(y, line);
          y++;
          if (y >= BOARD_SIZE)
            break;
        }
      }
    }

    private void drawRow(int y, String line) {
      int x = 0;

      for (int i = 0; i < line.length(); i++) {
        if (x >= BOARD_SIZE)
          break;
        char c = line.charAt(i);

        if (c == '(') {
          char colorChar = line.charAt(i + 1);
          setStone(x, y, colorChar == 'B' ? Stone.BLACK : Stone.WHITE);
          x++;
          i += 2;
        } else if (c == '+') {
          setStone(x, y, Stone.NONE);
          x++;
        }
      }
    }

    private void setStone(int x, int y, Stone color) {
      StackPane cell = cells[x][y];

      cell.getChildren().removeIf(node -> node instanceof Circle && ((Circle) node).getRadius() > 5.0);

      if (color != Stone.NONE) {
        Circle stone = new Circle(STONE_SIZE);

        stone.setEffect(new DropShadow(5.0, 3.0, 3.0, Color.color(0, 0, 0, 0.4)));

        if (color == Stone.BLACK) {
          stone.setFill(Color.BLACK);
          stone.setStroke(Color.BLACK);
        } else {
          stone.setFill(Color.WHITE);
          stone.setStroke(Color.GRAY);
        }

        cell.getChildren().add(stone);
      }
    }
  }
}