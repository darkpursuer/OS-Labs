import java.util.ArrayDeque;
import java.util.ArrayList;

public class RRAlgorithm extends AlgorithmBase {
	public static int Q = 2;

	private ArrayDeque<Process> readyList = new ArrayDeque<Process>();

	public RRAlgorithm() {
	}

	public RRAlgorithm(InformationType infoType) {
		this.infoType = infoType;
	}

	public AlgorithmState arrange(int cycle, ArrayList<Process> table) {
		reset(cycle);

		// Check the running and blocked statuses
		for (int i = 0; i < table.size(); i++) {
			Process process = table.get(i);

			switch (process.status) {
			case unstarted:
				if (process.start <= cycle) {
					ready(process);
				}
				break;

			case ready:
				process.waitingTime++;
				break;

			case running:
				process.remainingTime--;
				process.remainingQuantum--;
				if (process.nextDuration == 0) {
					if (process.remainingTime == 0) {
						// this process has been done
						terminate(process);
					} else {
						if (process.IOBurst == 0) {
							// go back to ready list
							ready(process);
						} else {
							// block this process to perform IO instruction
							block(process);
						}
					}
				} else {
					if (process.remainingQuantum == 0) {
						suspend(process);
					} else {
						currentRunningProcess = process;
					}
				}
				break;

			case blocked:
				process.IOTime++;
				blockedProcessCount++;
				if (process.nextDuration == 0) {
					ready(process);
				}
				break;

			default:
				break;
			}
		}

		// Check the ready statuses
		// when there is no process running in the next cycle
		if (currentRunningProcess == null) {
			Process process = readyList.poll();
			if (process != null) {
				if (process.savedRemainingTime == 0) {
					run(process);
				} else {
					continue_p(process);
				}
			}
		}

		if (randomUsed && infoType == InformationType.random) {
			System.out.printf("Find burst when choosing ready process to run %d\r\n", random);
		}

		return createReport();
	}

	public String getFormalName() {
		return "Round Robbin";
	}

	protected void ready(Process process) {
		super.ready(process);
		readyList.add(process);
	}

	protected void run(Process process) {
		super.run(process);

		process.remainingQuantum = Q;
	}

	protected void suspend(Process process) {
		process.status = ProcessStatus.ready;
		process.savedRemainingTime = process.nextDuration;
		process.nextDuration = 0;
		readyList.add(process);
	}

	protected void continue_p(Process process) {
		process.status = ProcessStatus.running;
		process.nextDuration = process.savedRemainingTime;
		process.savedRemainingTime = 0;
		process.remainingQuantum = Q;
		currentRunningProcess = process;
	}
}