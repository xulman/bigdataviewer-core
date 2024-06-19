package bdv.tools.benchmarks;

import java.util.List;
import java.util.ArrayList;

public class TimeReporter {

	private TimeReporter() {}

	private static TimeReporter INSTANCE = null;

	synchronized
	public static TimeReporter getInstance() {
		if (INSTANCE == null) INSTANCE = new TimeReporter();
		return INSTANCE;
	}

	private final List<Long> collectedTimes = new ArrayList<>(100);
	private final List<String> collectedNames = new ArrayList<>(100);
	private long initTime = -1;
	private int wantedReportsNumber = -1;

	public void startNowAndReportAfter(int wantedReportsNumber) {
		this.wantedReportsNumber = wantedReportsNumber;
		this.collectedTimes.clear();
		this.collectedNames.clear();
		this.initTime = System.currentTimeMillis();
	}

	synchronized
	public void reportWorkFinished(final String callerID) {
		if (this.collectedTimes.size() < this.wantedReportsNumber) {
			this.collectedTimes.add( System.currentTimeMillis() );
			this.collectedNames.add( callerID );
		}

		if (this.collectedTimes.size() == this.wantedReportsNumber) {
			long prevTime = initTime;
			for (int i = 0; i < wantedReportsNumber; ++i) {
				long time = collectedTimes.get(i);
				System.out.println(collectedNames.get(i)+": Delay from first "+(double)(time-initTime)/1000.0
						  +" seconds; from previous "+(double)(time-prevTime)/1000.0+" seconds");
				prevTime = time;
			}
			this.wantedReportsNumber = -1;
		}
	}
}
