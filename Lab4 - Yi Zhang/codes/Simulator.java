import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Simulator {

	private final int Q = 3;

	private int MACHINESIZE, PAGESIZE, PROCESSSIZE, REFNUM;
	private static int DEBUGLEVEL;

	private int PROCESSNUM;
	private int Cycle = 1;
	private Process[] processes;

	private int FRAMENUM;
	private Frame[] frames;
	private Handler handler;

	private final String randomFilePath = "random-numbers.txt";
	private static ArrayList<Integer> randomList;

	public Simulator(int machineSize, int pageSize, int pSize, int jobMix, int refNum, String algorithm,
			int debugLevel) {
		this.MACHINESIZE = machineSize;
		this.PAGESIZE = pageSize;
		this.PROCESSSIZE = pSize;
		this.REFNUM = refNum;
		this.DEBUGLEVEL = debugLevel;

		FRAMENUM = MACHINESIZE / PAGESIZE;
		frames = new Frame[FRAMENUM];
		for (int i = 0; i < FRAMENUM; i++) {
			frames[i] = new Frame(i);
		}

		loadJob(jobMix);
		loadAlgorithm(algorithm);

		randomList = new ArrayList<Integer>();
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

	public void start() {

		s: while (!isAllFinished()) {

			for (int i = 0; i < processes.length; i++) {
				Process process = processes[i];
				for (int ref = 0; ref < Q; ref++) {

					if (!process.isFinished()) {

						int pageIndex = process.addr / PAGESIZE;

						Frame frame = getFrame(i, pageIndex);
						if (frame == null) {
							process.pageFualtCount++;

							frame = handler.getFree();
							if (frame == null) {
								frame = handler.evict(process.name);

								Process evictedProcess = processes[frame.processIndex];
								evictedProcess.evictCount++;
								evictedProcess.residencySum += Cycle - frame.loadTime;

								if (isDebug()) {
									System.out.printf(
											"%d references word %d (page %d) at time %d: Fault, evicting page %d of %d from frame %d.\n",
											process.name, process.addr, pageIndex, Cycle, frame.pageIndex,
											frame.processIndex + 1, frame.frameIndex);
								}
							} else {
								if (isDebug()) {
									System.out.printf(
											"%d references word %d (page %d) at time %d: Fault, using free frame %d.\n",
											process.name, process.addr, pageIndex, Cycle, frame.frameIndex);
								}
							}

							frame.load(i, pageIndex, Cycle);
						} else {
							if (isDebug()) {
								System.out.printf("%d references word %d (page %d) at time %d: Hit in frame %d.\n",
										process.name, process.addr, pageIndex, Cycle, frame.frameIndex);
							}

							frame.load(i, pageIndex, Cycle);
						}

						int random = getNextRandom(process.name);
						int nextRandom = -1;

						int addr = process.nextAddr(random);
						if (addr == -1) {
							nextRandom = getNextRandom(process.name);
							addr = process.nextRandomAddr(nextRandom);
						}
						process.addr = addr;

						process.count++;
						Cycle++;

						if (isAllFinished()) {
							break s;
						}
					}
				}
			}
		}

		System.out.println();

		int totalFaults = 0;
		int totalEvicts = 0;
		int totalSum = 0;

		for (int i = 0; i < processes.length; i++) {
			Process process = processes[i];

			totalFaults += process.pageFualtCount;
			totalEvicts += process.evictCount;
			totalSum += process.residencySum;

			if (process.evictCount != 0) {
				System.out.printf("Process %d had %d faults and %.1f average residency.\n", process.name,
						process.pageFualtCount, (double) ((double) process.residencySum / (double) process.evictCount));
			} else {
				System.out.printf(
						"Process %d had %d faults.\n\tWith no evictions, the average residence is undefined.\n",
						process.name, process.pageFualtCount);
			}
		}
		System.out.println();

		if (totalEvicts != 0) {
			System.out.printf("The total number of faults is %d and the overall average residency is %.1f.\n",
					totalFaults, (double) ((double) totalSum / (double) totalEvicts));
		} else {
			System.out.printf(
					"The total number of faults is %d.\n\tWith no evictions, the overall average residence is undefined.\n",
					totalFaults);
		}
	}

	private boolean isAllFinished() {
		for (int i = 0; i < processes.length; i++) {
			if (!processes[i].isFinished()) {
				return false;
			}
		}
		return true;
	}

	private Frame getFrame(int processIndex, int pageIndex) {
		Frame result = null;
		for (int i = 0; i < frames.length; i++) {
			Frame frame = frames[i];
			if (frame.isMapped) {
				if (frame.processIndex == processIndex && frame.pageIndex == pageIndex) {
					result = frame;
					break;
				}
			}
		}
		return result;
	}

	private void loadJob(int jobType) {
		switch (jobType) {
		case 1:
			PROCESSNUM = 1;
			processes = new Process[PROCESSNUM];
			processes[0] = new Process(1, 1, 0, 0, PROCESSSIZE, REFNUM);
			break;

		case 2:
			PROCESSNUM = 4;
			processes = new Process[PROCESSNUM];
			for (int i = 0; i < PROCESSNUM; i++) {
				processes[i] = new Process(i + 1, 1, 0, 0, PROCESSSIZE, REFNUM);
			}
			break;

		case 3:
			PROCESSNUM = 4;
			processes = new Process[PROCESSNUM];
			for (int i = 0; i < PROCESSNUM; i++) {
				processes[i] = new Process(i + 1, 0, 0, 0, PROCESSSIZE, REFNUM);
			}
			break;

		case 4:
			PROCESSNUM = 4;
			processes = new Process[PROCESSNUM];
			processes[0] = new Process(1, 0.75f, 0.25f, 0, PROCESSSIZE, REFNUM);
			processes[1] = new Process(2, 0.75f, 0, 0.25f, PROCESSSIZE, REFNUM);
			processes[2] = new Process(3, 0.75f, 0.125f, 0.125f, PROCESSSIZE, REFNUM);
			processes[3] = new Process(4, 0.5f, 0.125f, 0.125f, PROCESSSIZE, REFNUM);
			break;

		default:
			break;
		}
	}

	private void loadAlgorithm(String algorithm) {
		if (algorithm.equalsIgnoreCase("lifo")) {

			handler = new LIFOHandler(frames);
		} else if (algorithm.equalsIgnoreCase("random")) {

			handler = new RandomHandler(frames);
		} else if (algorithm.equals("lru")) {

			handler = new LRUHandler(frames);
		}
	}

	private static int currentIndex = -1;

	public static int getNextRandom(int process) {
		currentIndex++;
		int x = randomList.get(currentIndex);

		if (isRandom()) {
			System.out.printf("%d uses random number: %d\n", process, x);
		}

		return x;
	}

	private boolean isDebug() {
		return DEBUGLEVEL == 1 || DEBUGLEVEL == 11;
	}

	private static boolean isRandom() {
		return DEBUGLEVEL == 11;
	}
}