package jsp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jsp.ProblemCreator.Problem;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;

public class Gantt extends ApplicationFrame{

	public Gantt(String title, int[][] schedule, int[][] startTime, int[][] process, int[][] machine, Problem problem) {
		super(title);
		// TODO Auto-generated constructor stub
		final IntervalCategoryDataset dataset = createDataset(schedule, startTime, process, machine, problem);
		final JFreeChart chart = createChart(dataset);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
//		CategoryItemRenderer renderer = plot.getRenderer();
//		MyRenderer renderer = new MyRenderer();
//		plot.setRenderer(renderer);
//		plot.getRenderer().setSeriesPaint(1, Color.yellow);
//		plot.getRenderer().setSeriesPaint(2, Color.yellow);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	private static IntervalCategoryDataset createDataset(int[][] schedule, int[][] startTime, int[][] process, int[][] machine, Problem problem) {
		TaskSeries[] taskSeries = new TaskSeries[schedule[0].length]; //one taskSeries for each job
//		Task[] tasks = new Task[schedule[0].length];
		for (int i = 0; i < taskSeries.length; i++) {
			taskSeries[i] = new TaskSeries("Job "+i);
		}
		String[] machineTitles = new String[schedule.length];
		for (int i = 0; i < machineTitles.length; i++) {
			machineTitles[i] = "M"+i;
		}
//		for (int i = 0; i < machineTitles.length; i++) {
//			int maxTime = getMaxTime(startTime, process);
//			tasks[i] = (new Task(machineTitles[i], new SimpleTimePeriod(0, maxTime)));
//		}
		
		for (int i = 0; i < schedule.length; i++) {
			for (int j = 0; j < schedule[i].length; j++) {
				int job = schedule[i][j];
				taskSeries[job].add(new Task(machineTitles[i],
						new SimpleTimePeriod(startTime[i][j], startTime[i][j] + problem.getJobs().get(job).getProcessTime(i))));
			}
		}
		
//		for (int i = 0; i < machineTitles.length; i++) { //for each machine
//			for (int j = 0; j < taskSeries.length; j++) {//for each level in schedule
//				int job = schedule[i][j];
//				System.out.println(job);
//				taskSeries[job].add(new Task(machineTitles[i],
//				new SimpleTimePeriod(startTime[i][j], startTime[i][j] + process[job][taskNr[job]])));
//				if (job == 0)
//					System.out.println("Machine "+i+", task "+j+"  StartTime: "+startTime[i][j]+"  endTime: "+(startTime[i][j] + process[job][taskNr[job]]));
//				taskNr[job] ++;
//			}
//		}
//		for (int i = 0; i < tasks.length; i++) {
//			taskSeries[i].add(tasks[i]);
//		}
		final TaskSeriesCollection collection = new TaskSeriesCollection();
		for (int i = 0; i < taskSeries.length; i++) {
			collection.add(taskSeries[i]);
		}
		return collection;
	}
	
//	private static int getMaxTime(int[][] startTime, int[][] process) {
//		int max = 0;
//		for (int i = 0; i < process.length; i++) {
//			for (int j = 0; j < process[0].length; j++) {
//				if (startTime[i][j]+process[i][j] > max)
//					max = startTime[i][j]+process[i][j];
//			}
//		}
//		return max;
//	}
	
	private JFreeChart createChart(final IntervalCategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createGanttChart(
				"JSP",
				"Machine",
				"Time",
				dataset,
				true,
				true,
				true
		);
		return chart;
	}
	

	
	private static class MyRenderer extends GanttRenderer {

	    @Override
	    public Paint getItemPaint(int row, int col) {
	    	Paint[] paints = {Color.BLUE, Color.BLACK, Color.DARK_GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW, Color.LIGHT_GRAY};
            return paints[row];
	    }
	}

}
