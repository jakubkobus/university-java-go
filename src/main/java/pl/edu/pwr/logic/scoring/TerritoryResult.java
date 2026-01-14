package pl.edu.pwr.logic.scoring;

/**
 * Rekord zawierający wynik obliczenia terytoriów graczy w grze w Go.
 * 
 * Przechowuje punkty terytorialne dla obu graczy:
 * <ul>
 *   <li>blackTerritory - liczba pól terytoriów zajętych przez gracza czarnym</li>
 *   <li>whiteTerritory - liczba pól terytoriów zajętych przez gracza białym</li>
 * </ul>
 * 
 * Terytoria reprezentują puste punkty na planszy otoczone kamieniami jednego koloru.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @param blackTerritory liczba punktów terytoriów gracza czarnego
 * @param whiteTerritory liczba punktów terytoriów gracza białego
 */
public record TerritoryResult(int blackTerritory, int whiteTerritory) {}

