import java.util.ArrayList;

public class Task {

	// the number of this task
	public int name;

	// the claimed resources of this task
	public int[] claimResources;

	// all the activities of this task
	public ArrayList<Activity> activities;

	private int activityIndex;

	// the occupied resources of this task
	public int[] occupiedResourses;

	// the remaining cycle of computing
	public int computingCycles;

	// the total cycles of computing
	public int totalComputingCycles;

	// whether this task has been terminated
	private int terminatedCycle;

	// the total waiting time of this task
	public int waitingTime;

	// last waiting activity
	public int waitingActivity;

	// in which cycle this task is aborted
	public int abortedCycle;

	public Task(int name, int resourceNum) {
		this.name = name;
		claimResources = new int[resourceNum];
		occupiedResourses = new int[resourceNum];
		computingCycles = 0;
		terminatedCycle = -1;
		waitingTime = 0;
		waitingActivity = -1;
		activities = new ArrayList<>();
		activityIndex = 0;
		abortedCycle = -1;
	}

	// add a new activity to this task
	public void add(Activity activity) {
		activities.add(activity);
	}

	public Activity next() {
		if (isTerminated()) {
			return null;
		}
		if (isWaiting()) {
			return activities.get(waitingActivity);
		}
		return activities.get(activityIndex++);
	}

	// initiate this task
	public void initiate(int type, int number) {
		claimResources[type] = number;
	}

	// request resources for this task
	public boolean request(int type, int number) {
		if (occupiedResourses[type] + number <= claimResources[type]) {
			occupiedResourses[type] += number;
			return true;
		}
		return false;
	}

	// release resources for this task
	public boolean release(int type, int number) {
		if (occupiedResourses[type] - number >= 0) {
			occupiedResourses[type] -= number;
			return true;
		}
		return false;
	}

	// start compute for this task
	public void startCompute(int cycles) {
		computingCycles = totalComputingCycles = cycles;
	}

	// whether this task is computing
	public boolean isComputing() {
		return computingCycles != 0;
	}

	// compute this task for one cycle
	public boolean compute() {
		if (computingCycles > 0) {
			computingCycles--;
			return true;
		}
		return false;
	}

	// terminate this task
	public void terminate(int cycle) {
		terminatedCycle = cycle;
	}

	// judge whether this task has been terminated
	public boolean isTerminated() {
		return terminatedCycle != -1;
	}

	// get the total time of this task
	public int totalTime() {
		return terminatedCycle;
	}

	// start waiting
	public void startWaiting() {
		waitingActivity = activityIndex - 1;
	}

	// whether this task is waiting
	public boolean isWaiting() {
		return waitingActivity != -1;
	}

	// stop waiting for this task
	public void stopWaiting() {
		waitingActivity = -1;
	}

	// abort this task
	public void abort(int cycle) {
		abortedCycle = cycle;
		terminate(cycle);
	}

	// whether this task is aborted
	public boolean isAborted() {
		return abortedCycle != -1;
	}
}
