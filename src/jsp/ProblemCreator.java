package jsp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProblemCreator {
	public ProblemCreator(){}
	
	public static Problem create(String filename) throws IOException{
		List<String[]> strings = new ArrayList<String[]>();
		List<int[]> values = new ArrayList<int[]>();
		int machines;
		int jobs;
		FileReader fr = new FileReader(new File(filename));
		BufferedReader b = new BufferedReader(fr);
		String[] data = b.readLine().trim().split("\\s+");
		machines = Integer.parseInt(data[1]);
		jobs = Integer.parseInt(data[0]);
		String line;
		while((line = b.readLine())!= null){
			if(line.length()!=0){
				data = line.trim().split("\\s+");
				strings.add(data);
			}
		}
		for (String[] string : strings) {
			int[] val = new int[machines*2];
			for (int i = 0; i < string.length; i++) {
				val[i] = Integer.parseInt(string[i]);
			}
			values.add(val);
		}
		return new Problem(machines, jobs, values);
	}
	
	public static class Problem{
		private int machines;
		private int numjobs;
		private ArrayList<Job> jobs;
		private int[][] process;
		private int[][] machine;
		
		public Problem(int machines, int numjobs, List<int[]> jobList){
			this.machines = machines;
			this.numjobs = numjobs;
			this.jobs = new ArrayList<Job>();
			for (int[] j : jobList) {
				jobs.add(new Job(j));
			}
			this.process = procMatrix();
			this.machine = machMatrix();
		}
		
		private int[][] procMatrix(){
			int[][] matrix = new int[this.jobs.size()][this.jobs.get(0).getSequence().size()];
			for (Job j : this.jobs) {
				for (int[] val : j.getSequence()) {
					matrix[jobs.indexOf(j)][j.getSequence().indexOf(val)] = val[1];
				}
			}
			return matrix;
		}
		
		private int[][] machMatrix(){
			int[][] matrix = new int[this.jobs.size()][this.jobs.get(0).getSequence().size()];
			for (Job j : this.jobs) {
				for (int[] val : j.getSequence()) {
					matrix[jobs.indexOf(j)][j.getSequence().indexOf(val)] = val[0];
				}
			}
			return matrix;
		}
		
		public List<Job> getJobs(){
			return this.jobs;
		}
		
		public int[][] getProcMatrix(){
			return this.process;
		}
		
		public int[][] getMachMatrix(){
			return this.machine;
		}
		
		public int getNumJobs(){
			return this.numjobs;
		}
		
		public int getNumMachines(){
			return this.machines;
		}
	}
	
	public static class Job{
		/*
		indexed list where index corresponds to operation number
		item[0] is the machine for the operation
		item[1] is the processing time for the operation */
		private List<int[]> sequence;
		private int due;
		
		public Job(int[] values){
			this.sequence = new ArrayList<int[]>();
			due = 0;
			int[] j = new int[2];
			for (int i = 0; i < values.length; i++) {
				if(i%2==0){
					j = new int[2];
					j[0] = values[i];
				}else{
					j[1] = values[i];
					due+=values[i];
					sequence.add(j);
				}
			}
		}
		
		
		
		//treat as hard coded since will be set during runtime at demo
		//i.e. do not calculate when constructing schedules, use getDue()
		public void setDue(int dueTime){
			this.due = dueTime;
		}
		public int getDue(){
			return this.due;
		}
		
		public List<int[]> getSequence(){
			return this.sequence;
		}
		
		@Override
		public String toString() {
			String out = "Job\n";
			for (int[] is : sequence) {
				out+="Op:"+sequence.indexOf(is)+"\tMchn:"+is[0]+"\tTime:"+is[1]+"\n";
			}
			out+="Due: "+this.due+"\n";
			return out;
		}
	}
	
	public static void main(String[] args) throws Exception {
		Problem p = ProblemCreator.create("5.txt");
		System.out.println(p.getJobs().get(0));
		System.out.println(Arrays.toString(p.machine[0]));
		System.out.println(Arrays.toString(p.process[0]));
	}
}
