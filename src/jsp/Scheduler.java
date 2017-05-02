package jsp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import jsp.ProblemCreator.Problem;

public class Scheduler {
	
	public static int[] giffThomp(int[]chromosome, Problem p) {
		int[][] machine = p.getMachMatrix();
		int jobs = p.getNumJobs();
		int machines = p.getNumMachines();
		int[] jobStart = new int[jobs];
		int[] machStart = new int[machines];
		
		int t = 0;
		int[] P = new int[chromosome.length];
		List<Integer> S = new ArrayList<Integer>();
		for (int i = 0; i < jobs; i++) {
			S.add(i*machines);
		}
		
		while (! S.isEmpty()) {
			int b = Integer.MAX_VALUE;
			int M = 0;
			int finish = 0;
			int operation = 0;
			for (Integer op : S) {
				int j = getJob(op, machines);
				int jt = getJobTask(op, machines);
				int m = machine[j][jt];
				finish = Math.max(jobStart[j], machStart[m]) + p.getJobs().get(j).getProcessTime(m);
				if (finish < b) {
					M = m;
					b = finish;
					operation = op;
				} else if (finish == b) {
					for (Integer chromOp : chromosome) {
						if (chromOp == op) {
							M = m;
							b = finish;
							operation = op;
							break;
						} else if (chromOp == operation){
							break;
						}
					}
				}
			}
			int chosenOp = 0;
			int bestStart = Integer.MAX_VALUE;
			for (Integer op : S) {
				int j = getJob(op, machines);
				int jt = getJobTask(op, machines);
				int m = machine[j][jt];
				int start  = Math.max(jobStart[j], machStart[m]);
				if (m == M && start < b && start < bestStart) {
					chosenOp = op;
					int processtime = p.getJobs().get(j).getProcessTime(m);
					jobStart[j] += processtime;
					machStart[m] += processtime;
					bestStart = start;
				} else if (m == M && start < b && start == bestStart) {
					for (Integer chromOp : chromosome) {
						if (chromOp == op) {
							chosenOp = op;
							int processtime = p.getJobs().get(j).getProcessTime(m);
							jobStart[j] += processtime;
							machStart[m] += processtime;
							bestStart = start;
							break;
						} else if (chromOp == operation){
							break;
						}
					}
				}
			}
			P[t] = chosenOp;
			S.remove(S.indexOf(chosenOp));
			if (getJobTask(chosenOp, machines) < machines-1)
				S.add(chosenOp+1);
			t ++;
		}
		return P;
	}
	
	private static int getJob(int op, int machines) {
		return Math.floorDiv(op, machines);
	}
	
	private static int getJobTask(int op, int machines) {
		return op % machines;
	}
	
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
	
	public static void buildScheduleGantt(int[] chromosome, Problem p) throws IOException {
		int[][] process = p.getProcMatrix();
		int[][] machine = p.getMachMatrix();
		int jobs = p.getNumJobs();
		int machines = p.getNumMachines();
		int[][] schedule = new int[machines][jobs];
		int[][] startTime = new int[machines][jobs];
		int[][] endTime = new int[machines][jobs];
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
			endTime[j][sNext[j]] = start + p.getJobs().get(i).getProcessTime(j);
			jobStart[i] = start + process[i][tNext[i]];
			machStart[j] = start + process[i][tNext[i]];
			tNext[i] ++;
			sNext[j] ++;
		}
		
		String scheduleString = "";
		String startTimeString = "";
		String endTimeString = "";
		String numJobs = "" + schedule[0].length;
		String numMachs = "" + schedule.length;
		
