import java.util.ArrayList;

public class FIFOHandler extends HandlerBase {

	private boolean isDebug = false;

	// the set of activities of each task
	private Activity[] activities;

	public FIFOHandler(boolean isDebug) {
		this.isDebug = isDebug;
	}

	@Override
	public ArrayList<String> run(int cycle, Task[] tasks) {

		StringBuffer info = new StringBuffer();
		info.append(String.format("During %d-%d\n", cycle, cycle + 1));

		ArrayList<String> errors = new ArrayList<>();

		// the size of blocked tasks in the last cycle
		int base = blockedTasks.size();
		int lastTask = -1;

		// save a copy for the blocked tasks list
		@SuppressWarnings("unchecked")
		ArrayList<Integer> copyBlocked = (ArrayList<Integer>) blockedTasks.clone();

		activities = new Activity[tasks.length];

		// whether there is a task granted by resources
		boolean isGranted = false;

		// start simulation for each task
		for (int i = 0; i < tasks.length; i++) {
			// get the task and the activity
			Task task = null;
			int index = -1;

			// first handle with the blocked tasks
			if (i < base) {
				index = copyBlocked.get(i);
			} else {
				// then handle with other tasks

				index = lastTask + 1;
				while (copyBlocked.contains(index)) {
					index++;
				}
				lastTask = index;
			}
			task = tasks[index];

			// if this task is computing, then continue it
			if (task.isComputing()) {
				task.compute();
				info.append(String.format("   Task %d computes (%d of %d cycles).\n", task.name,
						task.totalComputingCycles - task.computingCycles, task.totalComputingCycles));
				continue;
			}

			// get the next available activity
			Activity activity = task.next();
			activities[i] = activity;

			if (activity != null) {
				switch (activity.type()) {

				// if the activity is to initialize this task
				case INITIATE:
					InitiateActivity initiateActivity = (InitiateActivity) activity;
					task.initiate(initiateActivity.resType, initiateActivity.number);
					info.append(String.format("   Task %d completes its initiate.\n", task.name));
					break;

				// if it is to request resource
				case REQUEST:
					RequestActivity requestActivity = (RequestActivity) activity;
					int resType = requestActivity.resType;
					int number = requestActivity.number;
					
					// if the available resources is larger, then grant them to this task
					if (availableResources()[resType] >= number) {
						isGranted = true;

						// if this request is waiting, then stop waiting it
						if (task.isWaiting()) {
							task.stopWaiting();
						}

						// if this task is blocked before, then stop blocking it
						if (blockedTasks.contains(index)) {
							blockedTasks.remove((Object) index);
						}

						occupiedResources[resType] += number;
						task.request(resType, number);
						info.append(String.format("   Task %d completes its request (i.e., the request is granted).\n",
								task.name));
					} else {
						// hold and let this task wait
						if (!task.isWaiting()) {
							task.startWaiting();
						}
						task.waitingTime++;

						// mark this task as blocked
						if (!blockedTasks.contains(index)) {
							blockedTasks.add(index);
						}

						info.append(String.format("   Task %d's request cannot be granted.\n", task.name));
					}
					break;

				// if it is to release resource
				case RELEASE:
					ReleaseActivity releaseActivity = (ReleaseActivity) activity;
					resType = releaseActivity.resType;
					number = releaseActivity.number;
					bufferedResources[resType] += number;
					occupiedResources[resType] -= number;
					task.release(resType, number);
					info.append(String.format("   Task %d releases %d unit (available at %d).\n", task.name, number,
							cycle + 1));
					break;

				// if it is to compute
				case COMPUTE:
					task.startCompute(((ComputeActivity) activity).cycle);
					task.compute();
					info.append(String.format("   Task %d computes (%d of %d cycles).\n", task.name,
							task.totalComputingCycles - task.computingCycles, task.totalComputingCycles));
					break;

				// if it is to terminate this task
				case TERMINATE:
					task.terminate(cycle);
					info.append(String.format("   Task %d terminates at %d.\n", task.name, cycle));
					break;

				default:
					break;
				}
			}
		}

		// if there is a deadlock
		while (!isGranted && isDeadlocked()) {
			// abort the smallest task
			for (int i = 0; i < tasks.length; i++) {
				Task task = tasks[i];
				if (!task.isTerminated()) {
					abort(task, cycle, info);
					break;
				}
			}
		}

		// restore the newly released resources in this cycle
		restoreBufferedResources();

		if (isDebug) {
			System.out.println(info.toString());
			System.out.println();
		}

		return errors;
	}

	// detect whether there is a deadlock
	private boolean isDeadlocked() {
		boolean result = true;
		boolean noRequest = true;

		for (int i = 0; i < activities.length; i++) {
			Activity activity = activities[i];
			boolean cannotMet = false;

			if (activity == null) {
				continue;
			}

			if (activity.type() == ActivityType.REQUEST) {
				RequestActivity requestActivity = (RequestActivity) activity;
				int resType = requestActivity.resType;
				int number = requestActivity.number;
				
				// check whether there is enough resources for the demand
				if (availableResources()[resType] < number) {
					cannotMet = true;
				}
				noRequest = false;
			}

			result &= cannotMet;
		}
		return !noRequest && result;
	}
}
