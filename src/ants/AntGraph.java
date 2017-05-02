package ants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import jsp.ProblemCreator;
import jsp.ProblemCreator.Job;
import jsp.ProblemCreator.Problem;
import swarm.PSO.Particle;
import utils.Utils;
import jsp.Scheduler;

public class AntGraph {
	private double[][] pheromone;
	private double[] firstPhero; //pheromone for paths from origin
	private double maxPhero;
	private double minPhero;
	private int jobs;
	private int machines;
	private double beta;
	private double decay;
	private double evap;
	private double initPheromone;
	private Random r;
	private double exploit;
	private double pSwap;
	private double pInsert;
	private double pInvert;
	private Problem p;
	private ArrayList<Integer> indexes;
	
	public AntGraph(Problem p, double initPheromone, double decay, double evap, double exploit, double beta, double maxPhero, double minPhero, double pSwap, double pInsert, double pInvert){
		this.p = p;
		this.beta = beta;
		this.jobs = p.getNumJobs();
		this.machines = p.getNumMachines();
		this.r = new Random();
		this.initPheromone = initPheromone;
		this.decay = decay;
		this.evap = evap;
		this.exploit = exploit;
		this.pSwap = pSwap;
		this.pInsert = pInsert;
		this.pInvert = pInvert;
		this.maxPhero = maxPhero;
		this.minPhero = minPhero;
		pheromone = new double[jobs*machines][jobs*machines];
		firstPhero = new double[jobs];
		resetPhero();
		indexes = new ArrayList<Integer>();
		for (int i = 1; i < p.getNumJobs()*p.getNumMachines(); i++) {
			indexes.add(i);
		}
	}
	
	private void resetPhero(){
		for (int i = 0; i < firstPhero.length; i++) {
//			firstPhero[i] = initPheromone;
			firstPhero[i] = maxPhero;
		}
		for (int i = 0; i < pheromone[0].length; i++) {
			for (int j = 0; j < pheromone[1].length; j++) {
//				pheromone[i][j] = initPheromone;
				pheromone[i][j] = maxPhero;
			}
		}
	}
	
