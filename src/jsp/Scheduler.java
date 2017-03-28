package jsp;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import jsp.ProblemCreator.Problem;

public class Scheduler {
	
	public static int[] buildSchedule(int[] chromosome, Problem p) {
		int[][] process = p.getProcMatrix();
		int[][] machine = p.getMachMatrix();
		int jobs = p.getNumJobs();
		int machines = p.getNumMachines();
		int[][] schedule = new int[machines][jobs];
		int[] tNext = new int[jobs];
		int[] jobStart = new int[jobs];
		int[] sNext = new int[machines];
		int[] machStart = new int[machines];

		for (int k = 0; k < chromosome.length; k++) {
			int i = chromosome[k];
			int j = machine[i][tNext[i]];
			schedule[j][sNext[j]] = i;
			int start = Math.max(jobStart[i], machStart[j]);
			jobStart[i] = start + process[i][tNext[i]];
			machStart[j] = start + process[i][tNext[i]];
			tNext[i] ++;
			sNext[j] ++;
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
	
	public static void buildScheduleGantt(int[] chromosome, Problem p) {
		int[][] process = p.getProcMatrix();
		int[][] machine = p.getMachMatrix();
		int jobs = p.getNumJobs();
		int machines = p.getNumMachines();
		int[][] schedule = new int[machines][jobs];
		int[][] startTime = new int[machines][jobs];
		int[] tNext = new int[jobs];
		int[] jobStart = new int[jobs];
		int[] sNext = new int[machines];
		int[] machStart = new int[machines];

		for (int k = 0; k < chromosome.length; k++) {
			int i = chromosome[k];
			int j = machine[i][tNext[i]];
			schedule[j][sNext[j]] = i;
			int start = Math.max(jobStart[i], machStart[j]);
			startTime[j][sNext[j]] = start;
			jobStart[i] = start + process[i][tNext[i]];
			machStart[j] = start + process[i][tNext[i]];
			tNext[i] ++;
			sNext[j] ++;
		}
		Gantt gantt = new Gantt("JSP", schedule, startTime, process, machine, p);
		gantt.pack();
		RefineryUtilities.centerFrameOnScreen(gantt);
		gantt.setVisible(true);
		for (int i = 0; i < schedule.length; i++) {
			for (int j = 0; j < schedule[i].length; j++) {
				System.out.print(schedule[i][j]+", ");
			}
			System.out.println("");
		}
		for (int i = 0; i < schedule.length; i++) {
			for (int j = 0; j < schedule[i].length; j++) {
				System.out.print(startTime[i][j]+", ");
			}
			System.out.println("");
		}
	}
	
}
