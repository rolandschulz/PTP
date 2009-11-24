/*******************************************************************************
 * Copyright (c) 2009 University of Utah School of Computing
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

package org.eclipse.ptp.isp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.eclipse.ptp.isp.ISPPlugin;
import org.eclipse.ptp.isp.messages.Messages;
import org.eclipse.ptp.isp.preferences.PreferenceConstants;

public class Transitions {

	private ArrayList<ArrayList<Envelope>> transitionList;
	private ArrayList<HashMap<String, Envelope>> errorCallsList;
	private ArrayList<Envelope> resourceLeaksList;
	private ArrayList<Integer> deadlockInterleavings;
	private int numRanks;
	public int currentInterleaving;
	public int currentTransition;
	private int deadlockIndex;
	private boolean deadlock;
	private boolean assertionViolation;
	private boolean resourceLeak;

	/**
	 * CTOR
	 * 
	 * @param logfile
	 *            The fully qualified path to the log file to parse.
	 * @throws ParseException
	 */
	public Transitions(String logfile) throws ParseException {
		this.transitionList = new ArrayList<ArrayList<Envelope>>();

		// Add null as padding so we can be one-based later
		this.errorCallsList = new ArrayList<HashMap<String, Envelope>>();
		this.errorCallsList.add(null);

		this.numRanks = -1;
		this.deadlock = false;
		this.assertionViolation = false;
		this.resourceLeak = false;
		parseLogFile(logfile);
	}

	/**
	 * Returns an Arraylist of Arraylists of Envelopes. Each Arraylist
	 * corresponds to a particular interleaving, and holds envelopes
	 * representing individual MPI call trasnsitions.
	 * 
	 * @param none
	 * @return ArrayList<ArrayList<Envelope>>, representing each MPI Call in
	 *         each possible interleaving
	 */
	public ArrayList<ArrayList<Envelope>> getTransitionList() {
		return this.transitionList;
	}

	/**
	 * Returns and ArrayList of Envelopes that holds each envelope that contains
	 * a resource leak.
	 * 
	 * @param none
	 * @return ArrayList<Envelope> The list of resource leaks.
	 */
	public ArrayList<Envelope> getResourceLeakList() {
		return this.resourceLeaksList;
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
	 * Returns the number of interleavings (AKA Schedules) found for this source
	 * file.
	 * 
	 * @param none
	 * @return int The total number of interleavings.
	 */
	public int getTotalInterleavings() {
		return this.transitionList.size();
	}

	/**
	 * Returns the interleaving current being displayed.
	 * 
	 * @param none
	 * @return int The current interleaving.
	 */
	public int getCurrentInterleaving() {
		return currentInterleaving + 1;
	}

	/**
	 * Returns the list of hashmaps containing Envelopes involved in the error.
	 * There may be multiple maps due to multiple errors in separate
	 * interleavings.
	 * 
	 * @param none
	 * @return ArrayList<HashMap<String, Envelope>> The list of hashmaps
	 *         containing Envelopes involved in program errors.
	 */
	public ArrayList<HashMap<String, Envelope>> getErrorCallsList() {
		return this.errorCallsList;
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
	 * Returns the index of the current transition.
	 * 
	 * @param none
	 * @return int The current transition index.
	 */
	public int getCurrentTransitionIndex() {
		return this.currentTransition;
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
	 * returns a boolean representing whether or not there is an assertion.
	 * violation.
	 * 
	 * @return boolean True if there was an assertion violation, false
	 *         otherwise.
	 */
	public boolean hasAssertion() {
		return this.assertionViolation;
	}

	/**
	 * Returns a boolean representing whether or not there is a resource leak.
	 * 
	 * @param none
	 * @return boolean True if there was a resource leak, false otherwise.
	 */
	public boolean hasResourceLeak() {
		return this.resourceLeak;
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
	synchronized public Envelope stepToFirstTransition(int rank) {

		boolean valid = true;
		if (rank < 0 || rank > this.numRanks) {
			valid = false;
		}
		int size = this.transitionList.get(this.currentInterleaving).size();
		for (int i = 0; i < size; i++) {
			Envelope env = this.transitionList.get(this.currentInterleaving)
					.get(i);
			if (!valid || env.getRank() == rank) {
				this.currentTransition = i;
				return env;
			}
		}
		return null;

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
	synchronized public Envelope stepToLastTransition(int rank) {
		boolean valid = true;
		if (rank < 0 || rank > this.numRanks) {
			valid = false;
		}
		int size = this.transitionList.get(this.currentInterleaving).size() - 1;
		for (int i = size; i >= 0; i--) {
			Envelope env = this.transitionList.get(this.currentInterleaving)
					.get(i);
			if (!valid || env.getRank() == rank) {
				this.currentTransition = i;
				return env;
			}
		}
		return null;
	}

	/**
	 * Returns true if there is a previous transition, false otherwise.
	 * 
	 * @param none
	 * @return boolean True if the current interleaving has a previous
	 *         transition, false otherwise.
	 */
	public boolean hasPreviousTransition() {
		return this.currentTransition > 0;
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
		int tempIndex = currentTransition - 1;
		while (tempIndex >= 0) {
			int line1 = transitionList.get(currentInterleaving).get(
					currentTransition).getLinenumber();
			int line2 = transitionList.get(currentInterleaving).get(tempIndex)
					.getLinenumber();
			boolean collective = transitionList.get(currentInterleaving).get(
					currentTransition).isCollective();
			if (line1 == line2 && rank == -1 && collective) {
				tempIndex--;
				continue;
			} else if (rank == -1
					|| transitionList.get(currentInterleaving).get(tempIndex)
							.getRank() == rank) {
				return true;
			}
			tempIndex--;
		}
		return false;
	}

	/**
	 * Returns true if there is a next transition, false otherwise.
	 * 
	 * @param none
	 * @return boolean True if the current interleaving has a next transition,
	 *         false otherwise.
	 */
	synchronized public boolean hasNextTransition() {
		return this.currentTransition + 1 < this.transitionList.get(
				this.currentInterleaving).size();
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
		if (currentTransition == -1) {
			try {
				transitionList.get(currentInterleaving).get(
						currentTransition + 1);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		int tempIndex = currentTransition;
		while (tempIndex < transitionList.get(currentInterleaving).size()) {
			int line1 = transitionList.get(currentInterleaving).get(
					currentTransition).getLinenumber();
			int line2 = transitionList.get(currentInterleaving).get(tempIndex)
					.getLinenumber();
			if (line1 == line2) {
				tempIndex++;
				continue;
			} else if (rank == -1
					|| transitionList.get(currentInterleaving).get(tempIndex)
							.getRank() == rank) {
				return true;
			}
			tempIndex++;
		}
		return false;
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
	synchronized public Envelope previousTransition(int rank) {
		if (this.currentTransition <= 0) {
			return null;
		}

		// If the rank is invalid, treat it as if any rank can be changed.
		if (rank < 0 || rank >= this.numRanks) {
			this.currentTransition--;
			return this.transitionList.get(this.currentInterleaving).get(
					this.currentTransition);
		} else {
			for (int i = this.currentTransition - 1; i >= 0; i--) {
				Envelope env = this.transitionList
						.get(this.currentInterleaving).get(i);
				if (env.getRank() == rank) {
					this.currentTransition = i;
					return env;
				}
			}
			return null;
		}
	}

	/**
	 * Finds the next transition for the specified rank in the current
	 * interleaving.
	 * 
	 * @param rank
	 *            The rank of the next Envelope to find..
	 * @return Envelope The next Envelope in the current interleaving for the
	 *         specified rank.
	 */
	synchronized public Envelope nextTransition(int rank) {
		if (this.currentTransition + 1 >= this.transitionList.get(
				this.currentInterleaving).size()) {
			return null;
		}

		// If the rank is invalid, treat it as any rank can advance.
		if (rank < 0 || rank >= this.numRanks) {
			this.currentTransition++;
			return this.transitionList.get(this.currentInterleaving).get(
					this.currentTransition);
		} else {
			int end = transitionList.get(currentInterleaving).size();
			for (int i = this.currentTransition + 1; i < end; i++) {
				Envelope e = this.transitionList.get(this.currentInterleaving)
						.get(i);
				if (e.getRank() == rank) {
					this.currentTransition = i;
					return e;
				}
			}
			return null;
		}
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
				|| this.currentTransition < 0
				|| this.currentTransition >= this.transitionList.get(
						this.currentInterleaving).size()) {
			return null;
		} else {
			return this.transitionList.get(this.currentInterleaving).get(
					this.currentTransition);
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
	synchronized public Envelope setCurrentTransition(Envelope env) {
		if (env == null) {
			return null;
		}

		if (env.getInterleaving() - 1 < 0
				|| env.getInterleaving() - 1 >= this.transitionList.size()) {
			return null;
		}

		for (int i = 0; i < this.transitionList.get(env.getInterleaving() - 1)
				.size(); i++) {
			if (this.transitionList.get(env.getInterleaving() - 1).get(i) == env) {
				this.currentInterleaving = env.getInterleaving() - 1;
				this.currentTransition = i;
				return env;
			}
		}
		return null;
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
	 * Moves to the previous interleaving.
	 * 
	 * @param none
	 * @return a boolean True if the operation was successful, false otherwise.
	 */
	synchronized public boolean previousInterleaving() {
		if (this.currentInterleaving == 0) {
			return false;
		} else {
			this.currentInterleaving--;
			this.currentTransition = -1;
			return true;
		}
	}

	/**
	 * Moves to the next interleaving.
	 * 
	 * @param none
	 * @return boolean True if the operation was successful, false otherwise.
	 */
	synchronized public boolean nextInterleaving() {

		if (this.currentInterleaving + 1 >= this.transitionList.size()) {
			return false;
		} else {
			this.currentInterleaving++;
			this.currentTransition = -1;
			return true;
		}
	}

	/**
	 * Moves to the deadlock interleaving, or if there are multiple
	 * interleavings, it cylces through all.
	 * 
	 * @param none
	 * @return boolean True if the operation was successful, false otherwise.
	 */
	synchronized public boolean deadlockInterleaving() {

		if (!this.deadlock) {
			return false;
		} else {
			this.currentInterleaving = this.deadlockInterleavings
					.get(this.deadlockIndex) - 1;
			this.currentTransition = -1;

			// This is to cycle through the deadlock interleavings
			this.deadlockIndex++;
			this.deadlockIndex = this.deadlockIndex
					% this.deadlockInterleavings.size();
			return true;
		}
	}

	/**
	 * Returns the envelope that matches with the envelope passed in.
	 * 
	 * @param Envelope
	 *            The Envelope whose match we are requesting.
	 * @return Envelope The Envelope that matches, or null if there is no match.
	 */
	public Envelope getMatchingTransition(Envelope env) {
		if (env.getIssueIndex() < 0) {
			return null;
		} else {
			return env.getMatch_envelope();
		}
	}

	/**
	 * Returns a list of all envelopes involved in the collective call held in
	 * the Envelope that is passed in.
	 * 
	 * @param Envelope
	 *            The Envelope holding the collective call.
	 * @return ArrayList<Envelope> A list of all Envelopes involved with that
	 *         call.
	 */
	public ArrayList<Envelope> getCollectiveTransitions(Envelope env) {
		if (env.getIssueIndex() < 0) {
			return null;
		} else {
			return env.getCommunicator_matches();
		}
	}

	/**
	 * Returns an ArrayList of all envelopes in the interleaving that is passed
	 * in.
	 * 
	 * @param interleaving
	 *            (an integer representing which interleaving)
	 * @return ArrayList<Envelope> The ArrayList of all envelopes in that
	 *         interleaving.
	 */
	public ArrayList<Envelope> getEnvelopesInInterleaving(int interleaving) {
		if (interleaving < 1 || interleaving > getTotalInterleavings()) {
			return null;
		} else {
			return this.transitionList.get(interleaving - 1);
		}
	}

	/**
	 * Observes the current envelope and discovers which ranks are involved with
	 * it at this moment.
	 * 
	 * @param total
	 *            The total ranks involved.
	 * @return String The string representing the ranks of those involved with
	 *         the current call.
	 */
	public String getRanksInvolved(int[] total) {
		String result = transitionList.get(currentInterleaving).get(
				currentTransition).getRank()
				+ ""; //$NON-NLS-1$

		int currLine = transitionList.get(currentInterleaving).get(
				currentTransition).getLinenumber();

		// We can safely assume that we are currently on the 1st iteration of
		// the call
		int i = currentTransition;
		int end = transitionList.get(currentInterleaving).size();
		int count = 1;

		// start at next env
		for (i++; i < end; i++) {
			if (currLine == transitionList.get(currentInterleaving).get(i)
					.getLinenumber()) {
				count++;
				result += "," //$NON-NLS-1$
						+ transitionList.get(currentInterleaving).get(i)
								.getRank();
			} else {
				break;
			}
		}

		total[0] = count;
		return result;
	}

	/*
	 * This is where the bulk of the p2p and collective matching gets done.
	 */
	private void parseLogFile(String logFilePath) throws ParseException {
		HashMap<String, Envelope> p2pMatches = new HashMap<String, Envelope>();
		HashMap<String, ArrayList<Envelope>> collectiveMatches = new HashMap<String, ArrayList<Envelope>>();
		ArrayList<HashMap<String, Integer>> collectiveCount = new ArrayList<HashMap<String, Integer>>();
		HashMap<Integer, Integer> issueOrders = new HashMap<Integer, Integer>();

		File logfile = new File(logFilePath);
		Scanner scanner;
		String line;
		int interleaving = -1;
		boolean fileReadSuccess = false;
		try {
			scanner = new Scanner(logfile);
			line = scanner.nextLine();

			try {
				this.numRanks = Integer.parseInt(line);
				fileReadSuccess = true;
			} catch (NumberFormatException nfe) {
				IspUtilities.showExceptionDialog(Messages.Transitions_2, nfe);
				IspUtilities.logError(Messages.Transitions_3, nfe);
			}
			if (fileReadSuccess) {
				for (int i = 0; i < this.numRanks; i++) {
					collectiveCount.add(new HashMap<String, Integer>());
				}
			}

			while (scanner.hasNext()) {
				line = scanner.nextLine();

				// identify the deadlock interleavings
				if (line.endsWith("DEADLOCK")) { //$NON-NLS-1$
					if (this.deadlockInterleavings == null) {
						this.deadlock = true;
						this.deadlockInterleavings = new ArrayList<Integer>();
						this.deadlockIndex = 0;
					}
					StringTokenizer st = new StringTokenizer(line);
					this.deadlockInterleavings.add(Integer.parseInt(st
							.nextToken()));
					continue;
				}

				Envelope env = Envelope.parse(line);
				if (env == null) {
					throw new ParseException(line, interleaving);
				} else if (env.isAssertion()) {
					this.assertionViolation = true;
					env.setInterleaving((interleaving < 0) ? 1 : interleaving);
				} else if (env.getFunctionName() == "MPI_assert") { //$NON-NLS-1$
					if (scanner.hasNext()) {
						scanner.nextLine();
					}
				} else if (env.isLeak()) {
					if (this.resourceLeaksList == null) {
						this.resourceLeak = true;
						this.resourceLeaksList = new ArrayList<Envelope>();
					}
					this.resourceLeaksList.add(env);
				}

				/*
				 * Advance to the next interleaving if needed. Also need to
				 * clear all the data structures.
				 */
				if (env.getInterleaving() != interleaving) {
					// add a transition list for the interleaving
					this.transitionList.add(new ArrayList<Envelope>());
					interleaving = env.getInterleaving();
					this.errorCallsList.add(interleaving, null);
					for (int j = 0; j < this.numRanks; j++) {
						collectiveCount.get(j).clear();
					}
					p2pMatches.clear();
					collectiveMatches.clear();
				}

				// Check for calls that were not issued and add them to the
				// interleaving's hashmap in the error list.
				// However leaks should not be added!
				if (env.getIssueIndex() == -1 && !env.isLeak()) {
					if (this.errorCallsList.get(interleaving) == null) {
						this.errorCallsList.add(interleaving,
								new HashMap<String, Envelope>());
					}
					String key = env.getFunctionName() + env.getFilename()
							+ env.getLinenumber();
					this.errorCallsList.get(interleaving).put(key, env);
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
				 * See if there is a P2P match - the hashmap contains the
				 * Envelope's interleaving, rank, and index. If there is a
				 * match, then store it in the envelope. This is so we don't
				 * have to search through everything later when we need the
				 * match.
				 */
				if (env.getMatch_index() >= 0 && env.getMatch_rank() >= 0) {
					String p2pMatchString = env.getInterleaving() + "_" //$NON-NLS-1$
							+ env.getMatch_rank() + "_" + env.getMatch_index(); //$NON-NLS-1$
					if (p2pMatches.containsKey(p2pMatchString)) {
						Envelope match = p2pMatches.get(p2pMatchString);
						env.pairWithEnvelope(match);
						if (env.getFunctionName() != "MPI_Probe" //$NON-NLS-1$
								&& env.getFunctionName() != "MPI_Iprobe") { //$NON-NLS-1$
							p2pMatches.remove(p2pMatchString);
						}
					} else {
						p2pMatches.put(env.getInterleaving() + "_" //$NON-NLS-1$
								+ env.getRank() + "_" + env.getIndex(), env); //$NON-NLS-1$
					}
				}

				/*
				 * Next, see if there is a collective match. Also, don't want to
				 * match for communicators of size one, since there will be
				 * nothing to add.
				 */
				if (env.isCommunicator_set()
						&& env.getCommunicator_ranks().size() > 1
						&& env.getCommunicator_ranks_string() != null) {
					int count;
					String collective = env.getFunctionName() + " " //$NON-NLS-1$
							+ env.isCommunicator_set();
					if (collectiveCount.get(env.getRank()).containsKey(
							collective)) {
						count = collectiveCount.get(env.getRank()).get(
								collective);
					} else {
						count = 0;
					}
					collectiveCount.get(env.getRank()).put(collective,
							count + 1);

					// Now see if there are others with the same collective.
					collective = env.getFunctionName() + " " + count + " " //$NON-NLS-1$ //$NON-NLS-2$
							+ env.getCommunicator_ranks_string();
					if (collectiveMatches.containsKey(collective)) {
						ArrayList<Envelope> matches = collectiveMatches
								.get(collective);
						for (Envelope e : matches) {
							env.addCollectiveMatch(e);
						}
						matches.add(env);

						// See if we have all of the matches, and if so, remove.
						if (matches.size() == env.getCommunicator_ranks()
								.size()) {
							collectiveMatches.remove(collective);
						}
					} else {
						ArrayList<Envelope> matches = new ArrayList<Envelope>();
						matches.add(env);
						collectiveMatches.put(collective, matches);
					}
				}

				// Add to the list, but if this is a resource leak DO NOT
				if (!env.isLeak())
					this.transitionList.get(this.transitionList.size() - 1)
							.add(env);
			}

			/*
			 * If there were no transitions in the interleaving, add at least
			 * one.
			 */
			if (this.transitionList.size() == 0) {
				this.transitionList.add(new ArrayList<Envelope>());
			}

			// Sort the transitions if the preference is enabled.
			String str = ISPPlugin.getDefault().getPreferenceStore().getString(
					PreferenceConstants.ISP_PREF_STEP_ORDER);
			int size = this.transitionList.size();
			if (str.equals("issueOrder")) { //$NON-NLS-1$
				for (int k = 0; k < size; k++) {

					// Use the IssueIndex Comparator
					Collections.sort(this.transitionList.get(k),
							new InternalIssueOrderSorter());
				}
			} else {
				for (int l = 0; l < size; l++) {

					// Use the Program Order Comparator
					Collections.sort(this.transitionList.get(l),
							new ProgramOrderSorter());
				}
			}
			this.currentInterleaving = 0;
			this.currentTransition = -1;

		} catch (FileNotFoundException fnfe) {
			IspUtilities.showExceptionDialog(Messages.Transitions_16, fnfe);
			IspUtilities.logError(Messages.Transitions_17, fnfe);
		}
	}

	public boolean hasError() {
		if (this.assertionViolation || this.deadlock) {
			return true;
		}
		Iterator<HashMap<String, Envelope>> itr = this.errorCallsList
				.iterator();
		while (itr.hasNext()) {
			HashMap<String, Envelope> currentHash = itr.next();
			if (currentHash != null && !currentHash.isEmpty()) {
				return true;
			}
		}
		return false;
	}

}