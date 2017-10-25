import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class MyAllocator {

	private static boolean isDebug = false;

	private int handlerType;
	public HandlerBase handler;

	private ArrayList<String> errors = new ArrayList<>();

	public MyAllocator(int handlerType) {
		this.handlerType = handlerType;
		if (handlerType == HandlerBase.FIFO) {
			handler = new FIFOHandler(isDebug);
		} else if (handlerType == HandlerBase.Banker) {
			handler = new BankerHandler(isDebug);
		}
	}

	public int TASKNUM;
	public int RESOURCENUM;
	public int[] resources;
	public Task[] tasks;

	public SimulResult start(String path) {

		// initiate the tasks and resources from input
		init(path);
		handler.init(resources);

		// start handle with the tasks
		int cycle = 0;

		while (!isAllTerminated()) {
			errors.addAll(handler.run(cycle, tasks));
			cycle++;
		}

		SimulResult result = new SimulResult(handlerType, TASKNUM);
		result.errors = errors;

		for (int i = 0; i < tasks.length; i++) {
			Task task = tasks[i];

			// if this task is aborted
			if (task.isAborted()) {
				result.aborted.add(i);
				continue;
			}

			result.totalTime[i] = task.totalTime();
			result.waitingTime[i] = task.waitingTime;
		}

		return result;
	}

	private void init(String path) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));

			// the first line is the claim of numbers
			String line = reader.readLine();
			while (line.equals("")) {
				line = reader.readLine();
			}
			String[] splits = line.split(" ");
			if (splits.length >= 3) {
				// initiate the resources and tasks
				TASKNUM = Integer.parseInt(splits[0]);
				tasks = new Task[TASKNUM];
				RESOURCENUM = Integer.parseInt(splits[1]);
				resources = new int[RESOURCENUM];

				for (int i = 0; i < RESOURCENUM; i++) {
					resources[i] = Integer.parseInt(splits[2 + i]);
				}
				for (int i = 0; i < TASKNUM; i++) {
					tasks[i] = new Task(i + 1, RESOURCENUM);
				}

				// then read all the activities
				while ((line = reader.readLine()) != null) {
					if (line.equals("")) {
						continue;
					}

					line = line.trim();
					line = line.replaceAll("\\s+", " ");
					splits = line.split(" ");
					String activity = splits[0];

					// convert all the following numbers to integers
					int[] params = new int[3];
					for (int i = 0; i < params.length; i++) {
						params[i] = Integer.parseInt(splits[i + 1]);
					}

					// get the related task
					Task task = tasks[params[0] - 1];

					// save all the following activities for this task
					if (activity.equalsIgnoreCase(Activity.INITIATE)) {

						int resType = params[1] - 1;
						int resNum = params[2];

						task.add(new InitiateActivity(resType, resNum));

						// error check before simulation
						if (handlerType == HandlerBase.Banker && resNum > resources[resType]) {
							errors.add(String.format(
									"Banker aborts task %d before run begins: \n     claim for resourse %d (%d) exceeds number of units present (%d).\n",
									task.name, resType + 1, resNum, resources[resType]));
							task.abort(0);
						}

					} else if (activity.equalsIgnoreCase(Activity.REQUEST)) {

						task.add(new RequestActivity(params[1] - 1, params[2]));

					} else if (activity.equalsIgnoreCase(Activity.RELEASE)) {

						task.add(new ReleaseActivity(params[1] - 1, params[2]));

					} else if (activity.equalsIgnoreCase(Activity.COMPUTE)) {

						task.add(new ComputeActivity(params[1]));

					} else if (activity.equalsIgnoreCase(Activity.TERMINATE)) {

						task.add(new TerminateActivity());
					}
				}
			} else {
				System.err.println("The number of initiation numbers is incorrect!");
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isAllTerminated() {
		for (int i = 0; i < tasks.length; i++) {
			if (!tasks[i].isTerminated()) {
				return false;
			}
		}
		return true;
	}
}
