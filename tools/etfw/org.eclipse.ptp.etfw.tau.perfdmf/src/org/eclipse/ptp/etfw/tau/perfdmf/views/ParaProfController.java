package org.eclipse.ptp.etfw.tau.perfdmf.views;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;

/**
 * @since 2.0
 */
public class ParaProfController {
	public enum Level {
		DATABASE,
		APPLICATION,
		EXPERIMENT,
		TRIAL
	}

	static class StreamRunner extends Thread {
		private final LinkedBlockingQueue<String> pushQueue;
		private final LinkedBlockingQueue<String> pullQueue;
		InputStream is;
		boolean exception = false;

		StreamRunner(InputStream is) {
			this.is = is;
			pushQueue = null;
			pullQueue = null;
		}

		StreamRunner(InputStream is, LinkedBlockingQueue<String> pushQueue, LinkedBlockingQueue<String> pullQueue) {
			this.is = is;
			this.pushQueue = pushQueue;
			this.pullQueue = pullQueue;
		}

		@Override
		public void run() {
			try {
				final InputStreamReader isr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {

					if (pushQueue == null || pullQueue == null) {
						if (line.contains("Exception")) { //$NON-NLS-1$
							exception = true;
						}
						System.out.println(line);
					} else {
						if (line.startsWith("control sourcecode")) { //$NON-NLS-1$
							pullQueue.add(line);
						} else {
							pushQueue.add(line);
						}
					}
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			// if (pullQueue != null){
			// if(!exception)
			//					pullQueue.add(DONE); //$NON-NLS-1$
			// }

			if (pushQueue != null) {
				pushQueue.add(DONE);
			}
		}
	}

	public class TreeTuple {

		public int id;

		public String name;
		public int dbid;
		public Level level;

		public TreeTuple(String name, int id, int dbid, Level level) {
			this.id = id;
			this.name = name;
			this.dbid = dbid;
			this.level = level;
		}
	}

	private LinkedBlockingQueue<String> pushQueue = null;
	private LinkedBlockingQueue<String> pullQueue = null;
	// private BufferedReader stdin=null;
	private PrintStream stdout = null;
	// private String comBuf="";
	private StreamRunner inRun;
	private StreamRunner errRun;
	private ProcessBuilder pb;
	private Process proc;
	private static final String DATABASES = "databases"; //$NON-NLS-1$
	private static final String APPLICATIONS = "applications"; //$NON-NLS-1$
	private static final String EXPERIMENTS = "experiments"; //$NON-NLS-1$

	private static final String TRIALS = "trials"; //$NON-NLS-1$

	private boolean canRun = true;
	public static TreeTuple EMPTY;

	private IBuildLaunchUtils utilBlob = null;

	/**
	 * @since 3.0
	 */
	public boolean pullReady = false;
	/**
	 * @since 3.0
	 */
	public static final String RESTART = "RESTART"; //$NON-NLS-1$
	/**
	 * @since 3.0
	 */
	public static final String DONE = "DONE"; //$NON-NLS-1$

	private static final String SPACE = " "; //$NON-NLS-1$

	/**
	 * @since 3.0
	 */
	public ParaProfController(IBuildLaunchUtils utilBlob) {
		this.utilBlob = utilBlob;
		createProcess();

	}

	private void createProcess() {
		final String taubin = utilBlob.getToolPath("tau"); //$NON-NLS-1$
		IFileStore paraprof = utilBlob.getFile(taubin);// + File.separator + "paraprof";
		paraprof = paraprof.getChild("paraprof"); //$NON-NLS-1$
		EMPTY = new TreeTuple("None", -1, -1, Level.DATABASE); //$NON-NLS-1$
		// File checkp = new File(paraprof);
		if (!paraprof.fetchInfo().exists()) {
			canRun = false;
			return;
		}

		pushQueue = new LinkedBlockingQueue<String>();
		pullQueue = new LinkedBlockingQueue<String>();
		final List<String> command = new ArrayList<String>();
		command.add(paraprof.toURI().getPath());
		command.add("--control"); //$NON-NLS-1$
		pb = new ProcessBuilder(command);

		proc = null;
		try {
			proc = pb.start();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		errRun = new StreamRunner(proc.getErrorStream());
		inRun = new StreamRunner(proc.getInputStream(), pushQueue, pullQueue);
		stdout = new PrintStream(new BufferedOutputStream(proc.getOutputStream()));
		errRun.start();
		inRun.start();
		pullReady = true;
	}

	public List<TreeTuple> getApplications(int db) {
		return getInfo(APPLICATIONS, db, -1, Level.APPLICATION);
	}

	public List<TreeTuple> getDatabases() {
		final List<TreeTuple> l = getInfo(DATABASES, -1, -1, Level.DATABASE);
		TreeTuple t;
		for (int i = 0; i < l.size(); i++) {
			final String name = l.get(i).name;
			if (name.equals("Default") || name.equals("default")) { //$NON-NLS-1$ //$NON-NLS-2$
				t = l.remove(i);
				l.add(0, t);
				break;
			}
		}
		return l;
	}

	public List<TreeTuple> getExperiments(int db, int ap) {
		return getInfo(EXPERIMENTS, db, ap, Level.EXPERIMENT);
	}

	private List<TreeTuple> getInfo(String type, int dbid, int hid, Level level) {
		final List<TreeTuple> out = new ArrayList<TreeTuple>();

		if (!canRun) {
			return out;
		}

		String comBuf = "control list " + type; //$NON-NLS-1$
		if (dbid > -1) {
			comBuf += " " + dbid; //$NON-NLS-1$

			if (hid > -1) {
				comBuf += " " + hid; //$NON-NLS-1$
			}
		}
		final int res = issueCommand(comBuf);
		if (res != 0) {
			return out;
		}
		final List<String> l = getResults();

		for (final String s : l) {
			if (s.startsWith("control return")) { //$NON-NLS-1$
				final String[] split = s.split(" "); //$NON-NLS-1$
				final int id = Integer.parseInt(split[2]);
				String name = split[3];
				for (int j = 4; j < split.length; j++) {
					name += " " + split[j]; //$NON-NLS-1$
				}
				out.add(new TreeTuple(name, id, dbid, level));
			}
		}
		return out;
	}

	public BlockingQueue<String> getPullQueue() {
		return pullQueue;
	}

	private List<String> getResults() {
		boolean done = false;
		final List<String> l = new ArrayList<String>();
		if (pushQueue != null) {
			while (!done) {// for(int i=0;i<dex;i++){
				String s = ""; //$NON-NLS-1$
				try {
					s = pushQueue.take();
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				if (s.equals("control endreturn") || s.equals(DONE)) { //$NON-NLS-1$ 
					done = true;
				} else {
					l.add(s);
				}
			}
		}
		return l;
	}

	public List<TreeTuple> getTrials(int db, int ex) {
		return getInfo(TRIALS, db, ex, Level.TRIAL);
	}

	private int issueCommand(String command) {

		if (!canRun) {
			return -1;
		}

		if (stdout != null) {
			stdout.println(command);
			stdout.flush();
		}

		if (errRun.exception) {
			System.out.println("Restarting Paraprof"); //$NON-NLS-1$
			killProcess();
			createProcess();
			return -1;
		}
		return 0;
	}

	private void killProcess() {
		pushQueue = null;
		pullReady = false;
		pullQueue.add(RESTART);
		pullQueue = null;
		pb = null;
		errRun = null;
		inRun = null;
	}

	public void openManager() {
		final String comBuf = "control open manager"; //$NON-NLS-1$
		issueCommand(comBuf);
	}

	public void openTrial(int dbid, int tid) {
		final String comBuf = "control load " + dbid + " " + tid; //$NON-NLS-1$ //$NON-NLS-2$
		issueCommand(comBuf);
	}

	/**
	 * @since 3.0
	 */
	public TreeTuple uploadTrial(IFileStore profile, int dbid, String app, String exp, String tri) {
		final String comBuf = "control upload " + profile.toURI().getPath() + SPACE + dbid + SPACE + app + SPACE + exp + SPACE + tri; //$NON-NLS-1$
		final int res = issueCommand(comBuf);
		if (res != 0) {
			return null;
		}
		TreeTuple tt = null;
		final List<String> l = getResults();

		for (final String s : l) {
			if (s.startsWith("control return")) { //$NON-NLS-1$
				final String[] split = s.split(" "); //$NON-NLS-1$
				final int id = Integer.parseInt(split[2]);
				tt = new TreeTuple(tri, id, dbid, Level.TRIAL);
			}
		}

		return tt;
	}

}
