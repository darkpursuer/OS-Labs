public class BankerMain {

	public static void main(String[] args) {
		if (args.length != 1) {
			return;
		}

		String path = args[0];

		// create two allocators
		MyAllocator fifo = new MyAllocator(HandlerBase.FIFO);
		MyAllocator banker = new MyAllocator(HandlerBase.Banker);

		// start simulation
		SimulResult fifoResult = fifo.start(path);
		SimulResult bankerResult = banker.start(path);

		// print the results
		for (int i = 0; i < bankerResult.errors.size(); i++) {
			System.out.println(bankerResult.errors.get(i));
		}

		System.out.println("              FIFO                     BANKER'S");

		int totalFIFO = 0;
		int totalWaitingFIFO = 0;
		int totalBanker = 0;
		int totalWaitingBanker = 0;

		for (int i = 0; i < fifoResult.totalTime.length; i++) {
			// print for FIFO

			// if this task is aborted
			if (fifoResult.aborted.contains((Object) i)) {
				System.out.printf("     Task %d    aborted     ", i + 1);
			} else {
				int totalTime = fifoResult.totalTime[i];
				int waitingTime = fifoResult.waitingTime[i];
				int ratio = Math.round(((float) waitingTime / (float) totalTime) * 100);
				System.out.printf("     Task %d %4d %4d %4d%%", i + 1, totalTime, waitingTime, ratio);

				totalFIFO += totalTime;
				totalWaitingFIFO += waitingTime;
			}

			// print for Banker's

			if (bankerResult.aborted.contains((Object) i)) {
				System.out.printf("     Task %d      aborted\n", i + 1);
			} else {
				int totalTime = bankerResult.totalTime[i];
				int waitingTime = bankerResult.waitingTime[i];
				int ratio = Math.round(((float) waitingTime / (float) totalTime) * 100);
				System.out.printf("     Task %d %4d %4d %4d%%\n", i + 1, totalTime, waitingTime, ratio);

				totalBanker += totalTime;
				totalWaitingBanker += waitingTime;
			}
		}

		// print for FIFO
		int ratio = Math.round(((float) totalWaitingFIFO / (float) totalFIFO) * 100);
		System.out.printf("     total  %4d %4d %4d%%", totalFIFO, totalWaitingFIFO, ratio);

		// print for Banker's
		ratio = Math.round(((float) totalWaitingBanker / (float) totalBanker) * 100);
		System.out.printf("     total  %4d %4d %4d%%\n", totalBanker, totalWaitingBanker, ratio);
	}
}
