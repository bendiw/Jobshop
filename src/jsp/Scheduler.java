package jsp;

import jsp.ProblemCreator.Problem;

public class Scheduler {

	
	
	public static int[] buildSchedule(int[] chromosome, Problem p) {
		int[][] process = p.getProcMatrix();
		int[][] machine = p.getMachMatrix();
		int jobs = p.getNumJobs();
		int machines = p.getNumMachines();
		int[][] solution = new int[machines][jobs];
		int[] nextTask = new int[jobs];
		int[] jobStart = new int[jobs];
//		for (int i = 0; i < jobStart.length; i++) {
//			nextTask[i] = 1;
//		}
		int[] nextSol = new int[machines];
		int[] machStart = new int[machines];
//		for (int i = 0; i < jobStart.length; i++) {
//			nextSol[i] = 1;
//		}
		for (int k = 0; k < chromosome.length; k++) {
			int i = chromosome[k];
			int j = machine[i][nextTask[i]];
			solution[j][nextSol[j]] = i;
			nextTask[i] ++;
			nextSol[j] ++;
			int start = Math.max(jobStart[i], machStart[j]);
			jobStart[i] = start + process[i][j];
			machStart[j] = start + process[i][j];
		}
		return jobStart;
	}
	
	public static int makespanFitness(int[] completionTimes) {
		int max = 0;
		for (int i : completionTimes) {
			if (i > max)
				max = i;
		}
		return max;
	}
	
	public static int tardinessFitness(int[] completionTimes, Problem p) {
		int sum = 0;
		int[] dueTime = new int[p.getNumJobs()];
		for (int i = 0; i < p.getNumJobs(); i++) {
			for (int j = 0; j < p.getNumMachines(); j++) {
				int[][] process = p.getProcMatrix();
				dueTime[i] += process[i][j];
			}
		}
		for (int i = 0; i < p.getNumJobs(); i++) {
			sum += Math.max(completionTimes[i] - dueTime[i], 0);
		}
		return sum;
	}
}
