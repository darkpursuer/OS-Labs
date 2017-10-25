
public abstract class AlgorithmBase implements IAlgorithm {
	protected InformationType infoType;

	protected int cycle;
	protected int random;

	protected boolean randomUsed = false;
	protected Process currentRunningProcess = null;
	protected int terminatedProcessCount = 0;
	protected int blockedProcessCount = 0;

	protected void reset(int cycle) {
		this.cycle = cycle;
		randomUsed = false;
		currentRunningProcess = null;
		terminatedProcessCount = 0;
		blockedProcessCount = 0;
	}
	
	protected AlgorithmState createReport(){
		AlgorithmState state = new AlgorithmState();
		state.isCPUUsed = currentRunningProcess != null;
		state.isIOUsed = blockedProcessCount > 0;
		state.terminatedCount = terminatedProcessCount;

		return state;
	}

	public int randomOS(int u) {
		random = MyOS.getCurrentRandom(cycle);
		randomUsed = true;
		return 1 + random % u;
	}

	protected void run(Process process) {
		int cpuBurst = randomOS(process.CPUBurst);
		if (cpuBurst > process.remainingTime) {
			cpuBurst = process.remainingTime;
		}
		process.randomCPUBurst = cpuBurst;
		process.nextDuration = cpuBurst;
		process.status = ProcessStatus.running;

		currentRunningProcess = process;
	}

	protected void ready(Process process) {
		process.status = ProcessStatus.ready;
		process.nextDuration = 0;
	}

	protected void block(Process process) {
		process.status = ProcessStatus.blocked;
		process.nextDuration = process.randomCPUBurst * process.IOBurst;
	}

	protected void terminate(Process process) {
		process.status = ProcessStatus.terminated;
		process.finishedTime = cycle;
		process.nextDuration = 0;
		terminatedProcessCount++;
	}
}
