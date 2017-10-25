
public class Paging {

	public static void main(String[] args) {
		if (args.length != 7) {
			return;
		}

		int M = Integer.parseInt(args[0]);
		int P = Integer.parseInt(args[1]);
		int S = Integer.parseInt(args[2]);
		int J = Integer.parseInt(args[3]);
		int N = Integer.parseInt(args[4]);
		String R = args[5];
		int debugLevel = Integer.parseInt(args[6]);

		System.out.printf("The machine size is %d.\n", M);
		System.out.printf("The page size is %d.\n", P);
		System.out.printf("The process size is %d.\n", S);
		System.out.printf("The job mix number is %d.\n", J);
		System.out.printf("The number of references per process is %d.\n", N);
		System.out.printf("The replacement algorithm is %s.\n", R);
		System.out.printf("The level of debugging output is %d.\n\n", debugLevel);

		Simulator simulator = new Simulator(M, P, S, J, N, R, debugLevel);
		simulator.start();
	}
}
