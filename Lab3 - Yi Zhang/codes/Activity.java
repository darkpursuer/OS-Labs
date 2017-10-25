
public abstract class Activity {

	// all the possible activities
	public final static String INITIATE = "initiate";
	public final static String REQUEST = "request";
	public final static String RELEASE = "release";
	public final static String COMPUTE = "compute";
	public final static String TERMINATE = "terminate";

	public abstract ActivityType type();
}

enum ActivityType {
	INITIATE, REQUEST, RELEASE, COMPUTE, TERMINATE
}

class InitiateActivity extends Activity {

	public int resType;
	public int number;

	public ActivityType type() {
		return ActivityType.INITIATE;
	}

	public InitiateActivity(int type, int number) {
		this.resType = type;
		this.number = number;
	}
}

class RequestActivity extends Activity {

	public int resType;
	public int number;

	public ActivityType type() {
		return ActivityType.REQUEST;
	}

	public RequestActivity(int type, int number) {
		this.resType = type;
		this.number = number;
	}
}

class ReleaseActivity extends Activity {

	public int resType;
	public int number;

	public ActivityType type() {
		return ActivityType.RELEASE;
	}

	public ReleaseActivity(int type, int number) {
		this.resType = type;
		this.number = number;
	}
}

class ComputeActivity extends Activity {

	public int cycle;

	public ActivityType type() {
		return ActivityType.COMPUTE;
	}

	public ComputeActivity(int cycle) {
		this.cycle = cycle;
	}
}

class TerminateActivity extends Activity {

	public ActivityType type() {
		return ActivityType.TERMINATE;
	}
}