	public void run(int iterations, int noAnts, double MIEprob, double endTemp, double cooling) throws IOException{
		List<Ant> ants = new ArrayList<Ant>();
		
		//vars to store best global solution
		int globalBestSpan = Integer.MAX_VALUE;
		int[] globalBestSchedule=null;
		int[] globalBestChromo = null;
		int[] globalBestPath = null;
		
		//vars to store iteration best solution
		int bestSpan = Integer.MAX_VALUE;
		int[] bestSchedule=null;
		int[] bestChromo = null;
		int[] bestPath = null;
		
		for (int i = 0; i < iterations; i++) {
			boolean changed = false;
			ants.clear();
			for (int z = 0; z < noAnts; z++) {
				ants.add(new Ant(machines, jobs));
			}
			for (int j = 0; j < machines*jobs; j++) {
				moveAnts(ants);
			}
			
			bestSpan = Integer.MAX_VALUE; //for iteration best update
			
			for (Ant ant : ants) {
				int[] path = ant.path;
//				int[] chromo = normalizeArray(ant.path);
//				System.out.println("Chromo before: "+Arrays.toString(chromo));
				int[] newChrom = Arrays.copyOfRange(ant.path, 1, ant.path.length);
				int[] giffUnNorm = Scheduler.giffThomp(newChrom, p);
				int[] giffChromo = Utils.normalizeArray(giffUnNorm, p.getNumMachines(), p.getNumJobs());
//				System.out.println("Chromo unnormalized: "+Arrays.toString(giffUnNorm));

//				System.out.println("Chromo after: "+Arrays.toString(chromo));
//				System.out.println("path: "+Arrays.toString(path));
//				System.out.println(Arrays.toString(chromo));
//				System.out.println("length: "+chromo.length);
//				System.out.println(Arrays.toString(chromo));
//				int[] schedule = Scheduler.buildSchedule(chromo,p);
				int[] schedule = Scheduler.buildSchedule(giffChromo, p);
//				int antSpan = Scheduler.makespanFitness(schedule);
				int antSpan = Scheduler.makespanFitness(schedule);
//				System.out.println("giff: "+antSpanGiff);
//				System.out.println("norm: "+antSpan);
//				antSpan = Math.min(antSpan, antSpanGiff);
				double mie = r.nextDouble();
				if(mie <= MIEprob){
					double initTemp = antSpan-globalBestSpan+10;
					MIE(ant, antSpan, initTemp, endTemp, cooling);
				}
				
				if (antSpan < bestSpan){ //test for iteration best
					bestPath = path;
					bestSchedule = schedule;
					bestChromo = giffChromo;
					bestSpan = antSpan;
				}
				
				if (antSpan < globalBestSpan){ //test for global best
					changed = true;
					globalBestPath = path;
					globalBestSchedule = schedule;
					globalBestChromo = giffChromo;
					globalBestSpan = antSpan;
					this.maxPhero = (1/(this.decay))*(1/globalBestSpan);
					if(this.maxPhero < this.minPhero)
						this.maxPhero = this.minPhero;
				}
			}
			if(i%10 == 0 || changed){
//			if(changed){
//				for (int j = 0; j < pheromone[0].length; j++) {
////					System.out.println(Arrays.toString(pheromone[j]));
//				}
				System.out.println("Iteration: "+i+".\tBest global makespan: "+globalBestSpan+".\t Best of iteration: "+bestSpan);
			}
			updatePheromoneGlobal(globalBestPath, globalBestSpan);
			
//			System.out.println(Arrays.toString(ants.get(0).path));
//			System.out.println(Arrays.toString(ants.get(1).path));
//			System.out.println(Arrays.toString(ants.get(2).path));
		}
//		for (int j = 0; j < pheromone[0].length; j++) {
//			for (int j2 = 0; j2 < pheromone[1].length; j2++) {
//				System.out.print(pheromone[j][j2]+"\t");
//			}
//			System.out.println("");
//		}
		Scheduler.buildScheduleGantt(globalBestChromo, p);
		System.out.println(globalBestChromo.length);
		System.out.println("chromo: "+Arrays.toString(globalBestChromo));
		System.out.println("Best makespan: "+globalBestSpan);
		System.out.println("times: "+Arrays.toString(globalBestSchedule));
//		int[] norm = normalizeArray(ants.get(0).path);
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
				this.pheromone[i][j] = this.pheromone[i][j]*(1-this.decay); //non MMAS
//				this.pheromone[i][j] = Math.max(this.pheromone[i][j]*(1-this.decay), this.minPhero);				
			}
		}
		for (int i = 0; i < this.firstPhero.length; i++) {
			firstPhero[i] = firstPhero[i]*(1-decay); //non MMAS
//			firstPhero[i] = Math.max(firstPhero[i]*(1-decay), this.minPhero);
		}
		/*increase best path pheromone*/
		for (int i = 0; i < path.length-1; i++) {
			if(path[i]==-1){
				this.firstPhero[Math.floorDiv(path[i+1], machines)] += (1-this.decay)*(1/globalLength); //non MMAS, old: this.decay*
//				this.firstPhero[Math.floorDiv(path[i+1], machines)] += (1/globalLength);
//				this.firstPhero[Math.floorDiv(path[i+1], machines)] = Math.min(this.firstPhero[Math.floorDiv(path[i+1], machines)], this.maxPhero);

			}else{
				this.pheromone[path[i]][path[i+1]] += (1-this.decay)*(1/globalLength); //non MMAS, old this.decay*
//				this.pheromone[path[i]][path[i+1]] += (1/globalLength);
//				this.pheromone[path[i]][path[i+1]] = Math.min(this.pheromone[path[i]][path[i+1]], this.maxPhero);
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
//				EnumeratedIntegerDistribution distr = getDistr(ant);
//				decision = distr.sample();
				decision = getMove(ant);
			}
//			updatePheromoneLocal(ant.current, decision); //comment out to only allow best ant to deposit
			ant.move(decision);
		}
	}
	
	public int getMove(Ant ant){
		int[] choices = (int[]) ArrayUtils.toPrimitive(ant.getOpen().toArray(new Integer[0]));
		double[] probs = new double[choices.length];
		double sum = 0;
		for (int i = 0; i < choices.length; i++) {
			int job = Math.floorDiv(choices[i],jobs);
			int oper = choices[i]%machines;
			double proc = p.getProcMatrix()[job][oper];
			double phero = 0;
			if(ant.current==-1){
//				System.out.println("jobs: "+jobs);
//				System.out.println("machines: "+machines);
//				System.out.println(Math.floorDiv(choices[i],machines));
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
		double sumProb = probs[0];
		double q = r.nextDouble();
		for (int i = 0; i < choices.length; i++) {
			if(q<sumProb){
				return choices[i];
			}else{
				sumProb+=probs[i+1];
			}
		}
		return -1;
	}
	
	private void MIE(Ant ant, int fitness, double startTemp, double endTemp, double cooling){
//		double[] position = prt.getPosition().clone();
		int[] position = Arrays.copyOf(ant.getPath(), ant.getPath().length);
//		int[] jobArray = Utils.getJobArray(position, p.getNumJobs());
		double temperature = startTemp;
		while(temperature > endTemp){
			Collections.shuffle(indexes);
			double q = r.nextDouble();
			if(q<=pSwap){
				position = swap(position);
			}
			else if(q>pSwap && q <=pInsert+pSwap){
				position =  insert(position);
			}
			else if(q>pInsert+pSwap && q<pInsert+pSwap+pInvert){
				invert(position);
			}else{
				longMov(position);
			}
			int[] chromo = normalizeArray(position);
//			System.out.println(Arrays.toString(position));
//			System.out.println(Arrays.toString(chromo));
			int[] schedule = Scheduler.buildSchedule(chromo,p);
			int newFitness = Scheduler.makespanFitness(schedule);
			if(newFitness <= fitness){
				ant.setPath(position);
//				prt.setPosition(position);
//				prt.updateFitness(newFitness);
			}else{
				double delta = newFitness-fitness;
				double rand = r.nextDouble();
				double accept = Math.exp(-(delta/temperature))*1;
				if(rand < Math.min(1, accept)){
//					prt.setPosition(position);
//					prt.updateFitness(newFitness);
					ant.setPath(position);
				}
			}
			temperature = temperature*cooling;
		}
	}
	
	private int[] swap(int[] position){
		boolean swapped = false;
		int index = indexes.get(0);
		int count = 1;
		while(!swapped){
			int swapIndex = indexes.get(count);
			count++;
			if(position[index]!=position[swapIndex]){
				int temp = new Integer(position[index]);
				position[index] = new Integer(position[swapIndex]);
				position[swapIndex] = temp;
				swapped = true;
			}
		}
		return position;
	}
	
	private int[] insert(int[] position){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i = 0; i < position.length; i++) {
			temp.add(position[i]);
		}
		int index = indexes.get(0);
		int insIndex = indexes.get(1);
		temp.add(insIndex, position[index]);
		if(insIndex < index){
			index+=1;
		}
		temp.remove(index);
		for (int i = 0; i < position.length; i++) {
			position[i] = temp.get(i);
		}
		return position;
	}
	
	private int[] invert(int[] position){
		int indexOne = indexes.get(0);
		int indexTwo = indexes.get(1);
		int firstIndex = Math.min(indexOne, indexTwo);
		int secondIndex = Math.max(indexOne,indexTwo);
		ArrayUtils.reverse(position, firstIndex, secondIndex+1);
		return position;
	}
	
	private int[] longMov(int[] position){
		ArrayList<Integer> tempList = new ArrayList<Integer>();
		ArrayList<Integer> tempList2 = new ArrayList<Integer>();
		for (int i = 0; i < position.length; i++) {
			tempList.add(position[i]);
		}
		int indexOne = indexes.get(0);
		int indexTwo = indexes.get(1);
		int firstIndex = Math.min(indexOne, indexTwo);
		int secondIndex = Math.max(indexOne,indexTwo);
		for (int i = firstIndex; i < secondIndex; i++) {
			tempList2.add(tempList.remove(firstIndex));
		}
		int insert = 1;
		if(tempList.size()!=0){
			insert = r.nextInt(tempList.size())+1;
		}
		tempList.addAll(insert, tempList2);
		for (int i = 0; i < position.length; i++) {
			position[i] = tempList.get(i);
		}
		return position;
	}
	
	public int[] normalizeArray(int[] val){
		int[] normalized = new int[machines*jobs];
		for (int i = 1; i < val.length; i++) {
			normalized[i-1] = Math.floorDiv(val[i], machines);
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
		return new EnumeratedIntegerDistribution(choices, probs);
	}
	
	/*parameters formatted such that operations are
	 * series of unique increasing integers*/
	public double getPheromone(int from, int to){
		return this.pheromone[from][to];
	}
	
	public static void main(String[] args) throws IOException {
		Problem p = ProblemCreator.create("6.txt");
		AntGraph a = new AntGraph(p, 2, 0.03, 0.1, 0, 1, 100, 0.001, 0.4, 0.4, 0.1); //decay was 0.01
		a.run(1000, 150, 0.0, 0.1, 0.97);
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
				open.add(i*machines);
			}
		}
		
		public HashSet<Integer> getOpen(){
			return this.open;
		}
		
		public int[] getPath(){
			return this.path;
		}
		
		public void setPath(int[] path){
			this.path = path;
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