		for (int i = 0; i < schedule.length; i++) {
			for (int j = 0; j < schedule[0].length; j++) {
				scheduleString += schedule[i][j] + ",";
				startTimeString += startTime[i][j] + ",";
				endTimeString += endTime[i][j] + ",";
			}
		}
		
//		for (int i = 0; i < process.length; i++) {
//			for (int j = 0; j < process[0].length; j++) {
//				processString += process[i][j] + ",";
//			}
//		}
		System.out.println(numJobs);
		System.out.println(numMachs);
		System.out.println(scheduleString);
		System.out.println(startTimeString);
//		System.out.println(processString);
		
		
		String[] cmd = {
				"python",
//				"C:\\Users\\agmal_000\\git\\Jobshop\\gantt.py",
				"C:\\Users\\Bendik\\git\\Jobshop\\gantt.py",
				numJobs,
				numMachs,
				scheduleString.substring(0, scheduleString.length()-1),
				startTimeString.substring(0,startTimeString.length()-1),
				endTimeString.substring(0, endTimeString.length()-1),
		};
		Runtime.getRuntime().exec(cmd);

		
//		Gantt gantt = new Gantt("JSP", schedule, startTime, process, machine, p);
//		gantt.pack();
//		RefineryUtilities.centerFrameOnScreen(gantt);
//		gantt.setVisible(true);

	}
	

	public static List<int[]> buildScheduleBee(int[] chromosome, Problem p) {
		int[][] process = p.getProcMatrix();
		int[][] machine = p.getMachMatrix();
		int jobs = p.getNumJobs();
		int machines = p.getNumMachines();
		int[][] schedule = new int[machines][jobs];
		int[][] startTime = new int[machines][jobs];
		int[][] endTime = new int[machines][jobs];
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
			endTime[j][sNext[j]] = start + p.getJobs().get(i).getProcessTime(j);
			jobStart[i] = start + process[i][tNext[i]];
			machStart[j] = start + process[i][tNext[i]];
			tNext[i] ++;
			sNext[j] ++;
		}
		List<List<Integer>> criticalPath = getCriticalPath(p, schedule, startTime, endTime);
		List<int[]> moves = new ArrayList<int[]>();
		for (int i = 0; i < criticalPath.size(); i++) {
			List<Integer> block = criticalPath.get(i);
			int[] move = new int[2];
			if (i != 0 && block.size() > 1) {
				move[0] = block.get(0);
				move[1] = block.get(1);
				moves.add(0, move);
			}
			if (! (move[0]+move[1] > 0 && block.size() == 2)) {
				if (i != criticalPath.size()-1 && block.size() > 1) {
					int size = block.size();
					move[0] = block.get(size-2);
					move[1] = block.get(size-1);
					moves.add(0, move);
				}
			}
		}
		return moves;
	}
	
	private static List<List<Integer>> getCriticalPath(Problem p, int[][] schedule, int[][] startTime, int[][] endTime) {
		int latest = 0;
		int latestJob = 0;
		int jobs = p.getNumJobs();
		int machines = p.getNumMachines();
		int[] task = new int[jobs];
		for (int i = 0; i < task.length; i++) {
			task[i] = machines-1;
		}
		
		for (int i = 0; i < machines; i++) {
			if (latest < endTime[i][jobs-1]) {
				latest = endTime[i][jobs-1];
				latestJob = schedule[i][jobs-1];
			}
		}
		
		int[][] machineSeq = p.getMachMatrix();
		int machine = machineSeq[latestJob][task[latestJob]];
		List<List<Integer>> criticalPath = new ArrayList<List<Integer>>();
		int scheduleTask = jobs - 1;
		
		while (true) {
			List<Integer> block = new ArrayList<Integer>();
			while (true) {
				block.add(0, getOpNr(latestJob, machines, task[latestJob]));
				task[latestJob] --;
				if(scheduleTask == 0){
					criticalPath.add(0, block);
					break;
				}
//				System.out.println(scheduleTask);
				if (endTime[machine][scheduleTask-1]  != startTime[machine][scheduleTask]) {
					criticalPath.add(0, block);
					break;
				} else {
					latestJob = schedule[machine][scheduleTask-1];
					scheduleTask --;
				}
			}
//			if(scheduleTask == 0){
//				break;
//			}
			boolean foundNew = false;
			for (int i = 0; i < endTime.length; i++) {
				if (i != machine) {
					for (int j = 0; j < endTime[0].length; j++) {
						if (schedule[i][j] == latestJob && endTime[i][j] == startTime[machine][scheduleTask])  {
							scheduleTask = j;
							machine = i;
							foundNew = true;
							break;
						}
					}
				}
				if (foundNew) {
					break;
				}
			}
			if (! foundNew) {
				break;
			}
		}
		return criticalPath;
	}
	
	private static int[] buildScheduleAttract(int[] chromosome, Problem p, int[] move) {
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
		int job1 = Math.floorDiv(move[0], machines);
		int job2 = Math.floorDiv(move[1], machines);
		int task1 = move[0]%machines;
		int task2 = move[1]%machines;
		int nextMachine = machine[job1][task1];
		int[] sTimes = new int[3];
		for (int i = 0; i < jobs-1; i++) {
			if (schedule[nextMachine][i] == job2) {
				sTimes[0] = startTime[nextMachine][i+1];
				break;
			}
		}
		if (task1 < machines-1) {
			nextMachine = machine[job1][task1+1];
			for (int i = 0; i < jobs; i++) {
				if (schedule[nextMachine][i] == job1) {
					sTimes[1] = startTime[nextMachine][i];
					break;
				}
			}
		}
		if (task2 < machines-1) {
			nextMachine = machine[job2][task2+1];
			for (int i = 0; i < jobs; i++) {
				if (schedule[nextMachine][i] == job2) {
					sTimes[2] = startTime[nextMachine][i];
					break;
				}
			}
		}
		return sTimes;
		
	}
	
	private static int[] makeChrom(int[] operations, Problem p) {
		int machines = p.getNumMachines();
		int[] chrom = new int[operations.length];
		for (int i = 0; i < operations.length; i++) {
			chrom[i] = Math.floorDiv(operations[i], machines);
		}
		return chrom;
	}
	
	private static int getOpNr(int job, int machines, int task) {
		return job*machines + task;
	}
	
	public static List<int[]> getMoves(int[] operations, Problem p) {
		int[] chrom = makeChrom(operations, p);
		return buildScheduleBee(chrom, p);
	}
	

	public static double[] getAttract(ArrayList<int[]> moves, Problem p, int[] oper) {
		double[] attract = new double[moves.size()];
		int[] operations = oper.clone();
		int counter = 0;
		for (int[] move : moves) {
			int[] oldT = buildScheduleAttract(makeChrom(operations, p), p, move);
			for (int i = 0; i < operations.length; i++) {
				if (operations[i] == move[0])
					operations[i] = move[1];
				else if (operations[i] == move[1])
					operations[i] = move[0];
			}
			int[] newT = buildScheduleAttract(makeChrom(operations, p), p, move);
			double sum = 0;
//			System.out.println(Arrays.toString(oldT));
//			System.out.println(Arrays.toString(newT));
			for (int i = 0; i < 3; i++) {
				sum += (newT[i]-oldT[i]);
			}
//			System.out.println(sum);
			if(sum <=0){
				attract[counter] = -sum;
			}else{
				attract[counter] = 1/sum;
			}
//			System.out.println(attract[counter]+"\n");
			counter ++;
		}
		return attract;
	}
}
