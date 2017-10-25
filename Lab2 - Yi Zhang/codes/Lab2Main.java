
public class Lab2Main {
	public static void main(String[] args) {

		if (args.length > 0) {
			AlgorithmType[] types = new AlgorithmType[] { AlgorithmType.FCFS, AlgorithmType.RR, AlgorithmType.SJF,
					AlgorithmType.Uniprogrammed };
			InformationType infoType = InformationType.simple;
			String inputFilePath = "";
			String randomFilePath = "random-numbers.txt";

			if (args[0].equals("--verbose")) {
				infoType = InformationType.verbose;
				inputFilePath = args[1];
			} else {
				inputFilePath = args[0];
			}

			for (int i = 0; i < types.length; i++) {
				MyOS myOS = new MyOS(types[i], randomFilePath, infoType);
				myOS.load(inputFilePath);
				myOS.run();
			}
			return;
		}

		help();
		return;

	}

	private static void help() {
		String help = "Help: java Lab2Main [--verbose] <inputfilepath>\r\n" + "Options Including:\r\n"
				+ "\t\t <inputfilepath>: the path of input file.\r\n"
				+ "\t\t [--verbose]: with this flag, the program will produce detailed output.\r\n"
				+ "\t\t For example: >> java Lab2Main -verbose inputfile\r\n" + "\r\n" + "\t -help\r\n"
				+ "\t\t To get help of this program.\r\n";
		System.out.println(help);
	}
}