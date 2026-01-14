package pl.edu.pwr.logic;

/**
 * Enum reprezentujący rodzaje kamieni na planszy gry w Go.
 * 
 * Opisuje trzy możliwe stany pola na planszy:
 * <ul>
 *   <li>BLACK - pole zajęte przez kamień czarny</li>
 *   <li>WHITE - pole zajęte przez kamień biały</li>
 *   <li>NONE - pole puste, bez kamienia</li>
 * </ul>
 * 
 * Każdy punkt na planszy zawiera dokładnie jeden z tych trzech stanów.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public enum Stone {
    /** Kamień czarny - należący do gracza grającego czarnymi */
    BLACK,
    
    /** Kamień biały - należący do gracza grającego białymi */
    WHITE,
    
    /** Brak kamienia - pole puste na planszy */
    NONE
}
