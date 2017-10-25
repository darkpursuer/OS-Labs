
public class AlgorithmFactory {
	public static IAlgorithm create(AlgorithmType type, InformationType infoType) {
		switch (type) {
		case FCFS:
			return new FCFSAlgorithm(infoType);

		case RR:
			return new RRAlgorithm(infoType);

		case SJF:
			return new SJFAlgorithm(infoType);

		case Uniprogrammed:
			return new UniAlgorithm(infoType);
		}
		return null;
	}
}