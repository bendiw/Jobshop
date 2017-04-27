package swarm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import utils.Utils;

import org.apache.commons.lang3.ArrayUtils;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import jsp.ProblemCreator;
import jsp.ProblemCreator.Problem;
import jsp.Scheduler;

public class PSO {
	private Problem p;
	private Random r;
	private double pSwap, pInsert, pInvert;
	private ArrayList<Integer> indexes;
	
	public PSO(Problem p, double pSwap, double pInsert, double pInvert){
		this.p = p;
		this.r = new Random();
		this.pSwap = pSwap;
		this.pInsert = pInsert;
		this.pInvert = pInvert;
		this.indexes = new ArrayList<>();
		for (int i = 0; i < p.getNumJobs()*p.getNumMachines(); i++) {
			this.indexes.add(i);
		}
		System.out.println(indexes.size());
	}
	
	public void run(int iterations, int swarmSize, double maxInertia, double minInertia, double MIEprob){
		Particle[] swarm = new Particle[swarmSize];
		double vLim = p.getNumJobs()*p.getNumMachines()*0.1;
		for (int i = 0; i < swarmSize; i++) {
			Particle prt = new Particle(r,p.getNumJobs(),p.getNumMachines(),1,1,vLim,-vLim); //fix values
			swarm[i] = prt;
		}
		
		double inertia = maxInertia;
		int globalBest = Integer.MAX_VALUE;
		double[] bestPos= null;
		int[] bestChromo = null;
		boolean changed;
		for (int i = 0; i < iterations; i++) {
			changed = false;
			//calc fitness
			for (int j = 0; j < swarm.length; j++) {
				int fit = calcFitness(swarm[j].getPosition());
				swarm[j].updateFitness(fit);
				double mie = r.nextDouble();
				if(mie <= MIEprob){
					MIE(swarm[j], fit);
				}
				if(fit < globalBest){
					changed = true;
					globalBest = fit;
					bestPos = swarm[j].getPosition();
					bestChromo = Utils.getJobArray(swarm[j].getPosition(), p.getNumJobs());
//					bestChromo = swarm[j].getJobArray();
				}
			}
			
			inertia = maxInertia - (i*(maxInertia-minInertia)/(double)iterations);
			//move particles
			for (int j = 0; j < swarm.length; j++) {
				swarm[j].move(bestPos, inertia);
			}
			if(changed){
				System.out.println("Iteration: "+i+"\tGlobal best: "+globalBest);
			}
		}
		int[] schedule = Scheduler.buildSchedule(bestChromo, p);
//		Scheduler.buildScheduleGantt(bestChromo, p);
		System.out.println("Best makespan: "+globalBest);
	}
	
	public int calcFitness(double[] position){
		int[] schedule = Scheduler.buildSchedule(Utils.getJobArray(position,p.getNumJobs()), this.p);
		return Scheduler.makespanFitness(schedule);
	}
	
	
	private void MIE(Particle prt, int fitness){
		double[] position = prt.getPosition().clone();
		int[] jobArray = Utils.getJobArray(position, p.getNumJobs());
		
		Collections.shuffle(indexes);
		double q = r.nextDouble();
		if(q<=pSwap){
			position = swap(position, jobArray, indexes);
		}
		else if(q>pSwap && q <=pInsert+pSwap){
			position =  insert(position, jobArray, indexes);
		}
//		else if(q>pInsert+pSwap && q<pInsert+pSwap+pInvert)
//			return invert(position, jobArray, indexes);
//		else
//			return position; //longMov(position, jobArray, indexes);
		int newFitness = calcFitness(position);
		if(newFitness <= fitness){
			prt.setPosition(position);
			prt.updateFitness(newFitness);
		}
	}
	
