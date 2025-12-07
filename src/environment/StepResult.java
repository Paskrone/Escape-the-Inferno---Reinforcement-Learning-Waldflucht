package environment;

/**
 * Ergebnis eines step()-Aufrufs im ForestEnvironment
 * 
 * @param state   Aktueller Zustand [x, y]
 * @param reward  Erhaltene Belohnung
 * @param done    Episode beendet?
 * @param escaped Erfolgreich entkommen?
 */
public record StepResult(int[] state, double reward, boolean done, boolean escaped) {
    
    @Override
    public String toString() {
        return String.format("StepResult{state=[%d,%d], reward=%.1f, done=%b, escaped=%b}",
                state[0], state[1], reward, done, escaped);
    }
}

