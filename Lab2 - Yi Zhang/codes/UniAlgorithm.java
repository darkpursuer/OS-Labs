import java.util.ArrayDeque;
import java.util.ArrayList;

public class UniAlgorithm extends AlgorithmBase {
	private ArrayDeque<Process> readyList = new ArrayDeque<Process>();
	private Process currentProcess;

	public UniAlgorithm() {
	}

	public UniAlgorithm(InformationType infoType) {
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
				if (process.nextDuration == 0) {
					if (process.remainingTime == 0) {
						// this process has been done
						terminate(process);
					} else {
						// block this process to perform IO instruction
						block(process);
					}
				} else {
					currentRunningProcess = process;
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
		if (currentProcess == null || currentProcess.status == ProcessStatus.terminated) {
			Process process = readyList.poll();
			if (process != null)
				run(process);
		} else if (currentProcess != null && currentProcess.status == ProcessStatus.ready) {
			readyList.remove(currentProcess);
			run(currentProcess);
		}

		if (randomUsed && infoType == InformationType.random) {
			System.out.printf("Find burst when choosing ready process to run %d\r\n", random);
		}

		return createReport();
	}

	public String getFormalName() {
		return "Uniprocessing";
	}

	protected void ready(Process process) {
		super.ready(process);
		readyList.add(process);
	}

	protected void run(Process process) {
		super.run(process);
		currentProcess = process;
	}

	protected void block(Process process) {
		super.block(process);
		currentProcess = process;
	}
}