	private double[] swap(double[] position, int[] jobArray, List<Integer> indexes){
		boolean swapped = false;
		int index = indexes.get(0);
		int count = 1;
		while(!swapped){
			int swapIndex = indexes.get(count);
			count++;
			if(jobArray[index]!=jobArray[swapIndex]){
				double temp = new Double(position[index]);
				position[index] = new Double(position[swapIndex]);
				position[swapIndex] = temp;
				swapped = true;
			}
		}
		return position;
	}
	
	private double[] insert(double[] position, int[] jobArray, List<Integer> indexes){
		ArrayList<Double> temp = new ArrayList<Double>();
		for (int i = 0; i < position.length; i++) {
			temp.add(position[i]);
		}
		int index = indexes.get(0);
		int insIndex = indexes.get(1);
		temp.remove(index);
		temp.add(insIndex, position[index]);
		for (int i = 0; i < position.length; i++) {
			position[i] = temp.get(i);
		}
		return position;
	}
	
//	private double[] invert(double[] position, int[] jobArray){
//		
//	}
//	
//	private double[] longMov(double[] position, int[] jobArray){
//		
//	}
	
	public static void main(String[] args) throws IOException {
		Problem p = ProblemCreator.create("1.txt");
		PSO pso = new PSO(p, 0.5,0.5,0);
		pso.run(10000, 150, 1.4, 0.4, 0.03);
	}
	
	public static class Particle{
		private double[] velocity;
		private double[] position;
		private Random r;
		private int jobs;
		private int bestFitness;
		private double[] bestPosition;
		private int currFitness;
		private double selfLearn;
		private double socialLearn;
		private double vMin;
		private double vMax;
		private Problem p;
		
		public Particle(Random r, int jobs, int machines, double selfLearn, double socialLearn, double vMax, double vMin){
			this.r = r;
			this.jobs = jobs;
			this.vMin = vMin;
			this.vMax = vMax;
			this.selfLearn = selfLearn;
			this.socialLearn = socialLearn;
			this.bestFitness = Integer.MAX_VALUE;
			velocity = new double[jobs*machines];
			position = new double[jobs*machines];
			for (int i = 0; i < position.length; i++) {
				double rand = r.nextDouble();
				if(rand<0.5)
					velocity[i] = r.nextDouble()*vMin;
				else
					velocity[i] = r.nextDouble()*vMax;
					
				position[i] = r.nextDouble()*jobs*machines;
			}
		}
		
		public double[] getPosition(){
			return this.position;
		}
		
		public void setPosition(double[] position){
			this.position = position;
		}
		

		public void updateFitness(int fitness){
			this.currFitness = fitness;
			if(fitness < bestFitness){
				this.bestFitness = fitness;
				this.bestPosition = this.position;
			}
		}
		
		
//		public int[] getJobArray(double[] position, int jobs){
//			int[] jobArray = new int[position.length];
//			double[] posCopy = position.clone();
//			int[] indexArray = new int[position.length];
//			Arrays.sort(posCopy);
////			System.out.println(Arrays.toString(this.position));
////			System.out.println(Arrays.toString(posCopy));
//			for (int i = 0; i < jobArray.length; i++) {
//				int index = ArrayUtils.indexOf(posCopy,position[i]);
//				indexArray[i] = index;
//				jobArray[i] = index%(jobs);
//			}
////			System.out.println(Arrays.toString(indexArray));
////			System.out.println(Arrays.toString(jobArray));
//			return jobArray;
//		}
		
		public void move(double[] globalBest, double inertia){
			for (int i = 0; i < velocity.length; i++) {
				velocity[i] = velocity[i]*inertia + (this.selfLearn*r.nextDouble()*(this.bestPosition[i]-this.position[i]))+(this.socialLearn*r.nextDouble()*(globalBest[i]-this.position[i]));
				if(velocity[i]<vMin){
					velocity[i] = vMin;
				}else if(velocity[i] > vMax){
					velocity[i] = vMax;
				}
				position[i] += velocity[i];
			}
		}
	}
}
