package environment;

/**
 * Konstanten für das ForestEnvironment
 * Enthält Zelltypen, Aktionen und Rewards
 */
public final class ForestConstants {

    // =====================================================
    //                    ZELLTYPEN
    // =====================================================
    public static final int EMPTY    = 0;  // Begehbarer Wald
    public static final int FIRE     = 1;  // Feuer (tödlich)
    public static final int EXIT     = 2;  // Ausgang (Ziel)
    public static final int OBSTACLE = 3;  // Hindernis (unpassierbar)
    public static final int WATER    = 4;  // Wasser (sicher, Feuer kann nicht durchbrennen)

    // =====================================================
    //                    AKTIONEN
    // =====================================================
    public static final int ACTION_UP    = 0;
    public static final int ACTION_DOWN  = 1;
    public static final int ACTION_LEFT  = 2;
    public static final int ACTION_RIGHT = 3;
    public static final int NUM_ACTIONS  = 4;

    public static final String[] ACTION_NAMES = {"UP", "DOWN", "LEFT", "RIGHT"};

    // =====================================================
    //                    REWARDS
    // =====================================================
    public static final double REWARD_EXIT     = 100.0;   // Ausgang erreicht
    public static final double REWARD_FIRE     = -100.0;  // Im Feuer verbrannt
    public static final double REWARD_STEP     = -1.0;    // Zeitstrafe pro Schritt
    public static final double REWARD_WALL_HIT = -2.0;    // Gegen Wand/Hindernis gelaufen
    public static final double REWARD_WATER    = 0.0;     // Neutrale Sicherheit im Wasser

    // Keine Instanziierung erlaubt
    private ForestConstants() {}
}

