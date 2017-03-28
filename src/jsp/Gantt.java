package jsp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	private static IntervalCategoryDataset createDataset(int[][] schedule, int[][] startTime, int[][] process) {
		TaskSeries[] taskSeries = new TaskSeries[schedule[0].length];
		for (int i = 0; i < taskSeries.length; i++) {
			taskSeries[i] = new TaskSeries("Job "+i);
		}
		String[] machineTitles = new String[schedule.length];
		for (int i = 0; i < machineTitles.length; i++) {
			machineTitles[i] = "M"+i;
		}
		int[] taskNr = new int[schedule.length];
		for (int j = 0; j < taskSeries.length; j++) {
			for (int i = 0; i < machineTitles.length; i++) {
				taskSeries[schedule[i][j]].add(new Task(machineTitles[i],
						new SimpleTimePeriod(startTime[i][j], startTime[i][j] + process[schedule[i][j]][taskNr[j]])));
				taskNr[j] ++;
			}
		}
		final TaskSeriesCollection collection = new TaskSeriesCollection();
		for (TaskSeries taskSerie : taskSeries) {
			collection.add(taskSerie);
		}
		return collection;
	}
	
	private JFreeChart createChart(final IntervalCategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createGanttChart(
				"JSP",
				"Machine",
				"Time",
				dataset,
				true,
				false,
				true
		);
		return chart;
	}

}
