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
import org.eclipse.ptp.etfw.internal.IBuildLaunchUtils;

/**
 * @since 2.0
 */
public class ParaProfController {
	public enum Level {
		DATABASE, APPLICATION, EXPERIMENT, TRIAL
	}

	public class TreeTuple {

		public TreeTuple(String name, int id, int dbid, Level level) {
			this.id = id;
			this.name = name;
			this.dbid = dbid;
			this.level = level;
		}

		public int id;
		public String name;
		public int dbid;
		public Level level;
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
	private IBuildLaunchUtils utilBlob=null;
	public ParaProfController(IBuildLaunchUtils utilBlob) {
		this.utilBlob=utilBlob;
		createProcess();

	}
	public boolean pullReady=false;
public static final String RESTART="RESTART";
public static final String DONE="DONE";
	private void killProcess() {
		pushQueue = null;
		pullReady=false;
		pullQueue.add(RESTART);
		pullQueue = null;
		pb = null;
		errRun = null;
		inRun = null;
	}

	private void createProcess() {
		String taubin=utilBlob.getToolPath("tau");
		IFileStore paraprof = utilBlob.getFile(taubin);// + File.separator + "paraprof"; //$NON-NLS-1$ //$NON-NLS-2$
		paraprof=paraprof.getChild("paraprof");
		EMPTY = new TreeTuple("None", -1, -1, Level.DATABASE); //$NON-NLS-1$
		//File checkp = new File(paraprof);
		if (!paraprof.fetchInfo().exists()) {
			canRun = false;
			return;
		}

		pushQueue = new LinkedBlockingQueue<String>();
		pullQueue = new LinkedBlockingQueue<String>();
		List<String> command = new ArrayList<String>();
		command.add(paraprof.toURI().getPath());
		command.add("--control"); //$NON-NLS-1$
		pb = new ProcessBuilder(command);

		proc = null;
		try {
			proc = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		errRun = new StreamRunner(proc.getErrorStream());
		inRun = new StreamRunner(proc.getInputStream(), pushQueue, pullQueue);
		stdout = new PrintStream(new BufferedOutputStream(proc.getOutputStream()));
		errRun.start();
		inRun.start();
		pullReady=true;
	}

	public List<TreeTuple> getDatabases() {
		List<TreeTuple> l = getInfo(DATABASES, -1, -1, Level.DATABASE);
		TreeTuple t;
		for (int i = 0; i < l.size(); i++) {
			String name = l.get(i).name;
			if (name.equals("Default") || name.equals("default")) { //$NON-NLS-1$ //$NON-NLS-2$
				t = l.remove(i);
				l.add(0, t);
				break;
			}
		}
		return l;
	}

	public List<TreeTuple> getApplications(int db) {
		return getInfo(APPLICATIONS, db, -1, Level.APPLICATION);
	}

	public List<TreeTuple> getExperiments(int db, int ap) {
		return getInfo(EXPERIMENTS, db, ap, Level.EXPERIMENT);
	}

	public List<TreeTuple> getTrials(int db, int ex) {
		return getInfo(TRIALS, db, ex, Level.TRIAL);
	}

	private List<TreeTuple> getInfo(String type, int dbid, int hid, Level level) {
		List<TreeTuple> out = new ArrayList<TreeTuple>();

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
		int res = issueCommand(comBuf);
		if (res != 0)
			return out;
		List<String> l = getResults();

		for (String s : l) {
			if (s.startsWith("control return")) { //$NON-NLS-1$
				String[] split = s.split(" "); //$NON-NLS-1$
				int id = Integer.parseInt(split[2]);
				String name = split[3];
				for (int j = 4; j < split.length; j++) {
					name += " " + split[j]; //$NON-NLS-1$
				}
				out.add(new TreeTuple(name, id, dbid, level));
			}
		}
		return out;
	}
	private static final String SPACE=" ";
	public TreeTuple uploadTrial(IFileStore profile, int dbid, String app, String exp, String tri) {
		String comBuf = "control upload " + profile.toURI().getPath() + SPACE + dbid + SPACE + app + SPACE + exp + SPACE + tri; //$NON-NLS-1$ //$NON-NLS-2$ 
		int res = issueCommand(comBuf);
		if (res != 0)
			return null;
		TreeTuple tt = null;
		List<String> l = getResults();

		for (String s : l) {
			if (s.startsWith("control return")) { //$NON-NLS-1$
				String[] split = s.split(" "); //$NON-NLS-1$
				int id = Integer.parseInt(split[2]);
				tt = new TreeTuple(tri, id, dbid, Level.TRIAL);
			}
		}

		return tt;
	}

	private List<String> getResults() {
		boolean done = false;
		List<String> l = new ArrayList<String>();
		if (pushQueue != null)
			while (!done) {// for(int i=0;i<dex;i++){
				String s = ""; //$NON-NLS-1$
				try {
					s = pushQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (s.equals("control endreturn") || s.equals(DONE)) { //$NON-NLS-1$ //$NON-NLS-2$
					done = true;
				} else {
					l.add(s);
				}
			}
		return l;
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

	public void openTrial(int dbid, int tid) {
		String comBuf = "control load " + dbid + " " + tid; //$NON-NLS-1$ //$NON-NLS-2$
		issueCommand(comBuf);
	}

	public void openManager() {
		String comBuf = "control open manager"; //$NON-NLS-1$
		issueCommand(comBuf);
	}

	public BlockingQueue<String> getPullQueue() {
		return pullQueue;
	}

	static class StreamRunner extends Thread {
		private final LinkedBlockingQueue<String> pushQueue;
		private final LinkedBlockingQueue<String> pullQueue;
		InputStream is;
		boolean exception = false;

		StreamRunner(InputStream is, LinkedBlockingQueue<String> pushQueue, LinkedBlockingQueue<String> pullQueue) {
			this.is = is;
			this.pushQueue = pushQueue;
			this.pullQueue = pullQueue;
		}

		StreamRunner(InputStream is) {
			this.is = is;
			pushQueue = null;
			pullQueue = null;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
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
						} else
							pushQueue.add(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
//			if (pullQueue != null){
//				if(!exception)
//					pullQueue.add(DONE); //$NON-NLS-1$
//			}
				
			if (pushQueue != null)
				pushQueue.add(DONE); //$NON-NLS-1$
		}
	}

}
