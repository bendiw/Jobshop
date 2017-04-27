package bees;

import jsp.ProblemCreator;
import jsp.ProblemCreator.Problem;
import jsp.Scheduler;
import java.util.Random;

public class BeeColony {
	private Problem p;
	private double alpha;
	private double beta;
	private double rating;
	private int waggleFactor;
	private double waggleProb;
	private int eliteMax;
	private int iter;
	private double[][] path;
	private double[] profit;
	private int numJobs;
	private int numMachs;
	private Random r;
	private Scheduler s;
	
	
	
	public BeeColony(Problem p, double alpha, double beta, double rating, int waggleFactor, double waggleProb, int eliteMax, int iter) {
		this.p = p;
		this.alpha = alpha;
		this.beta = beta;
		this.rating = rating;
		this.waggleFactor = waggleFactor;
		this.waggleProb = waggleProb;
		this.eliteMax = eliteMax;
		this.iter = iter;
		r = new Random();
		init();
		run();
	}
	
	private void init(){
		numJobs = p.getNumJobs();
		numMachs = p.getNumMachines();
		path = new double[numJobs][numJobs*numMachs];
		setInitSol();
	}
	
	private void setInitSol(){
		for (int i = 0; i < numJobs; i++) {
			for (int j = 0; j < path[i].length; j++) {
				path[i][j] = r.nextDouble();
			}
			setProfit(i);
		}
	}
	
	private void setProfit(int bee) {
		int[] solution = new int[numJobs*numMachs];
		int minPos;
		int[] visited = new int[numJobs*numMachs];
		for (int i = 0; i < path[bee].length; i++) {
			minPos = 0;
			for (int j = 0; j < path[bee].length; j++) {
				if (path[bee][j] < path[bee][minPos] && visited[j] != 1) {
					minPos = j;
				}
			}
			visited[minPos] = 1;
			solution[minPos] = i;
		}
		for (int i = 0; i < solution.length; i++) {
			solution[i] = solution[i]%numJobs;
		}
		int[] finishingTimes = Scheduler.buildSchedule(solution, p);
		int makeSpan = 0;
		for (int i = 0; i < finishingTimes.length; i++) {
			if (makeSpan < finishingTimes[i]) {
				makeSpan = finishingTimes[i];
			}
		}
		profit[bee] = 1/makeSpan;
	}
	
	private void run() {
		
	}
}
