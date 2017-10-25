import java.util.ArrayList;

public class SimulResult {

	public int handlerType;

	public int[] totalTime;

	public int[] waitingTime;
	
	public ArrayList<Integer> aborted;

	public ArrayList<String> errors;

	public SimulResult(int type, int taskNum) {
		handlerType = type;
		totalTime = new int[taskNum];
		waitingTime = new int[taskNum];
		errors = new ArrayList<>();
		aborted = new ArrayList<>();
	}
}
