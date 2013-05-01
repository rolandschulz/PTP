/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.internal.gem.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.gem.GemPlugin;
import org.eclipse.ptp.internal.gem.preferences.PreferenceConstants;

/**
 * This class represents all information for each MPI call (transition) in every
 * interleaving explored. This is the central source of information used by all
 * components within GEM.
 */
public class Transitions {

	private final ArrayList<ArrayList<Envelope>> transitionList;
	private final ArrayList<HashMap<String, Envelope>> errorCalls;
	private HashMap<Integer, String> irrelevantBarriers;
	private HashMap<Integer, Envelope> resourceLeaks;
	private HashMap<Integer, String> typeMismatches;
	private ArrayList<Integer> deadlockInterleavings;
	private int numRanks;
	public int currentInterleaving;
	public int currentTransitionIndex;
	private int deadlockIndex;
	private boolean deadlock;
	private boolean assertionViolation;
	private boolean resourceLeak;
	private boolean fib;

	/**
	 * Constructor
	 * 
	 * @param logFile
	 *            The Resource representing the log file to parse.
	 * @throws ParseException
	 */
	public Transitions(IFile logFile) throws ParseException {
		this.transitionList = new ArrayList<ArrayList<Envelope>>();
		this.errorCalls = new ArrayList<HashMap<String, Envelope>>();
		this.irrelevantBarriers = null;
		this.resourceLeaks = null;
		this.typeMismatches = null;

		// Add null as padding so we can be one-based later
		this.errorCalls.add(null);

		this.numRanks = -1;
		this.deadlock = false;
		this.assertionViolation = false;
		this.resourceLeak = false;
		parseLogFile(logFile);
	}

	/**
	 * Moves to the deadlock interleaving, or if there are multiple
	 * interleavings, it cycles through all.
	 * 
	 * @param none
	 * @return boolean True if the operation was successful, false otherwise.
	 */
	synchronized public boolean deadlockInterleaving() {

		if (!this.deadlock) {
			return false;
		}

		this.currentInterleaving = this.deadlockInterleavings.get(this.deadlockIndex) - 1;
		this.currentTransitionIndex = -1;

		// This is to cycle through the deadlock interleavings
		this.deadlockIndex++;
		this.deadlockIndex = this.deadlockIndex % this.deadlockInterleavings.size();
		return true;
	}

	/**
	 * Returns a list of all envelopes involved in the collective call held in
	 * the Envelope that is passed in.
	 * 
	 * @param env
	 *            The Envelope holding the collective call.
	 * @return ArrayList<Envelope> A list of all Envelopes involved with that
	 *         call.
	 */
	// TODO This is not currently being used
	public ArrayList<Envelope> getCollectiveTransitions(Envelope env) {
		if (env.getIssueIndex() < 0) {
			return null;
		}

		return env.getCommunicator_matches();
	}

	/**
	 * Returns the interleaving current being displayed.
	 * 
	 * @param none
	 * @return int The current interleaving.
	 */
	public int getCurrentInterleaving() {
		return this.currentInterleaving + 1;
	}

	/**
	 * Returns the Envelope involved with the current transition.
	 * 
	 * @param none
	 * @return Envelope The current Envelope in the current interleaving.
	 */
	synchronized public Envelope getCurrentTransition() {
		if (this.currentInterleaving < 0
				|| this.currentInterleaving >= this.transitionList.size()
				|| this.currentTransitionIndex < 0
				|| this.currentTransitionIndex >= this.transitionList.get(this.currentInterleaving).size()) {
			return null;
		}

		return this.transitionList.get(this.currentInterleaving).get(this.currentTransitionIndex);
	}

	/**
	 * Returns the index of the current transition.
	 * 
	 * @param none
	 * @return int The current transition index.
	 */
	public int getCurrentTransitionIndex() {
		return this.currentTransitionIndex;
	}

	/**
	 * Returns the list of deadlock interleavings.
	 * 
	 * @param none
	 * @return ArrayList<Integer> The list of deadlock interleavings.
	 */
	public ArrayList<Integer> getDeadlockInterleavings() {
		return this.deadlockInterleavings;
	}

