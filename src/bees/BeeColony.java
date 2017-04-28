package bees;

import jsp.ProblemCreator;
import jsp.ProblemCreator.Problem;
import jsp.Scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import utils.Utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import ants.AntGraph.Ant;

public class BeeColony {
	private Problem p;
	private double alpha;
	private double beta;
	private double rating;
	private int waggleFactor;
	private double waggleProb;
	private double[] followProb;
	private int eliteMax;
	private int iter;
	private double[][] path;
	private double[] profit;
	private int numJobs;
	private int numMachs;
	private Random r;
	private Scheduler s;
	
	
	
	public BeeColony(Problem p, double alpha, double beta, double rating, double waggleProb) {
		this.p = p;
		this.alpha = alpha;
		this.beta = beta;
		this.rating = rating;
		this.waggleFactor = waggleFactor;
		this.waggleProb = waggleProb;
		this.eliteMax = eliteMax;
		this.iter = iter;
		this.followProb = new double[]{0.6, 0.2, 0.02, 0};
		r = new Random();
		init();
	}
	
	private void init(){
	}
	
	private ArrayList<int[]> generateInitSol(int bees){
		ArrayList<int[]> chromos = new ArrayList<int[]>();
		int[] initChromo = new int[p.getNumJobs()*p.getNumMachines()];
		for (int i = 0; i < initChromo.length; i++) {
			initChromo[i] = i;
		}
		for (int i = 0; i < bees; i++) {
			int[] chromo = initChromo.clone();
			Utils.shuffleArray(chromo);
			chromos.add(chromo);
		}
		return chromos;
	}
	
	private void setProfit(int bee) {
	}
	
	private EnumeratedIntegerDistribution getDistr(Bee bee){
		int[] choices = new int[bee.getMoves().size()];
		for (int i = 0; i < choices.length; i++) {
			choices[i]=i;
		}
		double[] ratings = bee.getRatings();
		double[] attr = bee.getAttractiveness();
		double[] probs = new double[choices.length];
		double sum = 0;
		for (int i = 0; i < choices.length; i++) {
			probs[i] = Math.pow(ratings[i],alpha)*Math.pow(attr[i], beta);
			sum += probs[i];
		}
		if(sum!=0){
			for (int i = 0; i < probs.length; i++) {
				probs[i] = probs[i]/sum;
			}
		}else{
			probs[0] = 1;
		}
//		System.out.println("rat: "+ratings[0]);
//		System.out.println("probs: "+Arrays.toString(probs));
		return new EnumeratedIntegerDistribution(choices, probs);
	}
	
