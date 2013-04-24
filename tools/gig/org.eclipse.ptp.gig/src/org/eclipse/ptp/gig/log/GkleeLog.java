/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ui.statushandlers.StatusManager;

public class GkleeLog {

	// emacs:code:block0:thread0:file0:line0:block1:thread1:file1:line1
	private static final Pattern emacsPattern = Pattern.compile("emacs:(.*):(\\d*):(\\d*):(.*):(\\d*):(\\d*):(\\d*):(.*):(\\d*)"); //$NON-NLS-1$
	private static final Pattern warpDivergencePattern = Pattern
			.compile("\\** Start checking warp divergence \\**"); //$NON-NLS-1$
	private static final Pattern threadsDivergedPattern = Pattern
			.compile("In warp (\\d+), threads are diverged into following sub-sets: "); //$NON-NLS-1$
	private static final Pattern setPattern = Pattern
			.compile("Set (\\d+):"); //$NON-NLS-1$
	private static final Pattern aveBCPattern = Pattern
			.compile("BC:(\\d+):(\\d+):(\\d+):(\\d+):(\\d+):(\\d+):(\\d+)"); //$NON-NLS-1$
	private static final Pattern aveMCPattern = Pattern
			.compile("MC:(\\d+):(\\d+):(\\d+):(\\d+):(\\d+):(\\d+):(\\d+)"); //$NON-NLS-1$
	private static final Pattern aveWDPattern = Pattern
			.compile("WD:(\\d+):(\\d+):(\\d+):(\\d+):(\\d+):(\\d+):(\\d+)"); //$NON-NLS-1$
	private static final Pattern memoryCoalescingPattern = Pattern
			.compile("\\** Start checking memory coalescing at DeviceMemory at capability: .* \\**"); //$NON-NLS-1$
	private final List<WarpDivergence> warpDivergences = new ArrayList<WarpDivergence>();
	private final OrganizedThreadInfo assertions = new OrganizedThreadInfo();
	private final OrganizedThreadInfo potentialDeadlocksVariedLength = new OrganizedThreadInfo();
	private final OrganizedThreadInfo potentialDeadlocksSameLength = new OrganizedThreadInfo();
	private final OrganizedThreadInfo deadlocks = new OrganizedThreadInfo();
	private final OrganizedThreadInfo memoryCoalescings = new OrganizedThreadInfo();
	private final OrganizedThreadInfo missingVolatiles = new OrganizedThreadInfo();
	// These are a bunch of read/write races, see the parse function for what they mean
	private final OrganizedThreadInfo rwraws = new OrganizedThreadInfo(), rwbds = new OrganizedThreadInfo(),
			rws = new OrganizedThreadInfo(), rrbcs = new OrganizedThreadInfo(), rwbcs = new OrganizedThreadInfo(),
			wwrwbs = new OrganizedThreadInfo(), wwrws = new OrganizedThreadInfo(),
			wwrawbs = new OrganizedThreadInfo(), wwraws = new OrganizedThreadInfo(),
			wwbdbs = new OrganizedThreadInfo(), wwbds = new OrganizedThreadInfo(), wws = new OrganizedThreadInfo(),
			wwbcs = new OrganizedThreadInfo();
	private Statistic bankConflictStats, memoryCoalescingStats, warpDivergenceStats;
	// describes which line in the file the main memory coalescing information is. May be null.
	private Integer memoryCoalescingLocation;

