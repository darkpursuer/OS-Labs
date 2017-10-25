
public class Frame {

	public int frameIndex;

	public Frame(int index) {
		frameIndex = index;
	}

	public int processIndex = -1;
	public int pageIndex = -1;

	public boolean isMapped = false;

	public int loadTime = -1;
	public int lastRefTime = -1;

	public void load(int pIndex, int pageIndex, int cycle) {
		isMapped = true;

		if(processIndex != pIndex || this.pageIndex != pageIndex){
			loadTime = cycle;
			processIndex = pIndex;
			this.pageIndex = pageIndex;
		}
		
		lastRefTime = cycle;
	}
}
