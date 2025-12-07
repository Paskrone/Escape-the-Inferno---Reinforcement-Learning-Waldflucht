import java.util.Random;

/*
 * Für jede Zelle gibt es 4 Q-Werte, einen für jede mögliche Aktion (up, down, left, right)
 * 
 * Die Q-Werte repräsentieren, wie gut es ist, von dieser Zelle aus eine bestimmte Aktion zu wählen, 
 * also den erwarteten kumulierten zukünftigen Belohnungswert, 
 * den der Agent erhält, wenn er dieser Aktion folgt 
 * und dann die optimale Strategie fortsetzt.
 * 
 * In der Heatmap zeigen wir nicht jeden einzelnen Q-Wert pro Zelle , 
 * sondern für jede Zelle den maximalen Q-Wert über alle (vier) Aktionen
 * 
 * Farbskala: 
 * Blau: niedriger Wert (schlecht, lohnt sich weniger)
 * Rot:  hoher Wert (gut, lohnt sich mehr)
 * 
 * Helle/rote Zellen: 
 * hier „weiß“ der Agent: Wenn ich von hier starte, kann ich mit der richtigen Aktion hohe Belohnungen bekommen.
 * Dunkle/blaue Zellen:
 * hier „weiß“ der Agent: Von hier aus ist es schlecht, also lohnt es sich nicht so sehr.
 * Nach vielen Episoden kann man an der Heatmap sehen, welche Wege der Agent gelernt hat, um das Ziel effizient zu erreichen.
 * 
 * 
 */

public class QLearningGrid_Table {

    static final int    WIDTH    = 15;
    static final int    HEIGHT   = 15;
    static final int    ACTIONS  = 4;  // up, down, left, right
    static final int    EPISODES = 1000;
    static final double ALPHA    = 0.1;
    static final double GAMMA    = 0.9;
    static final double EPSILON  = 0.2;
    static final int    GOAL_X   = WIDTH-1;
    static final int    GOAL_Y   = HEIGHT-1;

    // Q-Tabelle
    static double[][][] Q = new double[WIDTH][HEIGHT][ACTIONS];
    
    static Random rand    = new Random();
    static HeatmapVisualizer heatmap;

    public static void main(String[] args) throws InterruptedException{

        //initQ();
        
        heatmap = new HeatmapVisualizer(WIDTH, HEIGHT, GOAL_X, GOAL_Y);
        heatmap.update(Q);
        Thread.sleep(50); 
        
        for (int ep = 1; ep <= EPISODES; ep++) {
            runEpisode();
            //outputQ();


            System.out.println("Episode abgeschlossen: " + ep);
            heatmap.update(Q);
            Thread.sleep(50);      
        }
    }

    public static void initQ() {
    	for(int y=0;y<Q.length;y++) {
    		for(int x=0;x<Q[y].length;x++) {
    			for(int a=0;a<Q[y][x].length;a++) {
            		Q[y][x][a] = 10*Math.random();
            	}
        	}
    	}
    }
    
    static void runEpisode() throws InterruptedException{
        int x = 0, y = 0;
        int it =0 ;

        while (!(x == GOAL_X && y == GOAL_Y)) {
  	
            int action = chooseAction(x, y);

            int nx = x, ny = y;
            switch (action) {
                case 0: ny = Math.max(0, y - 1); break;
                case 1: ny = Math.min(HEIGHT - 1, y + 1); break;
                case 2: nx = Math.max(0, x - 1); break;
                case 3: nx = Math.min(WIDTH - 1, x + 1); break;
            }

            int reward = (nx == GOAL_X && ny == GOAL_Y) ? 10 : -1;

            updateReward(x, y, action, reward, nx, ny);
            //System.out.println();
            x = nx;
            y = ny;
    
        }
    }


    // ---------------------------
    //   CHOOSE ACTION (Epsilon-greedy)
    // ---------------------------
    static int chooseAction(int x, int y) {
        if (rand.nextDouble() < EPSILON) {
            return rand.nextInt(ACTIONS);
        }

        double best = -1e9;
        int bestA = 0;
        for (int a = 0; a < ACTIONS; a++) {
            if (Q[x][y][a] > best) {
                best = Q[x][y][a];
                bestA = a;
            }
        }
        return bestA;
    }


    // ---------------------------
    //         Q-UPDATE
    // ---------------------------
    static void updateReward(int x, int y, int action, int reward, int nx, int ny) {

        double oldQ     = Q[x][y][action];
        double maxNextQ = maxQ(nx, ny);

        Q[x][y][action] = oldQ + ALPHA * (reward + GAMMA * maxNextQ - oldQ);
    }


    static double maxQ(int x, int y) {
        double best = Q[x][y][0];
        for (int a = 1; a < ACTIONS; a++) {
            best = Math.max(best, Q[x][y][a]);
        }
        return best;
    }


    static void printValueHeatmap() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {

                if (x == GOAL_X && y == GOAL_Y) {
                    System.out.print("  G   ");
                } else {
                    System.out.printf("%4.1f ", maxQ(x, y));
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public static void outputQ() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                System.out.print(x + " " + y + " ");

                for (int a = 0; a < Q[x][y].length; a++) {
                    System.out.printf("%.4f ", Q[x][y][a]);  // 4 Nachkommastellen für Übersicht
                }
                System.out.println();
            }
        }
    }
}
