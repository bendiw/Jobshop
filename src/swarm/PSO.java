package swarm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import utils.Utils;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.axis.SymbolicTickUnit;

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
	
	public void run(int iterations, int swarmSize, double maxInertia, double minInertia, double MIEprob, double endTemp, double cooling) throws IOException{

		//init fields
		Particle[] swarm = new Particle[swarmSize];
		double vLim = p.getNumJobs()*p.getNumMachines()*0.1;
		double inertia = maxInertia;
		int globalBest = Integer.MAX_VALUE;
		double[] bestPos= null;
		int[] bestGiff = null;
		double[] bestChromo = null;
		boolean changed;
		
		//init particles
		for (int i = 0; i < swarmSize; i++) {
			Particle prt = new Particle(r,p.getNumJobs(),p.getNumMachines(), 2.0, 2.0, vLim, -vLim);
			swarm[i] = prt;
		}
		
		//main loop
		for (int i = 0; i < iterations; i++) {
			int iterBest = Integer.MAX_VALUE;
			double sumSpan = 0;
			changed = false;
			for (int j = 0; j < swarm.length; j++) {
				int[] oldChrom = Utils.getJobArray(swarm[j].getPosition(),p.getNumJobs(), false);
				int[] giffChrom = Scheduler.giffThomp(oldChrom, p);
				int[] jobGiff = Utils.normalizeArray(giffChrom, p.getNumMachines(), p.getNumJobs());
				int[] giffSchedule = Scheduler.buildSchedule(jobGiff, this.p);
				int fit = Scheduler.makespanFitness(giffSchedule);
				
				double mie = r.nextDouble();
				if(MIEprob > 0 ){
					if(mie <= MIEprob|| fit <= globalBest){
//					System.out.println("fit before MIE: "+fit);
						double initTemp = (fit-globalBest)+10;
						fit = MIE(swarm[j], fit, initTemp, endTemp, cooling);
//					System.out.println("fit after MIE: "+fit);
						oldChrom = Utils.getJobArray(swarm[j].getPosition(),p.getNumJobs(), false);
						giffChrom = Scheduler.giffThomp(oldChrom, p);
					}
				}
				swarm[j].updateFitness(fit);
				sumSpan+= fit;
				if(fit<iterBest){
					iterBest = fit;
				}

				if(fit < globalBest){
					changed = true;
					globalBest = fit;
					bestPos = Arrays.copyOf(swarm[j].getPosition(), swarm[j].getPosition().length);
					bestGiff = giffChrom;
					bestChromo = bestPos;
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
			if (i % 10 == 0){
				sumSpan = sumSpan/swarm.length;
//				System.out.println("best: "+Arrays.toString(bestPos));
				System.out.println("Iteration: "+i+"\t Global best: "+globalBest+"\t Iter best: "+iterBest+"\t Iter avg: "+sumSpan);
			}
		}
		System.out.println("final best pos: "+Arrays.toString(bestPos));
//		int[] normChrom = Utils.getJobArray(bestPos, p.getNumJobs(), false);
//		System.out.println(Arrays.toString(normChrom));
//		int[] giffChrom = Scheduler.giffThomp(normChrom, p);
//		giffChrom = Utils.normalizeArray(giffChrom, p.getNumMachines(), p.getNumJobs());
		int[] giffChrom = Utils.normalizeArray(bestGiff, p.getNumMachines(), p.getNumJobs());
		System.out.println(Arrays.toString(giffChrom));
		int[] giffSchedule = Scheduler.buildSchedule(giffChrom, p);
		int finalMakeSpan = Scheduler.makespanFitness(giffSchedule);
		System.out.println("final makespan: "+finalMakeSpan);
		Scheduler.buildScheduleGantt(giffChrom, p);
		System.out.println("Best makespan: "+globalBest);
	}
	
//	public int calcFitness(double[] position) throws IOException{
//		int[] oldChrom = Utils.getJobArray(position,p.getNumJobs(), false);
//		int[] giffChrom = Scheduler.giffThomp(oldChrom, p);
//		int[] jobGiff = Utils.normalizeArray(giffChrom, p.getNumMachines(), p.getNumJobs());
//		int[] giffSchedule = Scheduler.buildSchedule(jobGiff, this.p);
////		int[] normSchedule = Scheduler.buildSchedule(oldChrom, p);
//		int giffSpan = Scheduler.makespanFitness(giffSchedule);
////		int normSpan = Scheduler.makespanFitness(normSchedule);
//		
////		int makeSpan = Math.min(giffSpan, normSpan);
////		return makeSpan;
//		return giffSpan;
//	}
	
	public int calcOrigFitness(double[] position){
		int[] oldChrom = Utils.getJobArray(position,p.getNumJobs(), true);
		int[] normSchedule = Scheduler.buildSchedule(oldChrom, p);
		int normSpan = Scheduler.makespanFitness(normSchedule);
		return normSpan;
	}
	
	
	private int MIE(Particle prt, int fitness, double startTemp, double endTemp, double cooling) throws IOException{
		int initFit = fitness;
//		int initFit = calcOrigFitness(prt.getPosition());
		double temperature = startTemp;
		while(temperature > endTemp){
			double[] position = Arrays.copyOf(prt.getPosition(), prt.getPosition().length);
			int[] jobArray = Utils.getJobArray(position, p.getNumJobs(), true);
			Collections.shuffle(indexes);
			double q = r.nextDouble();
			if(q<=pSwap){
				swap(position, jobArray);
			}
			else if(q>pSwap && q <=pInsert+pSwap){
				insert(position);
			}
			else if(q>pInsert+pSwap && q<=pInsert+pSwap+pInvert)
				invert(position);
			else
				longMov(position);
//			int newFitness = calcFitness(position); //using giff
//			int newFitness = calcOrigFitness(position);
			int[] oldChrom = Utils.getJobArray(position,p.getNumJobs(), false);
			int[] giffChrom = Scheduler.giffThomp(oldChrom, p);
			int[] jobGiff = Utils.normalizeArray(giffChrom, p.getNumMachines(), p.getNumJobs());
			int[] giffSchedule = Scheduler.buildSchedule(jobGiff, this.p);
			int newFitness = Scheduler.makespanFitness(giffSchedule);
			if(newFitness <= initFit){
				initFit = newFitness;
				prt.setPosition(position);
				prt.updateFitness(newFitness);
			}else{
				double delta = newFitness-initFit;
				double rand = r.nextDouble();
				double accept = Math.exp(-(delta/temperature))*1;
				if(rand < Math.min(1, accept)){
					initFit = newFitness;
					prt.setPosition(position);
					prt.updateFitness(newFitness);
				}
			}
			temperature = temperature*cooling;
		}
		
		return initFit;
	}
	
	private double[] swap(double[] position, int[] jobArray){
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
	
	private double[] insert(double[] position){
		ArrayList<Double> temp = new ArrayList<Double>();
		for (int i = 0; i < position.length; i++) {
			temp.add(position[i]);
		}
		int index = indexes.get(0);
		int count = 1;
		int insIndex = indexes.get(count);
		while(Math.abs(insIndex-index)==1){
			count++;
			insIndex = indexes.get(count);
		}
//		System.out.println("ind: "+index);
//		System.out.println("ins: "+insIndex);
		temp.add(insIndex+1, position[index]);
		if(insIndex < index){
			index+=1;
		}
		temp.remove(index);
		for (int i = 0; i < position.length; i++) {
			position[i] = temp.get(i);
		}
		return position;
	}
	
	private double[] invert(double[] position){
		int indexOne = indexes.get(0);
		int indexTwo = indexes.get(1);
		int firstIndex = Math.min(indexOne, indexTwo);
		int secondIndex = Math.max(indexOne,indexTwo);
		ArrayUtils.reverse(position, firstIndex, secondIndex+1);
		return position;
	}
	
	private double[] longMov(double[] position){
		ArrayList<Double> tempList = new ArrayList<Double>();
		ArrayList<Double> tempList2 = new ArrayList<Double>();
		for (int i = 0; i < position.length; i++) {
			tempList.add(position[i]);
		}
		int indexOne = indexes.get(0);
		int indexTwo = indexes.get(1);
		int firstIndex = Math.min(indexOne, indexTwo);
		int secondIndex = Math.max(indexOne,indexTwo);
//		System.out.println("p: "+firstIndex);
//		System.out.println("q:"+secondIndex);
		for (int i = firstIndex; i < secondIndex+1; i++) {
			tempList2.add(tempList.remove(firstIndex));
		}
		int insert = 0;
		if(tempList.size()!=0){
			while(insert == firstIndex){
				insert = r.nextInt(tempList.size()+1);
			}
		}
		tempList.addAll(insert, tempList2);
		for (int i = 0; i < position.length; i++) {
			position[i] = tempList.get(i);
		}
		return position;
	}
	
	public static void main(String[] args) throws IOException {
		Problem p = ProblemCreator.create("3.txt");
		PSO pso = new PSO(p, 0.4,0.4,0.1);
		for (int i = 0; i < 1; i++) {
			pso.run(1000, 30, 1.4, 0.4, 0.02, 0.1, 0.97);
		}
	}
	
	public static class Particle{
		private double[] velocity;
		private double[] position;
		private Random r;
		private int jobs;
		private int machines;
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
			this.machines = machines;
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
			this.position = Arrays.copyOf(position, position.length);
		}
		
		public double[] getVelocity(){
			return this.velocity;
		}
		

		public void updateFitness(int fitness){
			this.currFitness = new Integer(fitness);
			if(fitness < bestFitness){
				this.bestFitness = new Integer(fitness);
				this.bestPosition = Arrays.copyOf(this.position, this.position.length);
			}
		}
		
		
		public void move(double[] globalBest, double inertia){
			for (int i = 0; i < velocity.length; i++) {
				velocity[i] = velocity[i]*inertia + (this.selfLearn*r.nextDouble()*(this.bestPosition[i]-this.position[i]))+(this.socialLearn*r.nextDouble()*(globalBest[i]-this.position[i]));
				if(velocity[i]<vMin){
					velocity[i] = vMin;
				}else if(velocity[i] > vMax){
					velocity[i] = vMax;
				}
				position[i] += velocity[i];
//				int max = jobs*machines;
//				if(position[i]>max)
//					position[i] = max;
			}
		}
	}
}