	/**
	 * Returns the list of HashMaps containing Envelopes involved in the error.
	 * There may be multiple maps due to multiple errors in separate
	 * interleavings.
	 * 
	 * @param none
	 * @return ArrayList<HashMap<String, Envelope>> The list of HashMaps
	 *         containing Envelopes involved in program errors.
	 */
	public ArrayList<HashMap<String, Envelope>> getErrorCalls() {
		return this.errorCalls;
	}

	/**
	 * Walks the appropriate list to the first transition for the specified rank
	 * in the current interleaving.
	 * 
	 * @param rank
	 *            The rank for which to find the first.
	 * @return Envelope The first Envelope in the current interleaving for the
	 *         specified rank.
	 */
	synchronized public Envelope getFirstTransition(int rank) {

		boolean valid = true;
		if (rank < 0 || rank > this.numRanks) {
			valid = false;
		}
		final int size = this.transitionList.get(this.currentInterleaving).size();
		for (int i = 0; i < size; i++) {
			final Envelope env = this.transitionList.get(this.currentInterleaving).get(i);
			if (!valid || env.getRank() == rank) {
				this.currentTransitionIndex = i;
				return env;
			}
		}
		return null;

	}

	/**
	 * Returns an ArrayList of all envelopes in the interleaving that is passed
	 * in.
	 * 
	 * @param interleaving
	 *            An integer representing the desired interleaving (1-based).
	 * @return ArrayList<Envelope> The ArrayList of all envelopes in the
	 *         specified interleaving.
	 */
	public ArrayList<Envelope> getInterleavingEnvelopes(int interleaving) {
		if (interleaving < 1 || interleaving > getTotalInterleavings()) {
			return null;
		}

		return this.transitionList.get(interleaving - 1); // 0-based
	}

	/**
	 * Returns the list of the interleavings listing the calls pertaining to
	 * Irrelevant barriers.
	 * 
	 * @param none
	 * @return HashMap<Integer, String> The unique set of irrelevant barriers.
	 */
	public HashMap<Integer, String> getIrrelevantBarriers() {
		return this.irrelevantBarriers;
	}

	/**
	 * Walks the appropriate list to the last transition for the specified rank
	 * in the current interleaving.
	 * 
	 * @param rank
	 *            The rank to find the last transition for.
	 * @return Envelope The last Envelope in the current interleaving for the
	 *         specified rank.
	 */
	synchronized public Envelope getLastTransition(int rank) {
		boolean valid = true;
		if (rank < 0 || rank > this.numRanks) {
			valid = false;
		}
		final int size = this.transitionList.get(this.currentInterleaving).size() - 1;
		for (int i = size; i >= 0; i--) {
			final Envelope env = this.transitionList.get(this.currentInterleaving).get(i);
			if (!valid || env.getRank() == rank) {
				this.currentTransitionIndex = i;
				return env;
			}
		}
		return null;
	}

	/**
	 * Returns the envelope that matches with the envelope passed in.
	 * 
	 * @param env
	 *            The Envelope whose match we are requesting.
	 * @return Envelope The Envelope that matches, or null if there is no match.
	 */
	public Envelope getMatchingTransition(Envelope env) {
		if (env.getIssueIndex() < 0) {
			return null;
		}

		return env.getMatch_envelope();
	}

	/**
	 * Finds the next transition for the specified rank in the current
	 * interleaving.
	 * 
	 * @param rank
	 *            The rank of the next Envelope to find.
	 * @return Envelope The next Envelope in the current interleaving for the
	 *         specified rank.
	 */
	synchronized public Envelope getNextTransition(int rank) {
		if (this.currentTransitionIndex + 1 >= this.transitionList.get(this.currentInterleaving).size()) {
			return null;
		}

		// If the rank is invalid, treat it as any rank can advance.
		if (rank < 0 || rank >= this.numRanks) {
			this.currentTransitionIndex++;
			return this.transitionList.get(this.currentInterleaving).get(this.currentTransitionIndex);
		}

		final int end = this.transitionList.get(this.currentInterleaving).size();
		for (int i = this.currentTransitionIndex + 1; i < end; i++) {
			final Envelope e = this.transitionList.get(this.currentInterleaving).get(i);
			if (e.getRank() == rank) {
				this.currentTransitionIndex = i;
				return e;
			}
		}
		return null;
	}

