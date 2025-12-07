import java.util.Random;

public class QLearningGrid_NN {

    static final int    WIDTH      = 15;
    static final int    HEIGHT     = 15;
    static final int    ACTIONS    = 4;
    static final int    EPISODES   = 1000;
    static final double GAMMA      = 0.9;
    static final double EPSILON    = 0.2;
    static final int    GOAL_X     = WIDTH -1;
    static final int    GOAL_Y     = HEIGHT-1; 

    static Random rand = new Random();
    static HeatmapVisualizer heatmap;
    static int epoche  = 0;
    static int episode = 1;

    
    // ---------------------------------------------------------------
    //  HIER unser FNN anstelle von Q
    // ---------------------------------------------------------------
	static int inputSize             = 2;		//fuer x und y
	static int outputSize            = ACTIONS;	//ein Q-Wert fuer jede der 4 Actions
	static int[] layerSizes          = {inputSize, 100, outputSize};
	static String hiddenActivations  = "relu"; 
	static String outputActivation   = "none";  //NEU!!!!!!!!!!!!!!!!!!  
	static double learningRate       = 0.09;
	static LossFunction lossFunction = new MeanSquaredError();
	
    static FFN net = new FFN(layerSizes, hiddenActivations, outputActivation, 100); 

    
    public static void main(String[] args) throws InterruptedException{

        System.out.println("Episode " + 0);
        heatmap = new HeatmapVisualizer(WIDTH, HEIGHT, GOAL_X, GOAL_Y);
        heatmap.update(berechneQ());
        Thread.sleep(50); 
        
        for (int ep = 1; ep <= EPISODES; ep++) {
            runEpisode();

            System.out.println("Episode abgeschlossen: " + ep);
            heatmap.update(berechneQ());
            Thread.sleep(5);   
            
            // Ausgabe von Statistik
//            double meanV = computeMeanV();
//            System.out.println("Episode abgeschlossen: " + ep + " | Mean V: " + meanV);
//            evalGreedy();
            episode++;
        }
    }

    static void runEpisode() throws InterruptedException{
        int x = 0, y = 0;

        while (!(x == GOAL_X && y == GOAL_Y)) {
            int action = chooseAction(x, y);
            int nx = x, ny = y;
            
            switch (action) {
                case 0: ny = Math.max(0, y - 1); 			break;
                case 1: ny = Math.min(HEIGHT - 1, y + 1); 	break;
                case 2: nx = Math.max(0, x - 1); 			break;
                case 3: nx = Math.min(WIDTH - 1, x + 1); 	break;
            }

            int reward = (nx == GOAL_X && ny == GOAL_Y) ? 10 : -1;

            updateReward(x, y, action, reward, nx, ny);

            x = nx;
            y = ny;
            
            
            //System.out.println(x + "          " + y);
        }
    }


    // ---------------------------
    //     CHOOSE ACTION
    // ---------------------------
    static int chooseAction(int x, int y) {
    	
//    	if(episode == 1)
//    		return (int)(Math.random()*ACTIONS);
        if (Math.random() < EPSILON)
            return (int)(Math.random()*ACTIONS);

    	double[] state = {norm(x), norm(y)}; 
        double[] q     = net.predictQ(state);

        int bestA = 0;
        double bestQ = q[0];
        for (int a=1; a<ACTIONS; a++) {
            if (q[a] > bestQ) {
                bestQ = q[a];
                bestA = a;
            }
        }
        return bestA;
    }

    
    

    // ---------------------------
    //        NN-UPDATE
    // ---------------------------
    
