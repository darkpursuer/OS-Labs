
public abstract class Handler {

	protected Frame[] frames;

	public abstract Frame evict(int processIndex);

	public Frame getFree() {
		Frame result = null;
		for (int i = frames.length - 1; i >= 0; i--) {
			if (!frames[i].isMapped) {
				result = frames[i];
				break;
			}
		}
		return result;
	}
}

class LIFOHandler extends Handler {

	public LIFOHandler(Frame[] frames) {
		this.frames = frames;
	}

	public Frame evict(int processIndex) {
		Frame result = null;
		int leastLoadTime = -1;

		for (int i = frames.length - 1; i >= 0; i--) {
			if (leastLoadTime < frames[i].loadTime) {
				result = frames[i];
				leastLoadTime = result.loadTime;
			}
		}

		return result;
	}

}

class RandomHandler extends Handler {

	public RandomHandler(Frame[] frames) {
		this.frames = frames;
	}

	public Frame evict(int processIndex) {
		int random = Simulator.getNextRandom(processIndex);
		int index = random % frames.length;
		return frames[index];
	}

}

class LRUHandler extends Handler {

	public LRUHandler(Frame[] frames) {
		this.frames = frames;
	}

	public Frame evict(int processIndex) {
		Frame result = null;
		int leastUsedTime = Integer.MAX_VALUE;

		for (int i = frames.length - 1; i >= 0; i--) {
			if (leastUsedTime > frames[i].lastRefTime) {
				result = frames[i];
				leastUsedTime = result.lastRefTime;
			}
		}

		return result;
	}

}
