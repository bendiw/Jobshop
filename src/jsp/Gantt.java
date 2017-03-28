package jsp;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;

public class Gantt extends ApplicationFrame{

	public Gantt(String title, int[][] schedule, int[][] startTime, int[][] process) {
		super(title);
		// TODO Auto-generated constructor stub
		final IntervalCategoryDataset dataset = createDataset(schedule, startTime, process);
		final JFreeChart chart = createChart(dataset);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
//		MyRenderer renderer = new MyRenderer(dataset);
//		renderer.setSeriesPaint(2, Color.yellow);
//		plot.getRenderer().setSeriesPaint(1, Color.yellow);
//		plot.getRenderer().setSeriesPaint(2, Color.yellow);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	private static IntervalCategoryDataset createDataset(int[][] schedule, int[][] startTime, int[][] process) {
		TaskSeries[] taskSeries = new TaskSeries[schedule[0].length]; //one taskSeries for each job
//		Task[] tasks = new Task[schedule[0].length];
		for (int i = 0; i < taskSeries.length; i++) {
			taskSeries[i] = new TaskSeries("Job "+i);
		}
		String[] machineTitles = new String[schedule.length];
		for (int i = 0; i < machineTitles.length; i++) {
			machineTitles[i] = "M"+i;
		}
		int[] taskNr = new int[schedule.length];
//		for (int i = 0; i < machineTitles.length; i++) {
//			int maxTime = getMaxTime(startTime, process);
//			tasks[i] = (new Task(machineTitles[i], new SimpleTimePeriod(0, maxTime)));
//		}
		for (int i = 0; i < machineTitles.length; i++) { //for each machine
			for (int j = 0; j < taskSeries.length; j++) {//for each level in schedule
//				Task subTask = new Task(machineTitles[i],
//						new SimpleTimePeriod(startTime[i][j], startTime[i][j] + process[schedule[i][j]][taskNr[j]]));
//				tasks[i].addSubtask(subTask);
				int job = schedule[i][j];
				System.out.println(job);
				taskSeries[job].add(new Task(machineTitles[i],
				new SimpleTimePeriod(startTime[i][j], startTime[i][j] + process[job][taskNr[job]])));
				if (job == 0)
					System.out.println("Machine "+i+", task "+j+"  StartTime: "+startTime[i][j]+"  endTime: "+(startTime[i][j] + process[job][taskNr[job]]));
				taskNr[job] ++;
			}
		}
//		for (int i = 0; i < tasks.length; i++) {
//			taskSeries[i].add(tasks[i]);
//		}
		final TaskSeriesCollection collection = new TaskSeriesCollection();
		for (TaskSeries taskSerie : taskSeries) {
			collection.add(taskSerie);
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
	
//	private static class MyRenderer extends GanttRenderer {
//
//	    private static final int PASS = 2; // assumes two passes
//	    private final List<Color> clut = new ArrayList<Color>();
//	    private final TaskSeriesCollection model;
//	    private int row;
//	    private int col;
//	    private int index;
//
//	    public MyRenderer(TaskSeriesCollection model) {
//	        this.model = model;
//	    }
//
//	    @Override
//	    public Paint getItemPaint(int row, int col) {
//	        if (clut.isEmpty() || this.row != row || this.col != col) {
//	            initClut(row, col);
//	            this.row = row;
//	            this.col = col;
//	            index = 0;
//	        }
//	        int clutIndex = index++ / PASS;
//	        return clut.get(clutIndex);
//	    }
//
//	    private void initClut(int row, int col) {
//	        clut.clear();
//	        Color c = (Color) super.getItemPaint(row, col);
//	        float[] a = new float[3];
//	        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), a);
//	        TaskSeries series = (TaskSeries) model.getRowKeys().get(row);
//	        List<Task> tasks = series.getTasks(); // unchecked
//	        int taskCount = tasks.get(col).getSubtaskCount();
//	        taskCount = Math.max(1, taskCount);
//	        for (int i = 0; i < taskCount; i++) {
//	            clut.add(Color.getHSBColor(a[0], a[1] / i, a[2]));
//	        }
//	    }
//	}

}
