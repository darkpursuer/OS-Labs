import java.util.ArrayList;

public class BankerHandler extends HandlerBase {

	private boolean isDebug = false;

	public BankerHandler(boolean isDebug) {
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

					// check whether this task request more than its claim, then
					// aborts it
					if (task.occupiedResourses[resType] + number > task.claimResources[resType]) {
						errors.add(String.format(
								"During cycle %d-%d of Banker's algorithms\n   Task %d's request exceeds its claim; aborted;\n",
								cycle, cycle + 1, task.name));

						// if this task is blocked before, then stop blocking it
						if (blockedTasks.contains(index)) {
							blockedTasks.remove((Object) index);
						}
						abort(task, cycle, info);
						continue;
					}

					if (isSafe(tasks, task, requestActivity)) {
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

						info.append(
								String.format("   Task %d's request cannot be granted (not safe). So %d is blocked.\n",
										task.name, task.name));
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

		restoreBufferedResources();

		if (isDebug) {
			System.out.println(info.toString());
			System.out.println();
		}

		return errors;
	}

	// check whether the future state is safe
	private boolean isSafe(Task[] tasks, Task rawTask, RequestActivity activity) {
		ArrayList<Task> completedTasks = new ArrayList<>();
		ArrayList<Task> incompletedTasks = new ArrayList<>();
		for (int i = 0; i < tasks.length; i++) {
			if (rawTask == tasks[i]) {

				// simulate this activity on the targeting task
				Task task = new Task(tasks[i].name, totalResources.length);
				task.claimResources = tasks[i].claimResources.clone();
				task.occupiedResourses = tasks[i].occupiedResourses.clone();

				task.occupiedResourses[activity.resType] += activity.number;

				incompletedTasks.add(task);
			} else {

				incompletedTasks.add(tasks[i]);
			}
		}

		// get the number of available resources
		int[] availableResources = availableResources();
		availableResources[activity.resType] -= activity.number;

		while (!incompletedTasks.isEmpty()) {
			Task metTask = null;

			// find a task whose unmet resource needs are all smaller than the
			// available resources
			for (int i = 0; i < incompletedTasks.size(); i++) {
				Task task = incompletedTasks.get(i);
				boolean isMet = true;
				for (int j = 0; j < availableResources.length; j++) {
					if (task.claimResources[j] - task.occupiedResourses[j] > availableResources[j]) {
						isMet = false;
						break;
					}
				}

				if (isMet) {
					metTask = task;
					break;
				}
			}

			// if not found, then the state is unsafe
			if (metTask == null) {
				return false;
			}

			completedTasks.add(metTask);
			incompletedTasks.remove(metTask);

			// give back the occupied resources
			for (int j = 0; j < availableResources.length; j++) {
				availableResources[j] += metTask.occupiedResourses[j];
			}
		}

		// otherwise, the state is safe
		return true;
	}
}
