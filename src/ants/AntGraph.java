package ants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import jsp.ProblemCreator;
import jsp.ProblemCreator.Job;
import jsp.ProblemCreator.Problem;
import jsp.Scheduler;

public class AntGraph {
	private double[][] pheromone;
	private int jobs;
	private int machines;
	private double beta;
	private double decay;
	private double evap;
	private double initPheromone;
	private Random r;
	private double exploit;
	private Problem p;
	
	public AntGraph(Problem p, double initPheromone, double decay, double evap, double exploit, double beta){
		this.p = p;
		this.beta = beta;
		this.jobs = p.getNumJobs();
		this.machines = p.getNumMachines();
		this.r = new Random();
		this.initPheromone = initPheromone;
		this.decay = decay;
		this.evap = evap;
		this.exploit = exploit;
		pheromone = new double[jobs*machines][jobs*machines];
		int[][] proc = p.getProcMatrix();
		for (int i = 0; i < pheromone[0].length; i++) {
			for (int j = 0; j < pheromone[1].length; j++) {
				pheromone[i][j] = initPheromone;
			}
		}
	}
	
	public void run(int iterations, int noAnts){
		List<Ant> ants = new ArrayList<Ant>();
		for (int j = 0; j < noAnts; j++) {
			ants.add(new Ant(this.machines,this.jobs));
		}
		for (int i = 0; i < iterations; i++) {	
			for (int j = 0; j < machines*jobs; j++) {
				moveAnts(ants);
			}
//			System.out.println(Arrays.toString(ants.get(0).path));
//			System.out.println(Arrays.toString(ants.get(1).path));
//			System.out.println(Arrays.toString(ants.get(2).path));
			if(i == iterations-1){
				int[] norm = normalizeArray(ants.get(0).path);
				System.out.println(Arrays.toString(norm));
				norm = ArrayUtils.remove(norm,0);
				System.out.println(Arrays.toString(norm));
				Scheduler.buildScheduleGantt(norm, p);
			}
			ants.clear();
			for (int z = 0; z < noAnts; z++) {
				ants.add(new Ant(machines, jobs));
			}
		}
	}
	
	public void updatePheromoneLocal(int from, int to){
//		System.out.println("from: "+from+" , to: "+to);
		this.pheromone[from][to] = this.pheromone[from][to]*(1-evap)+evap*initPheromone;
	}
	
	public void updatePheromoneGlobal(List<int[]> path, double globalLength){
		/*decay on all edges*/
		for (int i = 0; i < pheromone[0].length; i++) {
			for (int j = 0; j < pheromone[1].length; j++) {
				this.pheromone[i][j] = this.pheromone[i][j]*(1-this.decay);				
			}
		}
		/*increase best path pheromone*/
		for (int[] edge : path) {
			this.pheromone[edge[0]][edge[1]] += this.decay*(1/globalLength);
		}
	}
	
	public void moveAnts(List<Ant> ants){
		for(Ant ant : ants){
//			System.out.println("# of open nodes: "+ant.getOpen().size());
			double q = r.nextDouble();
			int decision = -1;
			if(q < exploit){
				double bestScore = Double.MAX_VALUE;
				for(Integer choice : ant.getOpen()){
					double newScore = pheromone[ant.current][choice]*Math.pow((1/p.getProcMatrix()[Math.floorDiv(choice,jobs)][choice%machines]), this.beta);
					if(newScore < bestScore){
						bestScore = newScore;
						decision = choice;
					}
				}
			}else{
				EnumeratedIntegerDistribution distr = getDistr(ant);
				decision = distr.sample();
			}
			updatePheromoneLocal(ant.current, decision);
			ant.move(decision);
		}
	}
	
	public int[] normalizeArray(int[] val){
		int[] normalized = new int[machines*jobs];
		for (int i = 0; i < val.length; i++) {
			normalized[i] = Math.floorDiv(val[i], jobs);
		}
		return normalized;
	}
	
	private EnumeratedIntegerDistribution getDistr(Ant ant){
		int[] choices = (int[]) ArrayUtils.toPrimitive(ant.getOpen().toArray(new Integer[0]));
		double[] probs = new double[choices.length];
		double sum = 0;
		for (int i = 0; i < choices.length; i++) {
			int job = Math.floorDiv(choices[i],jobs);
			int oper = choices[i]%machines;
			double proc = p.getProcMatrix()[job][oper];
			double phero = pheromone[ant.current][choices[i]];
			sum += phero*Math.pow((1/proc), this.beta);
		}
		for (int i = 0; i < choices.length; i++) {
			int job = Math.floorDiv(choices[i],jobs);
			int oper = choices[i]%machines;
			double proc = p.getProcMatrix()[job][oper];
			double phero = pheromone[ant.current][choices[i]];
			probs[i]= (phero*Math.pow((1/proc), this.beta))/sum;		
		}
//		System.out.println(Arrays.toString(choices));
//		System.out.println(Arrays.toString(probs));
		return new EnumeratedIntegerDistribution(choices, probs);
	}
	
	/*parameters formatted such that operations are
	 * series of unique increasing integers*/
	public double getPheromone(int from, int to){
		return this.pheromone[from][to];
	}
	
	public static void main(String[] args) throws IOException {
		Problem p = ProblemCreator.create("1.txt");
		AntGraph a = new AntGraph(p, 10.0, 0.3, 0.4, 0.8, 2.0);
		a.run(500, 10);
	}
	
	public static class Ant{
		private boolean[] tabu;
		private HashSet<Integer> open;
		private int[] path;
		private int current;
		private int moveNo;
		private Random r;
		private int machines;
		
		public Ant(int machines, int jobs){
			this.current = 0;
			this.machines = machines;
			this.path = new int[machines*jobs];
			this.tabu = new boolean[machines*jobs]; /*defaults to false = not visited*/
			this.open = new HashSet<Integer>();
			this.r = new Random();
			for (int i = 0; i < jobs; i++) {
				open.add(i*jobs);
			}
		}
		
		public HashSet<Integer> getOpen(){
			return this.open;
		}
		
		public int[] getPath(){
			return this.path;
		}
		
		public int getCurrent(){
			return this.current;
		}
		
		public void move(int to){
			path[moveNo] = current;
			moveNo++;
			current = to;
			open.remove(to);
			tabu[to] = true;
			updateOpen();
		}
		
		private void updateOpen(){
			if(current%machines!=machines-1){
				open.add(current+1);
			}
		}
		
		
	}
}
