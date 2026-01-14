package pl.edu.pwr.logic;

/**
 * Rekord zawierający wynik końcowy gry w Go.
 * 
 * Przechowuje punkty obu graczy i umożliwia określenie zwycięzcy.
 * Zawiera informacje:
 * <ul>
 *   <li>blackScore - łączna punktacja gracza czarnego (terytoria + jeńcy)</li>
 *   <li>whiteScore - łączna punktacja gracza białego (terytoria + jeńcy)</li>
 * </ul>
 * 
 * Wynik może być zwycięstwem gracza czarnego, białego lub remisem.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @param blackScore punktacja gracza czarnego
 * @param whiteScore punktacja gracza białego
 */
public record GameResult(int blackScore, int whiteScore) {
    
    /**
     * Określa zwycięzcę gry na podstawie punktacji obu graczy.
     * 
     * @return "BLACK" jeśli czarne wygrały, "WHITE" jeśli białe wygrały, "DRAW" jeśli remis
     */
    public String winner() {
        if (blackScore > whiteScore) return "BLACK";
        if (whiteScore > blackScore) return "WHITE";
        return "DRAW";
    }
}

