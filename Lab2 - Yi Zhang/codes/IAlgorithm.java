import java.util.ArrayList;

public interface IAlgorithm {
	AlgorithmState arrange(int cycle, ArrayList<Process> table);

	int randomOS(int u);

	String getFormalName();
}

enum AlgorithmType {
	FCFS, RR, SJF, Uniprogrammed
}

class AlgorithmState {
	int terminatedCount;

	boolean isCPUUsed;
	boolean isIOUsed;
}