	public GkleeLog(InputStream is, IFile logFile) throws LogException {
		// reset the static info
		ThreadInfo.reset();

		// begin parsing one line at a time
		final Scanner scanner = new Scanner(is);

		// lineNumber tracks which line we are on, so that we can make reference to specific lines of the log file
		int lineNumber = 0;
		String line;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			lineNumber++;
			Matcher matcher = emacsPattern.matcher(line);
			if (matcher.matches()) {
				parse(matcher, logFile, lineNumber);
				continue;
			}
			matcher = warpDivergencePattern.matcher(line);
			if (matcher.matches()) {
				line = scanner.nextLine();
				lineNumber++;
				while (!line.equals("")) { //$NON-NLS-1$
					matcher = threadsDivergedPattern.matcher(line);
					if (matcher.matches()) {
						final int warp = Integer.parseInt(matcher.group(1));
						final List<int[]> sets = new ArrayList<int[]>();

						line = scanner.nextLine();
						lineNumber++;
						matcher = setPattern.matcher(line);
						while (matcher.matches()) {
							line = scanner.nextLine();
							lineNumber++;
							final Scanner scan = new Scanner(line);
							final List<Integer> threads = new ArrayList<Integer>();
							while (scan.hasNext()) {
								scan.next();
								final int thread = Integer.parseInt(scan.next());
								scan.next();
								threads.add(thread);
							}
							final int[] threadsArray = new int[threads.size()];
							for (int i = 0; i < threadsArray.length; i++) {
								threadsArray[i] = threads.get(i);
							}
							sets.add(threadsArray);

							line = scanner.nextLine();
							lineNumber++;
							matcher = setPattern.matcher(line);
						}

						final WarpDivergence warpDivergence = new WarpDivergence(sets, warp);
						warpDivergences.add(warpDivergence);
						continue;
					}
					line = scanner.nextLine();
					lineNumber++;
				}
				continue;
			}
			matcher = aveBCPattern.matcher(line);
			if (matcher.matches()) {
				final int aveWarp = Integer.parseInt(matcher.group(2));
				final int numWarps = Integer.parseInt(matcher.group(3));
				final int totalWarps = Integer.parseInt(matcher.group(4));
				final int aveBI = Integer.parseInt(matcher.group(5));
				final int numBIs = Integer.parseInt(matcher.group(6));
				final int totalBIs = Integer.parseInt(matcher.group(7));
				bankConflictStats = new Statistic(aveWarp, numWarps, totalWarps, aveBI, numBIs, totalBIs, logFile, lineNumber);
				continue;
			}
			matcher = aveMCPattern.matcher(line);
			if (matcher.matches()) {
				final int aveWarp = Integer.parseInt(matcher.group(2));
				final int numWarps = Integer.parseInt(matcher.group(3));
				final int totalWarps = Integer.parseInt(matcher.group(4));
				final int aveBI = Integer.parseInt(matcher.group(5));
				final int numBIs = Integer.parseInt(matcher.group(6));
				final int totalBIs = Integer.parseInt(matcher.group(7));
				memoryCoalescingStats = new Statistic(aveWarp, numWarps, totalWarps, aveBI, numBIs, totalBIs, logFile,
						lineNumber);
				continue;
			}
			matcher = aveWDPattern.matcher(line);
			if (matcher.matches()) {
				final int aveWarp = Integer.parseInt(matcher.group(2));
				final int numWarps = Integer.parseInt(matcher.group(3));
				final int totalWarps = Integer.parseInt(matcher.group(4));
				final int aveBI = Integer.parseInt(matcher.group(5));
				final int numBIs = Integer.parseInt(matcher.group(6));
				final int totalBIs = Integer.parseInt(matcher.group(7));
				warpDivergenceStats = new Statistic(aveWarp, numWarps, totalWarps, aveBI, numBIs, totalBIs, logFile, lineNumber);
				continue;
			}
			matcher = memoryCoalescingPattern.matcher(line);
			if (matcher.matches()) {
				this.memoryCoalescingLocation = lineNumber;
				continue;
			}
		}
		scanner.close();

