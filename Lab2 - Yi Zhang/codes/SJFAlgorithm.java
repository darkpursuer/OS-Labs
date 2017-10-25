import java.util.ArrayList;

public class SJFAlgorithm extends AlgorithmBase {
	private ArrayList<Process> readyList = new ArrayList<Process>();

	public SJFAlgorithm() {
	}

	public SJFAlgorithm(InformationType infoType) {
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
		sortReadyList();

		if (currentRunningProcess == null) {
			if (!readyList.isEmpty()) {
				Process process = readyList.get(0);
				readyList.remove(0);
				run(process);
			}
		}

		if (randomUsed && infoType == InformationType.random) {
			System.out.printf("Find burst when choosing ready process to run %d\r\n", random);
		}

		return createReport();
	}

	public String getFormalName() {
		return "Shortest Job First";
	}

	protected void ready(Process process) {
		super.ready(process);
		readyList.add(process);
	}

	private void sortReadyList() {
		for (int i = 0; i < readyList.size() - 1; i++) {
			for (int j = 0; j < readyList.size() - 1 - i; j++) {
				if (readyList.get(j).remainingTime > readyList.get(j + 1).remainingTime) {
					Process tmp = readyList.get(j);
					readyList.set(j, readyList.get(j + 1));
					readyList.set(j + 1, tmp);
				}
			}
		}
	}
}