	/**
	 * Returns the number of ranks used.
	 * 
	 * @param none
	 * @return int The number of ranks used.
	 */
	public int getNumRanks() {
		return this.numRanks;
	}

	/**
	 * Finds the previous transition for the specified rank in the current
	 * interleaving.
	 * 
	 * @param rank
	 *            The rank to find the previous transition for.
	 * @return Envelope The previous Envelope in the current interleaving for
	 *         the specified rank.
	 */
	synchronized public Envelope getPreviousTransition(int rank) {
		if (this.currentTransitionIndex <= 0) {
			return null;
		}

		// If the rank is invalid, treat it as if any rank can be changed.
		if (rank < 0 || rank >= this.numRanks) {
			this.currentTransitionIndex--;
			return this.transitionList.get(this.currentInterleaving).get(this.currentTransitionIndex);
		}

		for (int i = this.currentTransitionIndex - 1; i >= 0; i--) {
			final Envelope env = this.transitionList.get(this.currentInterleaving).get(i);
			if (env.getRank() == rank) {
				this.currentTransitionIndex = i;
				return env;
			}
		}
		return null;
	}

	/**
	 * Observes the current envelope and discovers which ranks are involved with
	 * it at this moment.
	 * 
	 * @param total
	 *            The integer array containing all ranks involved.
	 * @return String The String representing the ranks of those involved with
	 *         the current call.
	 */
	public String getRanksInvolved(int totalRanksInvolved[]) {
		final Envelope currentEnvelope = this.transitionList.get(this.currentInterleaving).get(this.currentTransitionIndex);
		final int rank = currentEnvelope.getRank();
		String result = Integer.toString(rank);
		final int currentLineNumber = currentEnvelope.getLinenumber();

		// We can safely assume that we are currently on the 1st iteration
		int i = this.currentTransitionIndex;
		final int size = this.transitionList.get(this.currentInterleaving).size();
		int numRanksInvloved = 1;

		// start at next envelope
		for (i++; i < size; i++) {
			final Envelope env = this.transitionList.get(this.currentInterleaving).get(i);
			if (currentLineNumber == env.getLinenumber()) {
				numRanksInvloved++;
				result += "," + env.getRank(); //$NON-NLS-1$
			} else {
				break;
			}
		}

		totalRanksInvolved[0] = numRanksInvloved;
		return result;
	}

	/**
	 * Returns and HashMap of Envelopes that holds each envelope that contains
	 * a resource leak.
	 * 
	 * @param none
	 * @return HashMap<Integer, Envelope> The set of resource leaks.
	 */
	public HashMap<Integer, Envelope> getResourceLeaks() {
		return this.resourceLeaks;
	}

	/**
	 * Returns the number of interleavings found for this source file.
	 * 
	 * @param none
	 * @return int The total number of interleavings.
	 */
	public int getTotalInterleavings() {
		return this.transitionList.size();
	}

	/**
	 * Returns an ArrayList of ArrayLists of Envelopes. Each ArrayList
	 * corresponds to a particular interleaving, and holds envelopes
	 * representing individual MPI call transitions.
	 * 
	 * @param none
	 * @return ArrayList<ArrayList<Envelope>> List representing each MPI Call in
	 *         each possible interleaving.
	 */
	public ArrayList<ArrayList<Envelope>> getTransitionList() {
		return this.transitionList;
	}

	/**
	 * Returns the list of the interleavings listing the calls pertaining to MPI
	 * type mismatches.
	 * 
	 * @param none
	 * @return HashMap<Integer, String>The unique set of MPI type mismatches.
	 */
	public HashMap<Integer, String> getTypeMismatches() {
		return this.typeMismatches;
	}

