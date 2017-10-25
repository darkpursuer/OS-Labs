
public class Process {

	public String name;

	// predefined fields
	public int start, total;
	public int CPUBurst, IOBurst;
	
	// varied fields
	public int remainingTime;

	public ProcessStatus status;
	public int nextDuration;

	public int randomCPUBurst;

	public int IOTime;
	public int waitingTime;

	public int finishedTime;
	
	// for RR
	public int remainingQuantum;
	public int savedRemainingTime;

	public int turnaroundTime() {
		return finishedTime - start;
	}
	
	public int displayedNextDuration(){
		return remainingQuantum == 0 ? nextDuration : Math.min(nextDuration, remainingQuantum);
	}

	public String toString() {
		return String.format("[%s]: %s", name, status);
	}
}

enum ProcessStatus {
	unstarted, ready, running, blocked, terminated
}