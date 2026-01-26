package pl.edu.pwr.viewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import pl.edu.pwr.database.entities.GameEntity;
import pl.edu.pwr.database.entities.MoveEntity;
import pl.edu.pwr.logic.Stone;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Okno JavaFX do przeglądania zapisanych gier.
 * 
 * Funkcjonalności:
 * <ul>
 * <li>Lista gier pokazująca wszystkie gry z bazy danych</li>
 * <li>Odtwarzanie planszy z nawigacją po ruchach</li>
 * <li>Kontrolki do przewijania ruchów do przodu/tyłu</li>
 * <li>Wyświetlanie informacji o grze</li>
 * </ul>
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public class GameViewerWindow extends Application {

  private static final int BOARD_SIZE = 19;
  private static final int CELL_SIZE = 30;
  private static final double STONE_SIZE = 14.0;

  private GameViewerService viewerService;

  private ListView<GameEntity> gameListView;
  private StackPane[][] cells = new StackPane[BOARD_SIZE][BOARD_SIZE];
  private Label gameInfoLabel;
  private Label moveInfoLabel;
  private Button btnPrevMove;
  private Button btnNextMove;
  private Button btnFirst;
  private Button btnLast;
  private Slider moveSlider;

  private List<MoveEntity> currentMoves;
  private int currentMoveIndex = 0;

  @Override
  public void start(Stage stage) {
    viewerService = GameViewerApp.getSpringContext().getBean(GameViewerService.class);

    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");

    HBox titleBar = new HBox();
    titleBar.setPadding(new Insets(15, 20, 15, 20));
    titleBar.setAlignment(Pos.CENTER);
    titleBar.setStyle("-fx-background-color: linear-gradient(to bottom, #1a252f, #2c3e50);");

    Label titleLabel = new Label("Odtwarzacz gier");
    titleLabel.setTextFill(Color.web("#ecf0f1"));
    titleLabel.setFont(Font.font("Arial", 28));
    titleLabel.setStyle("-fx-font-weight: bold;");

    titleBar.getChildren().add(titleLabel);
    root.setTop(titleBar);

    VBox leftPanel = createGameListPanel();
    root.setLeft(leftPanel);

    VBox centerPanel = createBoardPanel();
    root.setCenter(centerPanel);

    Scene scene = new Scene(root, 1400, 900);
    stage.setTitle("Odtwarzacz gier");
    stage.setOnCloseRequest(e -> {
      Platform.exit();
      System.exit(0);
    });
    stage.setScene(scene);
    stage.show();

    loadGameList();
  }

  /**
   * Tworzy lewy panel z listą gier.
   * 
   * @return VBox zawierający listę gier i przycisk odświeżania
   */
  private VBox createGameListPanel() {
    VBox panel = new VBox(10);
    panel.setPadding(new Insets(20));
    panel.setPrefWidth(350);
    panel.setStyle("-fx-background-color: rgba(52, 73, 94, 0.9);");

    Label listLabel = new Label("Lista gier w bazie");
    listLabel.setTextFill(Color.web("#ecf0f1"));
    listLabel.setFont(Font.font("Arial", 18));
    listLabel.setStyle("-fx-font-weight: bold;");

    gameListView = new ListView<>();
    gameListView.setPrefHeight(700);
    gameListView.setStyle(
        "-fx-control-inner-background: #1a1a1a; " +
            "-fx-text-fill: #ecf0f1; " +
            "-fx-font-size: 13px;");

    gameListView.setCellFactory(lv -> new ListCell<GameEntity>() {
      @Override
      protected void updateItem(GameEntity game, boolean empty) {
        super.updateItem(game, empty);
        if (empty || game == null) {
          setText(null);
          setStyle("");
        } else {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
          String text = String.format("Gra #%d\n%s\nCzarny: %s vs Biały: %s\nWynik: %s",
              game.getId(),
              game.getDate().format(formatter),
              game.getPlayerBlackType(),
              game.getPlayerWhiteType(),
              game.getResult() != null ? game.getResult() : "Nieznany");
          setText(text);
          setStyle("-fx-text-fill: #ecf0f1; -fx-padding: 8;");
        }
      }
    });

    gameListView.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> {
          if (newVal != null) {
            System.out.println("Wybrana gra: #" + newVal.getId());
            loadGame(newVal);
          }
        });

    Button refreshBtn = createStyledButton("Odśwież", "#3498db", "#2980b9");
    refreshBtn.setOnAction(e -> loadGameList());
    refreshBtn.setMaxWidth(Double.MAX_VALUE);

    panel.getChildren().addAll(listLabel, gameListView, refreshBtn);
    return panel;
  }

  /**
   * Tworzy centralny panel z planszą i kontrolkami.
   * 
   * @return VBox zawierający planszę, suwak ruchów i przyciski nawigacji
   */
  private VBox createBoardPanel() {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(20));
    panel.setAlignment(Pos.CENTER);

    gameInfoLabel = new Label("Wybierz grę z listy");
    gameInfoLabel.setTextFill(Color.web("#ecf0f1"));
    gameInfoLabel.setFont(Font.font("Arial", 16));
    gameInfoLabel.setStyle("-fx-font-weight: bold;");

    StackPane boardContainer = new StackPane();
    boardContainer.setPadding(new Insets(15));
    boardContainer.setStyle(
        "-fx-background-color: linear-gradient(to bottom right, #d4a574, #e3c476);" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 25, 0, 0, 5);" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;");

    GridPane boardGrid = createBoard();
    boardContainer.getChildren().add(boardGrid);

    moveInfoLabel = new Label("Ruch: 0 / 0");
    moveInfoLabel.setTextFill(Color.web("#95a5a6"));
    moveInfoLabel.setFont(Font.font("Arial", 14));

    moveSlider = new Slider(0, 0, 0);
    moveSlider.setMaxWidth(600);
    moveSlider.setShowTickLabels(true);
    moveSlider.setShowTickMarks(true);
    moveSlider.setMajorTickUnit(10);
    moveSlider.setMinorTickCount(1);
    moveSlider.setBlockIncrement(1);
    moveSlider.setSnapToTicks(true);
    moveSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (!moveSlider.isValueChanging() || Math.abs(newVal.intValue() - oldVal.intValue()) >= 1) {
        goToMove(newVal.intValue());
      }
    });

    HBox controls = new HBox(10);
    controls.setAlignment(Pos.CENTER);

    btnFirst = createStyledButton("⏮ Pierwszy", "#95a5a6", "#7f8c8d");
    btnFirst.setOnAction(e -> goToMove(0));

    btnPrevMove = createStyledButton("◀ Poprzedni", "#3498db", "#2980b9");
    btnPrevMove.setOnAction(e -> previousMove());

    btnNextMove = createStyledButton("Następny ▶", "#3498db", "#2980b9");
    btnNextMove.setOnAction(e -> nextMove());

    btnLast = createStyledButton("Ostatni ⏭", "#95a5a6", "#7f8c8d");
    btnLast.setOnAction(e -> goToMove(currentMoves != null ? currentMoves.size() : 0));

    controls.getChildren().addAll(btnFirst, btnPrevMove, btnNextMove, btnLast);

    panel.getChildren().addAll(gameInfoLabel, boardContainer, moveInfoLabel, moveSlider, controls);
    return panel;
  }

  /**
   * Tworzy siatkę planszy z liniami i punktami hoshi.
   * 
   * @return GridPane reprezentujący planszę Go
   */
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
        hLine.setStroke(Color.web("#2c2416"));
        hLine.setStrokeWidth(1.0);
        double startX = (x == 0) ? CELL_SIZE / 2.0 : 0;
        double endX = (x == BOARD_SIZE - 1) ? CELL_SIZE / 2.0 : CELL_SIZE;
        hLine.setStartX(startX);
        hLine.setEndX(endX);
        hLine.setStartY(CELL_SIZE / 2.0);
        hLine.setEndY(CELL_SIZE / 2.0);

        Line vLine = new Line();
        vLine.setStroke(Color.web("#2c2416"));
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
          Circle hoshi = new Circle(3.5, Color.web("#2c2416"));
          cell.getChildren().add(hoshi);
        }

        cells[x][y] = cell;
        grid.add(cell, x, y);
      }
    }
    return grid;
  }

  /**
   * Sprawdza czy pozycja jest punktem hoshi (gwiazdka).
   * 
   * @param x współrzędna x
   * @param y współrzędna y
   * @return true jeśli pozycja jest punktem hoshi
   */
  private boolean isHoshi(int x, int y) {
    boolean xMatch = (x == 3 || x == 9 || x == 15);
    boolean yMatch = (y == 3 || y == 9 || y == 15);
    return xMatch && yMatch;
  }

  /**
   * Tworzy stylizowany przycisk z efektami hover.
   * 
   * @param text          tekst przycisku
   * @param colorHex      kolor podstawowy w formacie hex
   * @param hoverColorHex kolor przy najechaniu myszką w formacie hex
   * @return stylizowany przycisk
   */
  private Button createStyledButton(String text, String colorHex, String hoverColorHex) {
    Button btn = new Button(text);
    String baseStyle = "-fx-background-color: " + colorHex + "; " +
        "-fx-text-fill: white; " +
        "-fx-font-weight: bold; " +
        "-fx-font-size: 13px; " +
        "-fx-padding: 8 16; " +
        "-fx-background-radius: 5; " +
        "-fx-cursor: hand;";

    btn.setStyle(baseStyle);
    btn.setOnMouseEntered(e -> btn.setStyle(baseStyle.replace(colorHex, hoverColorHex)));
    btn.setOnMouseExited(e -> btn.setStyle(baseStyle));

    return btn;
  }

  /**
   * Ładuje wszystkie gry z bazy danych do listy.
   */
  private void loadGameList() {
    List<GameEntity> games = viewerService.getAllGames();
    ObservableList<GameEntity> gameList = FXCollections.observableArrayList(games);
    gameListView.setItems(gameList);
  }

  /**
   * Ładuje konkretną grę i wyświetla ją na planszy.
   * 
   * @param game encja gry do załadowania
   */
  private void loadGame(GameEntity game) {
    GameEntity fullGame = viewerService.getGameById(game.getId()).orElse(null);
    if (fullGame == null) {
      System.err.println("Failed to load game #" + game.getId());
      gameInfoLabel.setText("Błąd ładowania gry.");
      return;
    }

    currentMoves = viewerService.getMovesForGame(fullGame);
    currentMoveIndex = 0;

    System.out.println("Załadowano grę #" + fullGame.getId() + " z " + currentMoves.size() + " ruchami");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String info = String.format("Gra #%d - %s | Czarny: %s vs Biały: %s | Wynik: %s",
        fullGame.getId(),
        fullGame.getDate().format(formatter),
        fullGame.getPlayerBlackType(),
        fullGame.getPlayerWhiteType(),
        fullGame.getResult() != null ? fullGame.getResult() : "Nieznany");
    gameInfoLabel.setText(info);

    moveSlider.setMax(currentMoves.size());
    moveSlider.setValue(0);

    clearBoard();
    updateMoveInfo();
    updateControlButtons();
  }

  /**
   * Przechodzi do konkretnego ruchu w historii gry.
   * 
   * @param moveIndex indeks ruchu (0 = początek gry)
   */
  private void goToMove(int moveIndex) {
    if (currentMoves == null || currentMoves.isEmpty()) {
      return;
    }

    currentMoveIndex = Math.max(0, Math.min(moveIndex, currentMoves.size()));

    clearBoard();

    for (int i = 0; i < currentMoveIndex; i++) {
      MoveEntity move = currentMoves.get(i);
      if (move.getX() >= 0 && move.getY() >= 0) {
        Stone color = move.getColor().equals("BLACK") ? Stone.BLACK : Stone.WHITE;
        String type = move.getType();

        switch (type) {
          case "MOVE":
          case "FILL":
            setStone(move.getX(), move.getY(), color);
            break;
          case "CAPTURE":
          case "REMOVE":
            setStone(move.getX(), move.getY(), Stone.NONE);
            break;
          default:
            setStone(move.getX(), move.getY(), color);
            break;
        }
      }
    }

    moveSlider.setValue(currentMoveIndex);
    updateMoveInfo();
    updateControlButtons();
  }

  /**
   * Przechodzi do następnego ruchu.
   */
  private void nextMove() {
    if (currentMoves != null && currentMoveIndex < currentMoves.size()) {
      goToMove(currentMoveIndex + 1);
    }
  }

  /**
   * Przechodzi do poprzedniego ruchu.
   */
  private void previousMove() {
    if (currentMoveIndex > 0) {
      goToMove(currentMoveIndex - 1);
    }
  }

  /**
   * Aktualizuje etykietę z informacją o aktualnym ruchu.
   */
  private void updateMoveInfo() {
    if (currentMoves == null) {
      moveInfoLabel.setText("Move: 0 / 0");
      return;
    }

    String info = String.format("Ruch: %d / %d", currentMoveIndex, currentMoves.size());

    if (currentMoveIndex > 0 && currentMoveIndex <= currentMoves.size()) {
      MoveEntity move = currentMoves.get(currentMoveIndex - 1);
      String moveType = move.getType();
      String color = move.getColor();

      if (move.getX() >= 0 && move.getY() >= 0) {
        info += String.format(" | %s %s na (%d, %d)", color, moveType, move.getX() + 1, move.getY() + 1);
      } else {
        info += String.format(" | %s %s", color, moveType);
      }
    }

    moveInfoLabel.setText(info);
  }

  /**
   * Aktualizuje stan przycisków kontrolnych (włączone/wyłączone).
   */
  private void updateControlButtons() {
    boolean hasMoves = currentMoves != null && !currentMoves.isEmpty();
    btnFirst.setDisable(!hasMoves || currentMoveIndex == 0);
    btnPrevMove.setDisable(!hasMoves || currentMoveIndex == 0);
    btnNextMove.setDisable(!hasMoves || currentMoveIndex >= currentMoves.size());
    btnLast.setDisable(!hasMoves || currentMoveIndex >= currentMoves.size());
  }

  /**
   * Czyści wszystkie kamienie z planszy.
   */
  private void clearBoard() {
    for (int y = 0; y < BOARD_SIZE; y++) {
      for (int x = 0; x < BOARD_SIZE; x++) {
        setStone(x, y, Stone.NONE);
      }
    }
  }

  /**
   * Ustawia kamień na planszy.
   * 
   * @param x     współrzędna x
   * @param y     współrzędna y
   * @param color kolor kamienia (BLACK, WHITE, lub NONE do usunięcia)
   */
  private void setStone(int x, int y, Stone color) {
    StackPane cell = cells[x][y];

    cell.getChildren().removeIf(node -> node instanceof Circle && ((Circle) node).getRadius() > 4.0);

    if (color != Stone.NONE) {
      Circle stone = new Circle(STONE_SIZE);

      if (color == Stone.BLACK) {
        stone.setFill(Color.web("#1a1a1a"));
        stone.setStroke(Color.web("#000000"));
        stone.setStrokeWidth(0.8);
        stone.setEffect(new DropShadow(6.0, 2.0, 2.0, Color.color(0, 0, 0, 0.7)));
      } else {
        stone.setFill(Color.web("#f8f8f8"));
        stone.setStroke(Color.web("#d0d0d0"));
        stone.setStrokeWidth(1.0);
        stone.setEffect(new DropShadow(5.0, 1.5, 1.5, Color.color(0, 0, 0, 0.4)));
      }

      cell.getChildren().add(stone);
    }
  }
}
