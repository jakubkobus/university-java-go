# Gra w GO (Projekt - technologia programowania)

## Autorzy
- [Jakub Kobus](https://github.com/jakubkobus) *283969*
- [Dawid Leśkiewicz](https://github.com/283974-dawidleskiewicz) *283974*

## Uruchomienie
### Serwer
```bash
mvn clean compile
mvn exec:java -Pserver
```

### Klient
```bash
mvn clean compile
mvn exec:java -Pclient
```

## Generowanie dokumentacji JavaDoc
Aby wygenerować dokumentację JavaDoc dla projektu, uruchom:

```bash
mvn javadoc:javadoc
```

Wygenerowana dokumentacja będzie dostępna w katalogu `target/site/apidocs/`

## Zastosowane wzorce projektowe
1. **Singleton** - `Server` klasa z thread-safe implementacją (double-checked locking)
2. **Facade** - `ClientFacade` ukrywająca szczegóły komunikacji sieciowej
3. **Command** - Interfejs `Command` i jego implementacje: `MoveCommand`, `PassCommand`, `SurrenderCommand`, `RemoveCommand`, `FillCommand`
4. **Factory Method** - `BoardFactory` do tworzenia i walidacji planszy
5. **Strategy** - `IScoringStrategy` interfejs z `ScoringStrategy` implementacją do elastycznego obliczania wyniku gry
6. **Observer** - Pattern listener w `ClientFacade` nasłuchujący na wiadomości z serwera
7. **Model-View-Controller (MVC)** - `GameView` interfejs z implementacjami `ConsoleView` (widok tekstowy) i `GuiView` (widok graficzny)
8. **Template Method** - `Game` klasa definiuje ogólny flow gry z możliwością przesłonięcia strategii punktacji

## Diagram klas
![Diagram klas](docs/class_diagram.png)