	/**
	 * Returns a boolean representing whether or not there is an assertion.
	 * violation.
	 * 
	 * @param none
	 * @return boolean True if there was an assertion violation, false
	 *         otherwise.
	 */
	public boolean hasAssertion() {
		return this.assertionViolation;
	}

	/**
	 * Returns a boolean representing whether or not there is a deadlock.
	 * 
	 * @param none
	 * @return boolean True if there was a deadlock, false otherwise.
	 */
	public boolean hasDeadlock() {
		return this.deadlock;
	}

	/**
	 * Returns whether or not the transition list contains an error.
	 * 
	 * @param none
	 * @return boolean True for an error, false otherwise.
	 */
	public boolean hasError() {
		if (this.assertionViolation || this.deadlock) {
			return true;
		}
		final Iterator<HashMap<String, Envelope>> itr = this.errorCalls.iterator();
		while (itr.hasNext()) {
			final HashMap<String, Envelope> currentHash = itr.next();
			if (currentHash != null && !currentHash.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether or not a functionally irrelevant barrier was detected.
	 * 
	 * @param none
	 * @return boolean True if a functionally irrelevant barrier was detected, false otherwise.
	 */
	public boolean hasFIB() {
		return this.fib;
	}

	/**
	 * Returns a boolean representing whether or not there is a next
	 * interleaving from the currentInterleaving.
	 * 
	 * @param none
	 * @return boolean True if there is a next interleaving, false otherwise.
	 */
	synchronized public boolean hasNextInterleaving() {
		return this.currentInterleaving + 1 < this.transitionList.size();
	}

	/**
	 * Returns true if there is a next transition, false otherwise.
	 * 
	 * @param none
	 * @return boolean True if the current interleaving has a next transition,
	 *         false otherwise.
	 */
	synchronized public boolean hasNextTransition() {
		return this.currentTransitionIndex + 1 < this.transitionList.get(
				this.currentInterleaving).size();
	}

	/**
	 * Returns a boolean representing whether or not there is a previous
	 * interleaving from the currentInterleaving.
	 * 
	 * @param none
	 * @return boolean True if there is a previous interleaving, false
	 *         otherwise.
	 */
	synchronized public boolean hasPreviousInterleaving() {
		return this.currentInterleaving != 0;
	}

	/**
	 * Returns true if there is a previous transition, false otherwise.
	 * 
	 * @param none
	 * @return boolean True if the current interleaving has a previous
	 *         transition, false otherwise.
	 */
	// TODO This is not currently being used
	public boolean hasPreviousTransition() {
		return this.currentTransitionIndex > 0;
	}

	/**
	 * Returns a boolean representing whether or not a resource leak was detected.
	 * 
	 * @param none
	 * @return boolean True if a resource leak was detected, false otherwise.
	 */
	public boolean hasResourceLeak() {
		return this.resourceLeak;
	}

	/**
	 * Returns true if there is a next non-repetitive transition, false
	 * otherwise.
	 * 
	 * @param none
	 * @return boolean True if the current interleaving has a valid next
	 *         transition, false otherwise.
	 */
	synchronized public boolean hasValidNextTransition(int rank) {
		if (this.currentTransitionIndex == -1) {
			try {
				this.transitionList.get(this.currentInterleaving).get(this.currentTransitionIndex + 1);
				return true;
			} catch (final Exception e) {
				return false;
			}
		}
		int tempIndex = this.currentTransitionIndex;
		while (tempIndex < this.transitionList.get(this.currentInterleaving).size()) {
			final int line1 = this.transitionList.get(this.currentInterleaving).get(this.currentTransitionIndex).getLinenumber();
			final int line2 = this.transitionList.get(this.currentInterleaving).get(tempIndex).getLinenumber();
			if (line1 == line2) {
				tempIndex++;
				continue;
			} else if (rank == -1 || this.transitionList.get(this.currentInterleaving).get(tempIndex).getRank() == rank) {
				return true;
			}
			tempIndex++;
		}
		return false;
	}

	/**
	 * Returns true if there is a non-repetitive previous transition, false
	 * otherwise.
	 * 
	 * @param none
	 * @return boolean True if the current interleaving has a valid previous
	 *         transition, false otherwise.
	 */
	public boolean hasValidPreviousTransition(int rank) {
		int tempIndex = this.currentTransitionIndex - 1;
		while (tempIndex >= 0) {
			final int line1 = this.transitionList.get(this.currentInterleaving).get(this.currentTransitionIndex).getLinenumber();
			final int line2 = this.transitionList.get(this.currentInterleaving).get(tempIndex).getLinenumber();
			final boolean collective = this.transitionList.get(this.currentInterleaving).get(this.currentTransitionIndex)
					.isCollective();
			if (line1 == line2 && rank == -1 && collective) {
				tempIndex--;
				continue;
			} else if (rank == -1 || this.transitionList.get(this.currentInterleaving).get(tempIndex).getRank() == rank) {
				return true;
			}
			tempIndex--;
		}
		return false;
	}

	/*
	 * This is where the bulk of the p2p and collective matching gets done.
	 */
	private void parseLogFile(IFile logFile) {
		final HashMap<String, Envelope> p2pMatches = new HashMap<String, Envelope>();
		final HashMap<String, ArrayList<Envelope>> collectiveMatches = new HashMap<String, ArrayList<Envelope>>();
		final HashMap<Integer, Integer> issueOrders = new HashMap<Integer, Integer>();
		final ArrayList<HashMap<String, Integer>> collectiveCount = new ArrayList<HashMap<String, Integer>>();
		Scanner scanner = null;
		String line = null;
		int interleaving = -1;
		boolean fileReadSuccess = false;

		try {
			scanner = new Scanner(logFile.getContents());

			// Check for empty log file
			if (!scanner.hasNextLine()) {
				return;
			}

			line = scanner.nextLine();

			try {
				this.numRanks = Integer.parseInt(line);
				fileReadSuccess = true;
			} catch (final NumberFormatException e) {
				GemUtilities.logExceptionDetail(e);
			}
			if (fileReadSuccess) {
				for (int i = 0; i < this.numRanks; i++) {
					collectiveCount.add(new HashMap<String, Integer>());
				}
			}

			while (scanner.hasNext()) {
				// If the analysis has been aborted then stop parsing
				if (GemUtilities.isAborted()) {
					return;
				}

				// Check if we're processing warnings at the end of the log file
				if (!line.startsWith("[")) { //$NON-NLS-1$
					line = scanner.nextLine();
				}

				// identify the deadlock interleavings
				if (line.endsWith("DEADLOCK")) { //$NON-NLS-1$
					if (this.deadlockInterleavings == null) {
						this.deadlock = true;
						this.deadlockInterleavings = new ArrayList<Integer>();
						this.deadlockIndex = 0;
					}
					final StringTokenizer st = new StringTokenizer(line);
					this.deadlockInterleavings.add(Integer.parseInt(st.nextToken()));
					continue;
				}

				// If this line indicates FIB, process all of them
				if (line.equalsIgnoreCase("[FIB]")) { //$NON-NLS-1$
					this.fib = true;
					this.irrelevantBarriers = new HashMap<Integer, String>();
					line = scanner.nextLine();

					// Marks end of FIB list
					while (!line.equals("") && !line.startsWith("[")) { //$NON-NLS-1$ //$NON-NLS-2$
						final StringTokenizer st = new StringTokenizer(line);
						st.nextToken();
						final int lineNumber = Integer.parseInt(st.nextToken());
						this.irrelevantBarriers.put(lineNumber, line);

						// See if this is the end of the log file.
						if (!scanner.hasNextLine()) {
							break;
						}
						line = scanner.nextLine();
					}
					continue;
				}

				// If this line indicates TYPE MISMATCH, process all of them
				if (line.equalsIgnoreCase("[TYPEMISMATCH]")) { //$NON-NLS-1$
					this.typeMismatches = new HashMap<Integer, String>();
					line = scanner.nextLine();

					// Marks end of FIB list
					while (!line.equals("") && !line.startsWith("[")) { //$NON-NLS-1$ //$NON-NLS-2$
						final StringTokenizer st = new StringTokenizer(line);
						st.nextToken();
						final int lineNumber = Integer.parseInt(st.nextToken());
						this.typeMismatches.put(lineNumber, line);

						// See if this is the end of the log file.
						if (!scanner.hasNextLine()) {
							break;
						}
						line = scanner.nextLine();
					}
					continue;
				}

				final Envelope env = Envelope.parse(line);
				if (env == null) {
					throw new ParseException(line, interleaving);
					// continue;
				} else if (env.isAssertion()) {
					this.assertionViolation = true;
					env.setInterleaving((interleaving < 0) ? 1 : interleaving);
				} else if (env.getFunctionName() == "MPI_assert") { //$NON-NLS-1$
					if (scanner.hasNext()) {
						scanner.nextLine();
					}
				} else if (env.isLeak()) {
					if (this.resourceLeaks == null) {
						this.resourceLeak = true;
						this.resourceLeaks = new HashMap<Integer, Envelope>();
					}
					this.resourceLeaks.put(env.getLinenumber(), env);
				}

				// Advance to the next interleaving if needed. Also need to
				// clear all the data structures.
				if (env.getInterleaving() != interleaving) {
					// add a transition list for the interleaving
					this.transitionList.add(new ArrayList<Envelope>());
					interleaving = env.getInterleaving();
					this.errorCalls.add(interleaving, null);
					for (int j = 0; j < this.numRanks; j++) {
						collectiveCount.get(j).clear();
					}
					p2pMatches.clear();
					collectiveMatches.clear();
				}

				/*
				 * Check for calls that were not issued and add them to the
				 * interleaving's HashMap in the error list. However, leaks
				 * should not be added!
				 */
				if (env.getIssueIndex() == -1 && !env.isLeak()) {
					if (this.errorCalls.get(interleaving) == null) {
						this.errorCalls.add(interleaving, new HashMap<String, Envelope>());
					}
					final String key = env.getFunctionName() + env.getFilePath() + env.getLinenumber();
					this.errorCalls.get(interleaving).put(key, env);
				}

				/*
				 * If we have already seen the issue orders, set to -1, meaning
				 * not issued.
				 */
				if (issueOrders.containsKey(env.getIssueIndex())) {
					env.setIssueIndex(-1);
				} else {
					issueOrders.put(env.getIssueIndex(), 0);
				}

				/*
				 * See if there is a P2P match - the HashMap contains the
				 * Envelope's interleaving, rank, and index. If there is a
				 * match, then store it in the envelope. This is so we don't
				 * have to search through everything later when we need the
				 * match.
				 */
				if (env.getMatch_index() >= 0 && env.getMatch_rank() >= 0) {
					final String p2pMatchString = env.getInterleaving() + "_" + env.getMatch_rank() + "_" + env.getMatch_index(); //$NON-NLS-1$ //$NON-NLS-2$
					if (p2pMatches.containsKey(p2pMatchString)) {
						final Envelope match = p2pMatches.get(p2pMatchString);
						env.pairWithEnvelope(match);
						if (env.getFunctionName() != "MPI_Probe" && env.getFunctionName() != "MPI_Iprobe") { //$NON-NLS-1$ //$NON-NLS-2$
							p2pMatches.remove(p2pMatchString);
						}
					} else {
						p2pMatches.put(env.getInterleaving() + "_" + env.getRank() + "_" + env.getIndex(), env); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

				/*
				 * Next, see if there is a collective match. Also, don't want to
				 * match for communicators of size one, since there will be
				 * nothing to add.
				 */
				if (env.isCommunicator_set() && env.getCommunicator_ranks().size() > 1
						&& env.getCommunicator_ranks_string() != null) {
					int count;
					String collective = env.getFunctionName() + " " + env.isCommunicator_set(); //$NON-NLS-1$
					if (collectiveCount.get(env.getRank()).containsKey(collective)) {
						count = collectiveCount.get(env.getRank()).get(collective);
					} else {
						count = 0;
					}
					collectiveCount.get(env.getRank()).put(collective, count + 1);

					// Now see if there are others with the same collective.
					collective = env.getFunctionName() + " " + count + " " + env.getCommunicator_ranks_string(); //$NON-NLS-1$ //$NON-NLS-2$
					if (collectiveMatches.containsKey(collective)) {
						final ArrayList<Envelope> matches = collectiveMatches.get(collective);
						for (final Envelope e : matches) {
							env.addCollectiveMatch(e);
						}
						matches.add(env);

						// See if we have all of the matches, and if so, remove.
						if (matches.size() == env.getCommunicator_ranks()
								.size()) {
							collectiveMatches.remove(collective);
						}
					} else {
						final ArrayList<Envelope> matches = new ArrayList<Envelope>();
						matches.add(env);
						collectiveMatches.put(collective, matches);
					}
				}

				// Add to the list, but if this is a resource leak DO NOT
				if (!env.isLeak()) {
					this.transitionList.get(this.transitionList.size() - 1).add(env);
				}
			}

			// If no transitions found in the interleaving, add at least one.
			if (this.transitionList.size() == 0) {
				this.transitionList.add(new ArrayList<Envelope>());
			}

			// Sort the transitions if the preference is enabled.
			final String str = GemPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.GEM_PREF_STEP_ORDER);
			final int size = this.transitionList.size();
			if (str.equals("issueOrder")) { //$NON-NLS-1$
				// Sort by internal issue order
				for (int k = 0; k < size; k++) {
					Collections.sort(this.transitionList.get(k), new InternalIssueOrderSorter());
				}
			} else {
				// Sort by program order
				for (int l = 0; l < size; l++) {
					Collections.sort(this.transitionList.get(l), new ProgramOrderSorter());
				}
			}
			this.currentInterleaving = 0;
			this.currentTransitionIndex = -1;

		} catch (final CoreException ce) {
			GemUtilities.logExceptionDetail(ce);
		} catch (final ParseException pe) {
			GemUtilities.logExceptionDetail(pe);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}

	/**
	 * Sets the envelope involved with the current transition to the one
	 * specified.
	 * 
	 * @param none
	 * @return Envelope Null if it didn't make sense to place the specified
	 *         Envelope, the Envelope otherwise.
	 */
	// TODO This is not currently being used
	synchronized public Envelope setCurrentTransition(Envelope env) {
		if (env == null) {
			return null;
		}

		if (env.getInterleaving() - 1 < 0 || env.getInterleaving() - 1 >= this.transitionList.size()) {
			return null;
		}

		for (int i = 0; i < this.transitionList.get(env.getInterleaving() - 1).size(); i++) {
			if (this.transitionList.get(env.getInterleaving() - 1).get(i) == env) {
				this.currentInterleaving = env.getInterleaving() - 1;
				this.currentTransitionIndex = i;
				return env;
			}
		}
		return null;
	}

	/**
	 * Moves to the next interleaving.
	 * 
	 * @param none
	 * @return boolean True if the operation was successful, false otherwise.
	 */
	synchronized public boolean setNextInterleaving() {

		if (this.currentInterleaving + 1 >= this.transitionList.size()) {
			return false;
		}

		this.currentInterleaving++;
		this.currentTransitionIndex = -1;
		return true;
	}

	/**
	 * Moves to the previous interleaving.
	 * 
	 * @param none
	 * @return boolean True if the operation was successful, false otherwise.
	 */
	synchronized public boolean setPreviousInterleaving() {
		if (this.currentInterleaving == 0) {
			return false;
		}

		this.currentInterleaving--;
		this.currentTransitionIndex = -1;
		return true;
	}

}
