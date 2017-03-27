package ants;

import java.util.List;

import jsp.ProblemCreator.Job;
import jsp.ProblemCreator.Problem;

public class AntGraph {
	private double[][] pheromone;
	private int[][] process;
	private int jobs;
	private int machines;
	
	
	public AntGraph(Problem p){
		this.jobs = p.getNumJobs();
		this.machines = p.getNumMachines();
		pheromone = 
	}
}
