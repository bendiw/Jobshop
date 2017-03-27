package jsp;

public class Scheduler {
	int[][] solution;
	int[][] process;
	int[][] machine;
	int[] dueTime;
	int jobs;
	int machines;
	int makespan;
	int tardiness;
	
	public Scheduler(int[][] process, int[][] machine) {
		this.process = process;
		this.machine = machine;
		this.jobs = process.length;
		this.machines = process[0].length;
		this.dueTime = new int[this.jobs];
		for (int i = 0; i < jobs; i++) {
			for (int j = 0; j < machines; j++) {
				dueTime[i] += process[i][j];
			}
		}
	}
	
	public void buildSchedule(int[] chromosome) {
		solution = new int[machines][jobs];
		int[] nextTask = new int[jobs];
		int[] jobStart = new int[jobs];
		for (int i = 0; i < jobStart.length; i++) {
			nextTask[i] = 1;
		}
		int[] nextSol = new int[machines];
		int[] machStart = new int[machines];
		for (int i = 0; i < jobStart.length; i++) {
			nextSol[i] = 1;
		}
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
		makespanFitness(jobStart);
		tardinessFitness(jobStart);
	}
	
	public void makespanFitness(int[] completionTimes) {
		int max = 0;
		for (int i : completionTimes) {
			if (i > max)
				max = i;
		}
		makespan = max;
	}
	
	public void tardinessFitness(int[] completionTimes) {
		int sum = 0;
		for (int i = 0; i < jobs; i++) {
			sum += Math.max(completionTimes[i] - dueTime[i], 0);
		}
		tardiness = sum;
	}
}