		// Done parsing, now prepare the data for display
		// this part will have failed to parse if the log file had a problem
		if (this.warpDivergences.size() == 0) {
			throw new LogException(Messages.BAD_LOG_FILE);
		}
		final int threadsPerWarp = this.warpDivergences.get(0).getThreadsPerWarp();
		OrganizedThreadInfo.setThreadsPerWarp(threadsPerWarp);
		this.assertions.organize();
		this.memoryCoalescings.organize();
		this.deadlocks.organize();
		this.missingVolatiles.organize();
		this.potentialDeadlocksSameLength.organize();
		this.potentialDeadlocksVariedLength.organize();
		this.rrbcs.organize();
		this.rwbcs.organize();
		this.rwbds.organize();
		this.rwraws.organize();
		this.rws.organize();
		this.wwbcs.organize();
		this.wwbdbs.organize();
		this.wwbds.organize();
		this.wwrawbs.organize();
		this.wwraws.organize();
		this.wwrwbs.organize();
		this.wwrws.organize();
		this.wws.organize();
	}

	public OrganizedThreadInfo getAssertions() {
		return this.assertions;
	}

	public int getBankConflictRate() {
		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		final boolean bank = preferenceStore.getBoolean(Messages.BANK_OR_WARP);
		if (bank) {
			return this.bankConflictStats.getAverageBank();
		}
		return this.bankConflictStats.getAverageWarp();
	}

	public Statistic getBankConflictStats() {
		return this.bankConflictStats;
	}

	public OrganizedThreadInfo getDeadlocks() {
		return this.deadlocks;
	}

	public OrganizedThreadInfo getMemoryCoalescing() {
		return this.memoryCoalescings;
	}

	public Integer getMemoryCoalescingLocation() {
		return this.memoryCoalescingLocation;
	}

	public int getMemoryCoalescingRate() {
		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		final boolean bank = preferenceStore.getBoolean(Messages.BANK_OR_WARP);
		if (bank) {
			return this.memoryCoalescingStats.getAverageBank();
		}
		return this.memoryCoalescingStats.getAverageWarp();
	}

	public Statistic getMemoryCoalescingStats() {
		return this.memoryCoalescingStats;
	}

	public OrganizedThreadInfo getMissingVolatile() {
		return this.missingVolatiles;
	}

	public OrganizedThreadInfo getPotentialSame() {
		return this.potentialDeadlocksSameLength;
	}

	public OrganizedThreadInfo getPotentialVaried() {
		return this.potentialDeadlocksVariedLength;
	}

	public OrganizedThreadInfo getRrbc() {
		return this.rrbcs;
	}

	public OrganizedThreadInfo getRw() {
		return this.rws;
	}

	public OrganizedThreadInfo getRwbc() {
		return this.rwbcs;
	}

	public OrganizedThreadInfo getRwbd() {
		return this.rwbds;
	}

	public OrganizedThreadInfo getRwraw() {
		return this.rwraws;
	}

	public int getWarpDivergenceRate() {
		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		final boolean bank = preferenceStore.getBoolean(Messages.BANK_OR_WARP);
		if (bank) {
			return this.warpDivergenceStats.getAverageBank();
		}
		return this.warpDivergenceStats.getAverageWarp();
	}

	public List<WarpDivergence> getWarpDivergences() {
		return this.warpDivergences;
	}

	public Statistic getWarpDivergenceStats() {
		return this.warpDivergenceStats;
	}

	public OrganizedThreadInfo getWw() {
		return this.wws;
	}

	public OrganizedThreadInfo getWwbc() {
		return this.wwbcs;
	}

	public OrganizedThreadInfo getWwbd() {
		return this.wwbds;
	}

	public OrganizedThreadInfo getWwbdb() {
		return this.wwbdbs;
	}

	public OrganizedThreadInfo getWwraw() {
		return this.wwraws;
	}

	public OrganizedThreadInfo getWwrawb() {
		return this.wwrawbs;
	}

	public OrganizedThreadInfo getWwrw() {
		return this.wwrws;
	}

	public OrganizedThreadInfo getWwrwb() {
		return this.wwrwbs;
	}

	/*
	 * Parses the emacs pattern
	 */
	private void parse(Matcher matcher, IFile logFile, int line) {
		// emacs:code:block0:thread0:file0:line0:block1:thread1:file1:line1
		final String code = matcher.group(1);
		final String block0 = matcher.group(2);
		final String thread0 = matcher.group(3);
		final String file0 = matcher.group(4);
		final String line0 = matcher.group(5);
		final String block1 = matcher.group(6);
		final String thread1 = matcher.group(7);
		final String file1 = matcher.group(8);
		final String line1 = matcher.group(9);
		final ThreadInfo threadInfo0 = new ThreadInfo(block0, thread0, file0, line0, logFile);
		final ThreadInfo threadInfo1 = new ThreadInfo(block1, thread1, file1, line1, logFile);

		// this might fail in future releases of gklee, it is important to log the failure, but don't crash the logging
		try {
			parse(code, new TwoThreadInfo(threadInfo0, threadInfo1, logFile, line));
		} catch (final LogException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.LOG_EXCEPTION, e));
		}
	}

	/*
	 * parses the code part of the emacs line and puts the already parsed TwoThreadInfo in the correct group
	 */
	private void parse(String code, TwoThreadInfo threadInfo) throws LogException {
		/*
		 * ("assert" . "Assertion violation")
		 * ("wwrwb" . "Write write race within warp benign")
		 * ("wwrw" . "Write write race within warp")
		 * ("wwrawb" . "Write write race across warps benign")
		 * ("wwraw" . "Write write race across warps")
		 * ("rwraw" . "Read write race across warps")
		 * ("wwbdb" . "Write write branch divergence race benign")
		 * ("wwbd" . "Write write branch divergence race")
		 * ("rwbd" . "Read write branch divergence race")
		 * ("rw" . "Write read race")
		 * ("ww" . "Write write race")
		 * ("dlbm" . "Deadlock due to barrier mismatch\nlocation reported is first thread in divergent set")
		 * ("dbs" . "Potential deadlock -- different barrier sequence")
		 * ("bsdl" . "Potential deadlock -- barrier sequences of differing length")
		 * ("wwbc" . "Write write bank conflict")
		 * ("rrbc" . "Read read bank conflict")
		 * ("rwbc" . "Read write bank conflict")
		 * ("mc" . "Non-coalesced global memory access")
		 * ("mv" . "Missing volatile")
		 * ("wd" . "Warp divergence") --I'm not seeing this in any log files that should have it
		 */
		// switch on strings not supported in java, but we can still avoid long if-else statements
		switch (code.charAt(0)) {
		case 'a':
			// ("assert" . "Assertion violation")
			if (code.equals("assert")) { //$NON-NLS-1$
				assertions.add(threadInfo);
				return;
			}
			break;
		case 'b':
			// ("bsdl" . "Potential deadlock -- barrier sequences of differing length")
			if (code.equals("bsdl")) { //$NON-NLS-1$
				potentialDeadlocksVariedLength.add(threadInfo);
				return;
			}
			break;
		case 'd':
			/*
			 * ("dlbm" . "Deadlock due to barrier mismatch\nlocation reported is first thread in divergent set")
			 * ("dbs" . "Potential deadlock -- different barrier sequence")
			 */
			if (code.equals("dlbm")) { //$NON-NLS-1$
				deadlocks.add(threadInfo);
				return;
			}
			else if (code.equals("dbs")) { //$NON-NLS-1$
				potentialDeadlocksSameLength.add(threadInfo);
				return;
			}
			break;
		case 'm':
			/*
			 * ("mc" . "Non-coalesced global memory access")
			 * ("mv" . "Missing volatile")
			 */
			if (code.equals("mc")) { //$NON-NLS-1$
				memoryCoalescings.add(threadInfo);
				return;
			}
			else if (code.equals("mv")) { //$NON-NLS-1$
				missingVolatiles.add(threadInfo);
				return;
			}
			break;
		case 'r':
			/*
			 * ("rwraw" . "Read write race across warps")
			 * ("rwbd" . "Read write branch divergence race")
			 * ("rw" . "Write read race")
			 * ("rrbc" . "Read read bank conflict")
			 * ("rwbc" . "Read write bank conflict")
			 */
			if (code.equals("rwraw")) { //$NON-NLS-1$
				rwraws.add(threadInfo);
				return;
			}
			else if (code.equals("rwbd")) { //$NON-NLS-1$
				rwbds.add(threadInfo);
				return;
			}
			else if (code.equals("rw")) { //$NON-NLS-1$
				rws.add(threadInfo);
				return;
			}
			else if (code.equals("rrbc")) { //$NON-NLS-1$
				rrbcs.add(threadInfo);
				return;
			}
			else if (code.equals("rwbc")) { //$NON-NLS-1$
				rwbcs.add(threadInfo);
				return;
			}
			break;
		case 'w':
			/*
			 * ("wwrwb" . "Write write race within warp benign")
			 * ("wwrw" . "Write write race within warp")
			 * ("wwrawb" . "Write write race across warps benign")
			 * ("wwraw" . "Write write race across warps")
			 * ("wwbdb" . "Write write branch divergence race benign")
			 * ("wwbd" . "Write write branch divergence race")
			 * ("ww" . "Write write race")
			 * ("wwbc" . "Write write bank conflict")
			 * ("wd" . "Warp divergence") --I'm not seeing this in any log files that should have it
			 */
			if (code.equals("wwrwb")) { //$NON-NLS-1$
				wwrwbs.add(threadInfo);
				return;
			}
			else if (code.equals("wwrw")) { //$NON-NLS-1$
				wwrws.add(threadInfo);
				return;
			}
			else if (code.equals("wwrawb")) { //$NON-NLS-1$
				wwrawbs.add(threadInfo);
				return;
			}
			else if (code.equals("wwraw")) { //$NON-NLS-1$
				wwraws.add(threadInfo);
				return;
			}
			else if (code.equals("wwbdb")) { //$NON-NLS-1$
				wwbdbs.add(threadInfo);
				return;
			}
			else if (code.equals("wwbd")) { //$NON-NLS-1$
				wwbds.add(threadInfo);
				return;
			}
			else if (code.equals("ww")) { //$NON-NLS-1$
				wws.add(threadInfo);
				return;
			}
			else if (code.equals("wwbc")) { //$NON-NLS-1$
				wwbcs.add(threadInfo);
				return;
			}
			break;
		}
		throw new LogException(Messages.LOG_EXCEPTION + code);
	}

}
