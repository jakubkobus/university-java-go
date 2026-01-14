package pl.edu.pwr.logic;

/**
 * Enum reprezentujący stany gry w Go.
 * 
 * Opisuje możliwe stany, w jakich może się znajduje gra:
 * <ul>
 *   <li>IN_PROGRESS - gra trwa, gracze wykonują ruchy</li>
 *   <li>CLEANUP - faza czyszczenia, gracze usuwają zbite kamienie (cleanup phase)</li>
 *   <li>FINISHED - gra skończyła się, wyniki zostały obliczone</li>
 * </ul>
 * 
 * Przejścia stanów: IN_PROGRESS → CLEANUP → FINISHED
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public enum GameState {
    /** Stan gry w trakcie - gracze aktywnie grają */
    IN_PROGRESS,
    
    /** Faza czyszczenia planszy - usuwanie zbytych kamieni przed liczeniem punktów */
    CLEANUP,
    
    /** Gra zakończona - wyniki obliczone, gra nie może być kontynuowana */
    FINISHED
}