    static void updateReward(int x, int y, int action, int reward, int nx, int ny) throws InterruptedException{
        double[] state    	= {norm(x),  norm(y) };
        double[] nextState 	= {norm(nx), norm(ny) };

        double[] q_s  		= net.predictQ(state);
        double[] q_sp		= net.predictQ(nextState);

        double[] target 	= q_s.clone();

        double targetValue;

        // --- Terminalzustand korrekt behandeln ---
        if (isTerminal(nx, ny)) {
            targetValue = reward;             // KEIN maxNext, KEIN Gamma
            System.out.println("Target");
        } else {
            // normalen Bellman-Backup berechnen
            double maxNext = q_sp[0];
            for (int a = 1; a < ACTIONS; a++) {
                if (q_sp[a] > maxNext)
                    maxNext = q_sp[a];
            }
            targetValue = reward + GAMMA * maxNext;
        }
        
        double maxNext = q_sp[0];
        for (int a = 1; a < ACTIONS; a++) {
            if (q_sp[a] > maxNext)
                maxNext = q_sp[a];
        }
        targetValue = reward + GAMMA * maxNext;
        
        
        target[action] = targetValue;

        net.trainFromAction(state, target, learningRate, lossFunction, epoche);
        //net.trainMiniBatchFromAction(state, target, learningRate, lossFunction, epoche);

        epoche++;
        //heatmap.update(berechneQ());
        //Thread.sleep(5);  
    }

   

    
    
    
    // --------------------------------
    //  Hilfsmethoden
    // --------------------------------
    static double norm(int v) {
        return v / (double)(WIDTH);  // einfache Normalisierung
        //return v / 5.0;  // einfache Normalisierung

    }

    static boolean isTerminal(int x, int y) {
        // Beispiel: Ziel (5,5)
        return (x == GOAL_X && y == GOAL_Y);
    }
    
    static double maxQ(int x, int y) {
    	double[] state = {norm(x), norm(y)}; 
        double[] q     = net.predictQ(state);
        double best    = q[0];
        for (int a=1; a<ACTIONS; a++) {
        	if(best < q[a])best = q[a];
        }
        return best;
    }

    public static double[][][] berechneQ(){
    	double[][][] q = new double[WIDTH][HEIGHT][ACTIONS];
    	
    	for(int x=0;x<q.length;x++) {
    		for(int y=0;y<q[x].length;y++) {
    			double[] state = { norm(x), norm(y) };
    			double[] predQ = net.predictQ(state);
    			for(int a=0;a<predQ.length;a++) {
    				q[x][y][a] = predQ[a];
    			}
    		}
    	}
    	return q;
    }
    
    
    public static void outputQ() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                System.out.print(x + " " + y + " ");
                double[] state = {norm(x), norm(y)};
                double[] predQ = net.predictQ(state);
                for (int a = 0; a < predQ.length; a++) {
                    System.out.printf("%.4f ", predQ[a]);  // 4 Nachkommastellen für Übersicht
                }
                System.out.println();
            }
        }
    }


    static double evalGreedy() {
        int x=0,y=0;
        double totalReward = 0;
        int steps = 0;
        while(!(x==GOAL_X && y==GOAL_Y) && steps < WIDTH*HEIGHT*2) {
            double[] state = {norm(x), norm(y)};
            double[] q = net.predictQ(state);
            int a = NNMath.argmax(q);
            int nx=x, ny=y;
            switch(a) {
                case 0: ny = Math.max(0, y-1); break;
                case 1: ny = Math.min(HEIGHT-1, y+1); break;
                case 2: nx = Math.max(0, x-1); break;
                default: nx = Math.min(WIDTH-1, x+1); break;
            }
            totalReward += ((nx==GOAL_X && ny==GOAL_Y) ? 10 : -1);
            x=nx; y=ny; steps++;
        }
        System.out.println("Eval greedy: reward="+totalReward+" steps="+steps);
        return totalReward;
    }
    static double computeMeanV() {
        double sum=0; int count=0;
        for(int x=0;x<WIDTH;x++) for(int y=0;y<HEIGHT;y++) {
            double[] s = {norm(x), norm(y)};
            double[] q = net.predictQ(s);
            double best = q[0];
            for(int a=1;a<ACTIONS;a++) if(q[a]>best) best=q[a];
            sum += best; count++;
        }
        return sum/count;
    }


}
