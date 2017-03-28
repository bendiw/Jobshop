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
	private double[] firstPhero;
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
		firstPhero = new double[jobs];
		for (int i = 0; i < firstPhero.length; i++) {
			firstPhero[i] = initPheromone;
		}
		int[][] proc = p.getProcMatrix();
		for (int i = 0; i < pheromone[0].length; i++) {
			for (int j = 0; j < pheromone[1].length; j++) {
				pheromone[i][j] = initPheromone;
			}
		}
	}
	
	public void run(int iterations, int noAnts){
		List<Ant> ants = new ArrayList<Ant>();
		int bestSpan = Integer.MAX_VALUE;
		int[] bestSchedule=null;
		int[] bestChromo = null;
		int[] bestPath = null;
		for (int i = 0; i < iterations; i++) {	
			ants.clear();
			for (int z = 0; z < noAnts; z++) {
				ants.add(new Ant(machines, jobs));
			}
			for (int j = 0; j < machines*jobs; j++) {
				moveAnts(ants);
			}
			
			for (Ant ant : ants) {
				int[] path = ant.path;
				int[] chromo = ArrayUtils.remove(normalizeArray(ant.path),0);
//				System.out.println("path: "+Arrays.toString(path));
//				System.out.println(Arrays.toString(chromo));
//				System.out.println("length: "+chromo.length);
				int[] schedule = Scheduler.buildSchedule(chromo,p);
				int antSpan = Scheduler.makespanFitness(schedule);
				if (antSpan < bestSpan){
					bestPath = path;
					bestSchedule = schedule;
					bestChromo = chromo;
					bestSpan = antSpan;
				}
			}
			updatePheromoneGlobal(bestPath, bestSpan);
			
//			System.out.println(Arrays.toString(ants.get(0).path));
//			System.out.println(Arrays.toString(ants.get(1).path));
//			System.out.println(Arrays.toString(ants.get(2).path));
		}
		Scheduler.buildScheduleGantt(bestChromo, p);
		System.out.println(bestChromo.length);
		System.out.println("chromo: "+Arrays.toString(bestChromo));
		System.out.println("Best makespan: "+bestSpan);
		System.out.println("times: "+Arrays.toString(bestSchedule));
		int[] norm = normalizeArray(ants.get(0).path);
//		System.out.println(Arrays.toString(norm));
//		norm = ArrayUtils.remove(norm,0);
//		System.out.println(Arrays.toString(norm));
	}
	
	public void updatePheromoneLocal(int from, int to){
//		System.out.println("from: "+from+" , to: "+to);
		if(from ==-1){
			this.firstPhero[Math.floorDiv(to, machines)] = this.firstPhero[Math.floorDiv(to, machines)]*(1-evap)+evap*initPheromone;
		}else{
			this.pheromone[from][to] = this.pheromone[from][to]*(1-evap)+evap*initPheromone;
		}
	}
	
	public void updatePheromoneGlobal(int[] path, double globalLength){
		/*decay on all edges*/
		for (int i = 0; i < pheromone[0].length; i++) {
			for (int j = 0; j < pheromone[1].length; j++) {
				this.pheromone[i][j] = this.pheromone[i][j]*(1-this.decay);				
			}
		}
		for (int i = 0; i < this.firstPhero.length; i++) {
			firstPhero[i] = firstPhero[i]*(1-decay);
		}
		/*increase best path pheromone*/
		for (int i = 0; i < path.length-1; i++) {
			if(path[i]==-1){
				this.firstPhero[Math.floorDiv(path[i+1], machines)] += this.decay*(1/globalLength);
			}else{
				this.pheromone[path[i]][path[i+1]] += this.decay*(1/globalLength);
			}
		}
	}
	
	public void moveAnts(List<Ant> ants){
		for(Ant ant : ants){
//			System.out.println("# of open nodes: "+ant.getOpen().size());
			double q = r.nextDouble();
			int decision = -1;
			if(q < exploit){
				double bestScore = Double.MIN_VALUE;
				double newScore;
				for(Integer choice : ant.getOpen()){
					if(ant.current == -1){
						double proc = p.getProcMatrix()[Math.floorDiv(choice,jobs)][choice%machines];
						double quotient = (1/proc);
						double val = Math.pow(quotient, this.beta);
						newScore = firstPhero[Math.floorDiv(choice, machines)]*val;
					}else{
						double divisor = p.getProcMatrix()[Math.floorDiv(choice,jobs)][choice%machines];
						newScore = pheromone[ant.current][choice]*Math.pow((1/divisor), this.beta);
					}
					if(newScore > bestScore){
						bestScore = newScore;
						decision = choice;
					}else if(newScore == bestScore){
						q = r.nextDouble();
						if(q > 0.5){
							bestScore = newScore;
							decision = choice;
						}
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
		int[] normalized = new int[machines*jobs+1];
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
			double phero = 0;
			if(ant.current==-1){
				phero = firstPhero[Math.floorDiv(choices[i],machines)];
			}else{
				phero = pheromone[ant.current][choices[i]];
			}
			probs[i]= phero*Math.pow((1/proc), this.beta);
			sum += probs[i];
		}
		for (int i = 0; i < probs.length; i++) {
			probs[i] = probs[i]/sum;
		}
//		for (int i = 0; i < choices.length; i++) {
//			int job = Math.floorDiv(choices[i],jobs);
//			int oper = choices[i]%machines;
//			double proc = p.getProcMatrix()[job][oper];
//			double phero = 0;
//			if(ant.current==-1){
//				phero = firstPhero[Math.floorDiv(choices[i],machines)];
//			}else{
//				phero = pheromone[ant.current][choices[i]];
//			}
//			probs[i]= (phero*Math.pow((1/proc), this.beta))/sum;		
//		}
//		System.out.println(Arrays.toString(choices));
//		System.out.println(Arrays.toString(probs));
//		System.out.println("choices: "+Arrays.toString(choices));
		return new EnumeratedIntegerDistribution(choices, probs);
	}
	
	/*parameters formatted such that operations are
	 * series of unique increasing integers*/
	public double getPheromone(int from, int to){
		return this.pheromone[from][to];
	}
	
	public static void main(String[] args) throws IOException {
		Problem p = ProblemCreator.create("1.txt");
		AntGraph a = new AntGraph(p, 2.0, 0.01, 0.5, 0, 1.5);
		a.run(1000, 36);
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
			this.current = -1;
			moveNo = 0;
			this.machines = machines;
			this.path = new int[machines*jobs+1];
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
			moveNo+=1;
			current = to;
			open.remove(to);
			tabu[to] = true;
			updateOpen();
			if(open.isEmpty()){
				path[moveNo] = to;
//				System.out.println(Arrays.toString(path));
			}
		}
		
		private void updateOpen(){
			if(current%machines!=machines-1){
				open.add(current+1);
			}
		}
		
		
	}
}
