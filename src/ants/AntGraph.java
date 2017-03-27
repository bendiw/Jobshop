package ants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jsp.ProblemCreator;
import jsp.ProblemCreator.Job;
import jsp.ProblemCreator.Problem;

public class AntGraph {
	private double[][] pheromone;
	private int jobs;
	private int machines;
	private double decay;
	private double evap;
	private double initPheromone;
	
	public AntGraph(Problem p, double initPheromone, double decay, double evap){
		this.jobs = p.getNumJobs();
		this.machines = p.getNumMachines();
		this.initPheromone = initPheromone;
		this.decay = decay;
		this.evap = evap;
		pheromone = new double[jobs*machines][jobs*machines];
		int[][] proc = p.getProcMatrix();
		for (int i = 0; i < pheromone[0].length; i++) {
			for (int j = 0; j < pheromone[1].length; j++) {
				pheromone[i][j] = initPheromone;
			}
		}
	}
	
	public void updatePheromoneLocal(int from, int to){
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
	
	/*parameters formatted such that operations are
	 * series of unique increasing integers*/
	public double getPheromone(int from, int to){
		return this.pheromone[from][to];
	}
	
	public static void main(String[] args) throws IOException {
		Problem p = ProblemCreator.create("5.txt");
		AntGraph a = new AntGraph(p, 0.01, 0.3, 0.4);
	}
	
	public static class Ant{
		private boolean[] tabu;
		private List<Integer> open;
		private int[] path;
		
		public Ant(int machines, int jobs){
			this.path = new int[machines*jobs];
			this.tabu = new boolean[machines*jobs]; /*defaults to false*/
			this.open = new ArrayList<Integer>();
		}
		
		
		
		
	}
}
