import java.util.ArrayList;

public abstract class HandlerBase {

	public static int FIFO = 0;
	public static int Banker = 1;

	// the total amount of resources
	protected int[] totalResources;

	// the occupied amount of resources
	protected int[] occupiedResources;

	// the temporarily buffered resources, and they will be available in the
	// next cycle
	protected int[] bufferedResources;

	// the blocked tasks in the last cycle
	protected ArrayList<Integer> blockedTasks = new ArrayList<>();

	// initialize the resources data
	public void init(int[] resources) {
		totalResources = resources.clone();
		occupiedResources = new int[resources.length];
		bufferedResources = new int[resources.length];
	}

	// start run the handler
	public abstract ArrayList<String> run(int cycle, Task[] tasks);

	// get the situations of all available resources
	protected int[] availableResources() {
		int[] result = new int[totalResources.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = totalResources[i] - occupiedResources[i] - bufferedResources[i];
		}
		return result;
	}

	// recover the buffered resources
	protected void restoreBufferedResources() {
		bufferedResources = new int[bufferedResources.length];
	}

	// abort a task
	protected void abort(Task task, int cycle, StringBuffer info) {
		task.abort(cycle);
		info.append(String.format(
				"According to the spec task %d is aborted now and its resources are available next cycle (%d-%d).\n",
				task.name, cycle + 1, cycle + 2));

		// release its resources
		for (int j = 0; j < task.occupiedResourses.length; j++) {
			occupiedResources[j] -= task.occupiedResourses[j];
		}
	}
}
