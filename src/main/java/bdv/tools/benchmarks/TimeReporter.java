package bdv.tools.benchmarks;

public class TimeReporter {

	private TimeReporter() {}

	private static TimeReporter INSTANCE = null;

	synchronized
	public static TimeReporter getInstance() {
		if (INSTANCE == null) INSTANCE = new TimeReporter();
		return INSTANCE;
	}

	private long initTime = -1;
	private long lastReportTime = -1;
	private int maxReportsNumber = -1;

	/**
	 * This method is intended for a (one) caller that starts some action and waits for
	 * a number 'wantedReportsNumber' of workers, that got triggered by this action,
	 * until they report themselves via {@link TimeReporter#reportWorkFinished(String)}.
	 * @param wantedReportsNumber Number of measurements to appear on the console.
	 */
	public void startNowAndReportNotMoreThan(int wantedReportsNumber) {
		this.maxReportsNumber = wantedReportsNumber;
		this.initTime = System.currentTimeMillis();
		this.lastReportTime = this.initTime;
	}

	/**
	 * This is intended for workers (see {@link TimeReporter#startNowAndReportNotMoreThan(int)})
	 * to report autonomously and immediately after they finished their current work. The method
	 * is monitoring not to display more than a wanted number of reports (see again the above method).
	 * @param callerID Identification of the worker to appear on the console.
	 */
	synchronized
	public void reportWorkFinished(final String callerID) {
		if (this.maxReportsNumber > 0) {
			maxReportsNumber -= 1;

			long time = System.currentTimeMillis();
			System.out.println(callerID+": Delay from init "+(double)(time-initTime)/1000.0
					+" seconds; from previous "+(double)(time-lastReportTime)/1000.0+" seconds");
			lastReportTime = time;
		}
	}
}
