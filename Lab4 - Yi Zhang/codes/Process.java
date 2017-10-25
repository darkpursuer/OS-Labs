
public class Process {

	private float A, B, C;
	private int S, N;

	private float culB, culC;

	public int name;

	public int addr;

	public int count;

	public int pageFualtCount = 0;
	public int evictCount = 0;
	public int residencySum = 0;

	public Process(int name, float a, float b, float c, int s, int n) {
		this.name = name;

		A = a;
		B = b;
		C = c;
		culB = A + B;
		culC = A + B + C;

		S = s;
		N = n;

		addr = startFrom();
	}

	public boolean isFinished() {
		return count == N;
	}

	public int startFrom() {
		return (111 * name) % S;
	}

	public int nextAddr(int random) {
		double y = (double) random / (Integer.MAX_VALUE + 1d);
		if (y < A) {
			return (addr + 1) % S;
		}
		if (y < culB) {
			return (addr - 5 + S) % S;
		}
		if (y < culC) {
			return (addr + 4) % S;
		}
		return -1;
	}

	public int nextRandomAddr(int random) {
		return random % S;
	}
}