	private EnumeratedIntegerDistribution getDistr(ArrayList<Double> prof, double avgProf){
		int[] indexes = new int[prof.size()];
//		System.out.println(avgProf);
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}
		double[] probs = new double[prof.size()];
		for (int i = 0; i < probs.length; i++) {
			probs[i] = prof.get(i)/avgProf;
		}
//		System.out.println(Arrays.toString(probs));
		return new EnumeratedIntegerDistribution(indexes, probs);
	}

	
	private void run(int iterations, int bees, double probDance) throws IOException {
		ArrayList<int[]> initChromos = generateInitSol(bees);
		ArrayList<Bee> colony = new ArrayList<Bee>();
		for (int i = 0; i < bees; i++) {
			colony.add(new Bee(p, initChromos.get(i), rating));
		}
		int globBest = Integer.MAX_VALUE;
		int[] bestChromo = null;
		int[] bestSchedule = null;
		forage(colony);
		for (int i = 0; i < iterations; i++) {
			int iterBest = Integer.MAX_VALUE;
			boolean changed = false; 
			ArrayList<Bee> dancers = new ArrayList<Bee>();
			ArrayList<Double> danceProf = new ArrayList<Double>();
			double[] prof = new double[colony.size()];
			double avgProf = 0;
			double avgSpan = 0;
			for (int j = 0; j < colony.size(); j++) {
				Bee bee = colony.get(j);
				boolean glob = false;
				boolean iterb = false;
				int[] schedule = Scheduler.buildSchedule(normalizeArray(bee.getChromo()),p);
				int makeSpan = Scheduler.makespanFitness(schedule);
				if(makeSpan < globBest){
					globBest = makeSpan;
					bestChromo = bee.getChromo();
					bestSchedule = schedule;
					changed = true;
					glob = true;
				}
				if(makeSpan < iterBest){
					iterBest = makeSpan;
					iterb= true;
				}
				double q = r.nextDouble();
				double beeProf = 1/(double)makeSpan;
				prof[j] = beeProf;
				if(q<waggleProb || iterb){
					dancers.add(bee);
					danceProf.add(beeProf);
					avgProf+= beeProf;
					avgSpan += (double) makeSpan;
				}
			}
			int recruited = 0;
			if(dancers.size()>0){
				double avgProf2 = avgProf/(double)dancers.size();
				EnumeratedIntegerDistribution danceDistr = getDistr(danceProf, avgProf);
				for (int j = 0; j < colony.size(); j++) {
					double q = r.nextDouble();
					double rProb = getProb(prof[j], avgProf2);
//					System.out.println("r: "+rProb);
					if(q < rProb){
						recruited+=1;
						Bee dancer = dancers.get(danceDistr.sample());
						colony.get(j).adoptPreferred(dancer.getPreferred(), dancer.getNumPreferred());
					}
				}
			}
			forage(colony);
			if(changed){
				System.out.println("Iteration: "+i+"\t Best: "+globBest);
				System.out.println("bees recruited by dancing: "+recruited);
				System.out.println("Dancers: "+dancers.size()+", avg. dancer makespan: "+avgSpan/dancers.size()+"\n");
			}
//			System.out.println("iteration best: "+iterBest);
		}
<<<<<<< HEAD
		System.out.println("Best: "+globBest);
=======
		System.out.println("\t Best: "+globBest);
		Scheduler.buildScheduleGantt(normalizeArray(bestChromo), p);
>>>>>>> refs/remotes/origin/master
	}
	public int[] normalizeArray(int[] val){
		int[] normalized = new int[p.getNumJobs()*p.getNumMachines()];
		for (int i = 0; i < val.length; i++) {
			normalized[i] = Math.floorDiv(val[i], p.getNumMachines());
		}
		return normalized;
	}
	
	public void forage(List<Bee> bees) throws IOException{
		int noMoves = 0;
		Bee drawBee = null;
		for (Bee bee : bees) {
			if(bee.getMoves().size()>0){
				EnumeratedIntegerDistribution distr = getDistr(bee);
				int choice = distr.sample();
				int[] move = bee.getMoves().get(choice);
				int[] schedule = Scheduler.buildSchedule(normalizeArray(bee.getChromo()),p);
//				int makeSpan = Scheduler.makespanFitness(schedule);
//				System.out.println("before:"+makeSpan);
//				System.out.println(Arrays.toString(bee.getChromo()));
				bee.move(move);
//				System.out.println(Arrays.toString(bee.getChromo()));
//				System.out.println("after:"+makeSpan);
				ArrayList<int[]> moves = bee.getMoves();
				bee.updateMoves(p);
				if(bees.indexOf(bee)==0){
//					System.out.println("move: "+Arrays.toString(move));
//					System.out.println("new chromo: "+Arrays.toString(bee.getChromo()));
				}
			}else{
				Scheduler.buildScheduleGantt(normalizeArray(drawBee.getChromo()), p);
				noMoves +=1;
				Scheduler.getMoves(bee.getChromo(),p);
			}
		}
//		if(drawBee!=null)
//		System.out.println("number of moveless bees: "+noMoves);
	}
	
	public double getProb(double prof, double avgProf){
		double ratio = prof/avgProf;
		if(ratio<0.5)
			return 0.6;
		else if(ratio>=0.5 && ratio <0.65)
			return 0.2;
		else if(ratio >= 0.65 && ratio < 0.85)
			return 0.02;
		else
			return 0;
		}
	
	public static void main(String[] args) throws IOException {
<<<<<<< HEAD
		Problem p = ProblemCreator.create("2.txt");
		BeeColony bc = new BeeColony(p, 1, 1, 0.008, 0.01);
//		ArrayList<int[]> c = bc.generateInitSol(30);
//		System.out.println(Arrays.toString(c.get(0)));
//		System.out.println(Arrays.toString(c.get(1)));
		bc.run(300, 100, 0.01);
=======
		Problem p = ProblemCreator.create("1.txt");
		BeeColony bc = new BeeColony(p, 1, 1,0.99,0.01);
		ArrayList<int[]> c = bc.generateInitSol(30);
		System.out.println(Arrays.toString(c.get(0)));
		System.out.println(Arrays.toString(c.get(1)));
		bc.run(2, 50000, 0.01);
>>>>>>> refs/remotes/origin/master
	}
	
	public static class Bee{
		private int[] chromosome;
		private int[] indexes;
		private ArrayList<int[]> moves;
		private double[] attractiveness;
		private boolean[][] preferred;
		private int numPref;
		private double gamma;
		private int movesMade;

		
		public Bee(Problem p, int[] chromo, double gamma){
			chromosome = chromo;
			this.movesMade = 0;
			this.gamma = gamma;
			moves = (ArrayList<int[]>)Scheduler.getMoves(chromo, p);
			attractiveness = Scheduler.getAttract(moves, p, chromo);
			indexes = new int[chromo.length];
			for (int i = 0; i < chromo.length; i++) {
				indexes[chromosome[i]] = i;
			}
			preferred = new boolean[p.getNumJobs()*p.getNumMachines()][p.getNumJobs()*p.getNumMachines()];
		}
		
		
		public void adoptPreferred(boolean[][] pref, int numPref){
			this.preferred = pref;
			this.numPref = numPref;
		}
		
		public int[] getChromo(){
			return this.chromosome;
		}
		
		public void updateMoves(Problem p){
			this.moves = (ArrayList<int[]>)Scheduler.getMoves(chromosome, p);
			attractiveness = Scheduler.getAttract(moves, p, chromosome);
		}
		
		public double[] getAttractiveness(){
			return this.attractiveness;
		}
		
		public int getNumPreferred(){
			return this.numPref;
		}
		public boolean[][] getPreferred(){
			return this.preferred;
		}
		
		public ArrayList<int[]> getMoves(){
			return this.moves;
		}
		
		private int getIntersection(){
			int num = 0;
			for (int[] move : moves) {
				if(preferred[move[0]][move[1]]){
					num+=1;
				}
			}
			return num;
		}
		
		public double[] getRatings(){
			double[] ratings = new double[this.moves.size()];
			int inter = getIntersection();
			inter = Math.min(inter, 1);
			if(numPref ==0 || numPref - inter == 0){
				for (int i = 0; i < ratings.length; i++) {
					ratings[i] = 0.99;
				}
			}else{
				for (int i = 0; i < ratings.length; i++) {
					if(preferred[moves.get(i)[0]][moves.get(i)[1]]){
						ratings[i] = gamma;
//						System.out.println("index "+i+" is preferred.");
					}else{
						ratings[i] = (1-gamma*inter)/(numPref-inter);
					}
				}
			}
//			System.out.println(Arrays.toString(ratings));
			return ratings;
		}
		
		public int getMovesMade(){
			return this.movesMade;
		}
		
		public void move(int[] move){
			this.movesMade+=1;
			int first = move[0];
			int second = move[1];
//			System.out.println("first: "+first+", sec: "+second);
//			System.out.println("fIndex:"+indexes[first]+", sIndex: "+indexes[second]);
//			System.out.println("index: "+Arrays.toString(indexes));
			preferred[first][second] = true;
			numPref += 1;
			chromosome[indexes[first]] = second;
			chromosome[indexes[second]] = first;
			int tempIndex = new Integer(indexes[first]);
			indexes[first] = new Integer(indexes[second]);
			indexes[second] = tempIndex;
//			System.out.println("index: "+Arrays.toString(indexes));
//			System.out.println("fIndex:"+indexes[first]+", sIndex: "+indexes[second]);
		}
	}
	
	
}
