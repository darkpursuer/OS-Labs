import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class MyOS {

	public int Cycle = 0;
	private int CPUUsedCycle = 0;
	private int IOUsedCycle = 0;

	private String inputText = "";

	public ArrayList<Process> ProcessTable;

	public AlgorithmType Type;
	public IAlgorithm Algorithm;

	public InformationType infoType;

	private static ArrayList<Integer> randomList;

	public MyOS(AlgorithmType type, String randomFilePath, InformationType infoType) {
		ProcessTable = new ArrayList<Process>();
		randomList = new ArrayList<Integer>();

		this.infoType = infoType;

		Type = type;
		Algorithm = AlgorithmFactory.create(Type, infoType);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(randomFilePath));
			String line;
			while ((line = reader.readLine()) != null) {
				randomList.add(Integer.parseInt(line));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(String inputPath) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(new File(inputPath).toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String line = lines.get(0);
		int count = Integer.parseInt(line.substring(0, line.indexOf(" ")));
		inputText += count + " ";

		for (int i = 0; i < count; i++) {
			String section = line.substring(line.indexOf("("), line.indexOf(")") + 1);
			line = line.substring(line.indexOf(")") + 1).trim();
			inputText += section + " ";

			section = section.substring(1, section.length() - 1);
			String[] splits = section.split(" ");

			Process p = new Process();
			p.start = Integer.parseInt(splits[0]);
			p.total = Integer.parseInt(splits[2]);
			p.CPUBurst = Integer.parseInt(splits[1]);
			p.IOBurst = Integer.parseInt(splits[3]);
			p.status = ProcessStatus.unstarted;
			p.remainingTime = p.total;

			ProcessTable.add(p);
		}

		// sort the inputs
		for (int i = 0; i < ProcessTable.size() - 1; i++) {
			for (int j = 0; j < ProcessTable.size() - 1 - i; j++) {
				Process former = ProcessTable.get(j);
				Process later = ProcessTable.get(j + 1);
				if (former.start > later.start) {
					ProcessTable.set(j, later);
					ProcessTable.set(j + 1, former);
				}
			}
		}

		// name the processes
		for (int i = 0; i < ProcessTable.size(); i++) {
			Process p = ProcessTable.get(i);
			p.name = i + "";
		}
	}

	public void run() {
		System.out.printf("The original input was: %s\r\n", inputText);

		System.out.printf("The (sorted) input is:  %d ", ProcessTable.size());
		for (int i = 0; i < ProcessTable.size(); i++) {
			Process p = ProcessTable.get(i);
			System.out.printf("(%d %d %d %d) ", p.start, p.CPUBurst, p.total, p.IOBurst);
		}
		System.out.println();
		System.out.println();

		if (infoType == InformationType.verbose || infoType == InformationType.random)
			System.out.println("This detailed printout gives the state and remaining burst for each process\r\n");

		int isNotTerminated = ProcessTable.size();
		while (isNotTerminated != 0) {

			if (infoType == InformationType.random || infoType == InformationType.verbose) {
				System.out.printf("Before cycle\t%d:", Cycle);

				for (int i = 0; i < ProcessTable.size(); i++) {
					Process process = ProcessTable.get(i);
					System.out.printf("\t%s\t%d", process.status, process.displayedNextDuration());
				}

				System.out.println(".");
			}

			for (int i = 0; i < ProcessTable.size(); i++) {
				Process process = ProcessTable.get(i);
				if (process.nextDuration > 0) {
					process.nextDuration--;
				}
			}
			
			AlgorithmState state = Algorithm.arrange(Cycle, ProcessTable);

			if (state.isCPUUsed)
				CPUUsedCycle++;
			if (state.isIOUsed)
				IOUsedCycle++;

			isNotTerminated = isNotTerminated - state.terminatedCount;
			if(isNotTerminated == 0)
			{
				break;
			}

			Cycle++;
		}

		// print summary
		double totalTurnaroundTime = 0;
		double totalWaitingTime = 0;

		System.out.printf("The scheduling algorithm used was %s\r\n\r\n", Algorithm.getFormalName());
		for (int i = 0; i < ProcessTable.size(); i++) {
			Process p = ProcessTable.get(i);
			System.out.printf("Process %s:\r\n", p.name);
			System.out.printf("\t(A, B, C, M) = (%d, %d, %d, %d)\r\n", p.start, p.CPUBurst, p.total, p.IOBurst);
			System.out.printf("\tFinishing time: %d\r\n", p.finishedTime);
			System.out.printf("\tTurnaround time: %d\r\n", p.turnaroundTime());
			System.out.printf("\tI/O time: %d\r\n", p.IOTime);
			System.out.printf("\tWaiting  time: %d\r\n", p.waitingTime);
			System.out.println();

			totalTurnaroundTime += p.turnaroundTime();
			totalWaitingTime += p.waitingTime;
		}

		System.out.println("Summary Data:");
		System.out.printf("\tFinishing time: %d\r\n", Cycle);
		System.out.printf("\tCPU Utilization: %f\r\n", (double) CPUUsedCycle / (double) Cycle);
		System.out.printf("\tI/O Utilization: %f\r\n", (double) IOUsedCycle / (double) Cycle);
		System.out.printf("\tThroughput: %f processes per hundred cycles\r\n",
				(double) (ProcessTable.size() * 100) / (double) Cycle);
		System.out.printf("\tAverage turnaround time: %f\r\n", totalTurnaroundTime / (double) ProcessTable.size());
		System.out.printf("\tAverage waiting time: %f\r\n", totalWaitingTime / (double) ProcessTable.size());
	}

	private static int lastCycle = -1;
	private static int currentIndex = -1;

	public static int getCurrentRandom(int cycle) {
		if (cycle != lastCycle) {
			currentIndex++;
			lastCycle = cycle;
		}
		int x = randomList.get(currentIndex);
		return x;
	}
}

enum InformationType {
	simple, verbose, random
}