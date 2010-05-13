/*******************************************************************************
 * Copyright (c) 2009, 2010 University of Utah School of Computing
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

package org.eclipse.ptp.gem.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.gem.messages.Messages;

/**
 * This class is a representation of all possible parameters for an MPI function
 * intercepted by the ISP Profiler.
 */
public class Envelope {

	// Basic Envelope information
	private int interleaving;
	private int rank;
	private int index;
	private int orderIndex;
	private int issueIndex;
	private int linenumber;

	// For output
	private String func_name;
	private String filename;
	private String toStringOutput;

	// For point-2-point matches.
	private int match_rank;
	private int match_index;
	private Envelope match_envelope;

	// For collectives, sends, and receives.
	private int communicator;
	private String communicator_ranks_string;
	private ArrayList<Integer> communicator_ranks;
	private ArrayList<Envelope> communicator_matches;

	// For sends / receives.
	private int src_rank;
	private int dest_rank;
	private int tag;

	// Intra and inter CB edges
	private ArrayList<CB> intraCb;
	private ArrayList<CB> interCb;

	// For outputting to string, determine what to output.
	private boolean communicator_set;
	private boolean src_rank_set;
	private boolean dest_rank_set;
	private boolean tag_set;

	// For assertions.
	private boolean assertion;
	private String assert_message;
	private String assert_function;

	// For resource leaks.
	private boolean leak;
	private String leak_resource;

	// Regular expressions for parsing log file lines
	private static Pattern envelopeRegex = Pattern
			.compile("^([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([-0-9]+)\\s+([^\\s]+)(?:\\s+([^\\s]+))?(?:\\s+([^\\s]+))?(?:\\s+([^\\s]+))?\\s+\\{\\s+([^\\}]*)\\}\\s+\\{\\s+([^}]*)\\}\\s+Match:\\s+([-]?[0-9]+)\\s+([-]?[0-9]+)\\s+File:\\s+[-]?[0-9]+ (.+?) ([-]?[0-9]+)$"); //$NON-NLS-1$
	private static Pattern assertRegex = Pattern
			.compile("^([0-9]+)\\s+([0-9]+)\\s+ASSERT[ ]+Message:[ ]+[0-9]+ (.+?)Function:[ ]+[0-9]+ (.+?) File:[ ]+[0-9]+ (.+?) ([-]?[0-9]+)$"); //$NON-NLS-1$
	private static Pattern leakRegex = Pattern
			.compile("^([0-9]+)\\s+([0-9]+)\\s+Leak\\s+(.+?)\\s+\\{\\s+\\}\\s+\\{\\s+\\}\\s+Match:\\s+-1\\s+-1\\sFile:[ ]+[0-9]+ (.+?) ([-]?[0-9]+)$"); //$NON-NLS-1$
	private static Pattern communicatorRegex = Pattern
			.compile("^([0-9]+)_(?:([0-9]+):)*$"); //$NON-NLS-1$
	private static Pattern interCbRegex = Pattern
			.compile("^(?:\\[\\s+([0-9]+)\\s+([0-9]+)\\s+\\]\\s+)*$"); //$NON-NLS-1$
	private static Pattern intraCbRegex = Pattern.compile("^([0-9]+\\s+)*$"); //$NON-NLS-1$

	/**
	 * Returns the interleaving for this Envelope.
	 * 
	 * @param none
	 * @return int The interleaving for this Envelope.
	 */
	public int getInterleaving() {
		return interleaving;
	}

	/**
	 * Returns the interleaving for this Envelope.
	 * 
	 * @param interleaving The interleaving for this Envelope.
	 * @return void
	 */
	public void setInterleaving(int interleaving) {
		this.interleaving = interleaving;
	}

	/**
	 * Returns the rank for this Envelope.
	 * 
	 * @param none
	 * @return int The rank for this Envelope.
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Returns the index for this Envelope.
	 * 
	 * @param none
	 * @return int The index for this Envelope.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the program order for this Envelope.
	 * 
	 * @param none
	 * @return int The program order for this Envelope.
	 */
	public int getOrderIndex() {
		return orderIndex;
	}

	/**
	 * Returns the issue order for this Envelope.
	 * 
	 * @param none
	 * @return int The issue order for this Envelope.
	 */
	public int getIssueIndex() {
		return issueIndex;
	}

	/**
	 * Sets the issue index for this Envelope.
	 * 
	 * @param The issue index for this Envelope.
	 * @return void
	 */
	public void setIssueIndex(int value) {
		this.issueIndex = value;
	}

	/**
	 * Returns the function name for this Envelope.
	 * 
	 * @param none
	 * @return String The function name for this Envelope.
	 */
	public String getFunctionName() {
		return func_name;
	}

	/**
	 * Returns the file name for this Envelope.
	 * 
	 * @param none
	 * @return String The file name for this Envelope.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the line number in the source file for this Envelope.
	 * 
	 * @param none
	 * @return int The line number in the source file for this Envelope.
	 */
	public int getLinenumber() {
		return linenumber;
	}

	/**
	 * Returns the resource leak for this Envelope.
	 * 
	 * @param none
	 * @return String The resource leak for this Envelope.
	 */
	public String getLeakResource() {
		return this.leak_resource;
	}

	/**
	 * Returns the match rank for this Envelope.
	 * 
	 * @param none
	 * @return int The match rank for this Envelope.
	 */
	public int getMatch_rank() {
		return match_rank;
	}

	/**
	 * Returns the match rank index for this Envelope.
	 * 
	 * @param none
	 * @return int The match rank index for this Envelope.
	 */
	public int getMatch_index() {
		return match_index;
	}

	/**
	 * Returns the match Envelope for this Envelope.
	 * 
	 * @param none
	 * @return Envelope The match Envelope for this Envelope.
	 */
	public Envelope getMatch_envelope() {
		return match_envelope;
	}

	/**
	 * Returns the list of communicator ranks for this Envelope.
	 * 
	 * @param none
	 * @return ArrayList<Integer> The list of communicator ranks for this
	 *         Envelope.
	 */
	public ArrayList<Integer> getCommunicator_ranks() {
		return communicator_ranks;
	}

	/**
	 * Returns the String that holds the Communicator ranks
	 * 
	 * @return String representing the Communicator ranks
	 */
	public String getCommunicator_ranks_string() {
		return communicator_ranks_string;
	}

	/**
	 * Returns an ArrayList of Envelopes holding each envelope that matches with
	 * this one
	 * 
	 * @return ArrayList of matching Envelopes
	 */
	public ArrayList<Envelope> getCommunicator_matches() {
		return communicator_matches;
	}

	/**
	 * Returns the rank of the source process that sent the call
	 * 
	 * @return int src_rank
	 */
	public int getSrc_rank() {
		return src_rank;
	}

	/**
	 * Returns a boolean representing whether or not this envelope represents an
	 * assertion.
	 * 
	 * @return boolean isAssertion
	 */
	public boolean isAssertion() {
		return this.assertion;
	}

	/**
	 * Returns a boolean representing whether or not this envelope is the source
	 * of a leak
	 * 
	 * @return boolean isLeak
	 */
	public boolean isLeak() {
		return this.leak;
	}

	/**
	 * Returns a boolean representing whether or not the Communicator is set
	 * 
	 * @return boolean communicator_set
	 */
	public boolean isCommunicator_set() {
		return communicator_set;
	}

	/**
	 * Returns a boolean representing whether or not the source rank is set
	 * 
	 * @return boolean src_rank_set
	 */
	public boolean isSrc_rank_set() {
		return src_rank_set;
	}

	/**
	 * Adds the specified Envelope to the list of collective matches.
	 * 
	 * @param envelope The Envelope to add.
	 * @return void
	 */
	public void addCollectiveMatch(Envelope envelope) {
		if (communicator_matches == null) {
			communicator_matches = new ArrayList<Envelope>();
		}
		communicator_matches.add(envelope);

		if (envelope.communicator_matches == null) {
			envelope.communicator_matches = new ArrayList<Envelope>();
		}
		envelope.communicator_matches.add(this);
	}

	/**
	 * Pairs the specified envelope with its match.
	 * 
	 * @param envelope The Envelope to match.
	 * @return void
	 */
	public void pairWithEnvelope(Envelope envelope) {
		this.match_envelope = envelope;
		envelope.match_envelope = this;
	}

	/**
	 * Parses the input line in to an envelope, or returns null if there was a
	 * parse error and an envelope couldn't be matched.
	 * 
	 * @param line The log file line to parse with regular expressions.
	 * @return Envelope The Envelope representing the MPI function call.
	 */
	public static Envelope parse(String line) {
		Matcher envelopeMatch = envelopeRegex.matcher(line);

		// This means we have a leak or an assert
		if (!envelopeMatch.matches()) {
			Matcher assertMatch = assertRegex.matcher(line);
			if (assertMatch.matches()) {
				Envelope env = new Envelope();
				env.assertion = true;
				env.issueIndex = -1;
				env.orderIndex = Integer.MAX_VALUE;
				env.index = Integer.MAX_VALUE;
				env.match_index = -1;
				env.match_rank = -1;
				env.func_name = "MPI_assert"; //$NON-NLS-1$
				try {
					env.interleaving = Integer.parseInt(assertMatch.group(1));
				} catch (NumberFormatException nfe) {
					env.interleaving = -1;
				}
				try {
					env.rank = Integer.parseInt(assertMatch.group(2));
				} catch (NumberFormatException nfe) {
					env.rank = -1;
				}
				env.assert_message = assertMatch.group(3);
				env.assert_function = assertMatch.group(4);
				env.filename = assertMatch.group(5);
				try {
					env.linenumber = Integer.parseInt(assertMatch.group(6));
				} catch (NumberFormatException nfe) {
					env.linenumber = -1;
				}
				return env;
			} else {
				Matcher leakMatch = leakRegex.matcher(line);
				if (leakMatch.matches()) {
					Envelope env = new Envelope();
					env.leak = true;
					env.issueIndex = -1;
					env.orderIndex = Integer.MAX_VALUE;
					env.index = Integer.MAX_VALUE;
					env.match_index = -1;
					env.match_rank = -1;
					env.func_name = "leak"; //$NON-NLS-1$
					try {
						env.interleaving = Integer.parseInt(leakMatch.group(1));
					} catch (NumberFormatException nfe) {
						env.interleaving = -1;
					}
					try {
						env.rank = Integer.parseInt(leakMatch.group(2));
					} catch (NumberFormatException nfe) {
						env.rank = -1;
					}
					env.leak_resource = leakMatch.group(3);
					env.filename = leakMatch.group(4);
					try {
						env.linenumber = Integer.parseInt(leakMatch.group(5));
					} catch (NumberFormatException nfe) {
						env.linenumber = -1;
					}
					return env;
				} else {
					return null;
				}
			}
		}

		// We know we have an Envelope by now
		Envelope env = new Envelope();
		for (int i = 1; i <= envelopeMatch.groupCount(); i++) {
			String value = envelopeMatch.group(i);

			switch (i) {
			case 1: {
				env.interleaving = Integer.parseInt(value);
				break;
			}
			case 2: {
				env.rank = Integer.parseInt(value);
				break;
			}
			case 3: {
				env.index = Integer.parseInt(value);
				break;
			}
			case 4: {
				env.orderIndex = Integer.parseInt(value);
				break;
			}
			case 5: {
				env.issueIndex = Integer.parseInt(value);
				break;
			}
			case 6: {
				env.func_name = "MPI_" + value; //$NON-NLS-1$
				break;
			}
			case 7: {

				// If communicator, only first filled in.
				if (!isNullOrEmpty(value)
						&& isNullOrEmpty(envelopeMatch.group(i + 1))
						&& isNullOrEmpty(envelopeMatch.group(i + 2))) {
					parseCommunicator(env, value);
					env.tag = 0;
					env.tag_set = false;
					env.src_rank = 0;
					env.src_rank_set = false;
					env.dest_rank = 0;
					env.dest_rank_set = false;
				}

				// If all three tokens are present, then it's a send or receive.
				else if (!isNullOrEmpty(value)
						&& !isNullOrEmpty(envelopeMatch.group(i + 1))
						&& !isNullOrEmpty(envelopeMatch.group(i + 2))) {
					if (env.func_name.toLowerCase().contains("recv") //$NON-NLS-1$
							|| env.func_name.toLowerCase().contains("probe")) { //$NON-NLS-1$
						env.src_rank = Integer.parseInt(value);
						env.src_rank_set = true;
						env.dest_rank = 0;
						env.dest_rank_set = false;
					} else {
						env.src_rank = 0;
						env.src_rank_set = false;
						env.dest_rank = Integer.parseInt(value);
						env.dest_rank_set = true;
					}
					env.tag = Integer.parseInt(envelopeMatch.group(i + 1));
					env.tag_set = true;
					parseCommunicator(env, envelopeMatch.group(i + 2));
				}

				// Otherwise, a Wait, Test, or Finalize.
				else {
					env.src_rank = 0;
					env.src_rank_set = false;
					env.dest_rank = 0;
					env.dest_rank_set = false;
					env.tag = 0;
					env.tag_set = false;
					env.communicator = 0;
					env.communicator_set = false;
				}
				break;
			}
			case 10: {

				// IntraCBs
				ArrayList<CB> intraCb = new ArrayList<CB>();
				Matcher intraCbMatch = intraCbRegex.matcher(value);
				if (intraCbMatch.matches()) {

					// This puts a single integer into each match group
					Pattern pattern = Pattern.compile("([0-9]+\\s+)"); //$NON-NLS-1$
					Matcher matcher = pattern.matcher(value);

					String index = ""; //$NON-NLS-1$
					while (matcher.find()) {
						index = matcher.group().trim();
						if (!isNullOrEmpty(index)) {
							intraCb.add(new CB(env.rank, Integer
									.parseInt(index)));
						}
					}
				}
				env.intraCb = intraCb;
				break;
			}
			case 11: {

				// InterCBs
				ArrayList<CB> interCb = new ArrayList<CB>();
				Matcher interCbMatcher = interCbRegex.matcher(value);

				// Make sure we're starting with the right thing
				if (interCbMatcher.matches()) {

					// This will put a single integer into each match group
					Pattern pattern = Pattern
							.compile("[\\s+([0-9])+\\s+[0-9]+)\\s+]\\s+"); //$NON-NLS-1$
					Matcher matcher = pattern.matcher(value);

					String rank = ""; //$NON-NLS-1$
					String index = ""; //$NON-NLS-1$
					while (matcher.find()) {
						rank = matcher.group().trim();
						if (matcher.find()) {
							index = matcher.group().trim();
						}
						if (!isNullOrEmpty(rank) && !isNullOrEmpty(index)) {
							interCb.add(new CB(Integer.parseInt(rank), Integer
									.parseInt(index)));
						}
					}
					env.interCb = interCb;
					break;
				}
			}
			case 12: {
				env.match_rank = Integer.parseInt(value);
				break;
			}
			case 13: {
				env.match_index = Integer.parseInt(value);
				break;
			}
			case 14: {
				env.filename = value;
				break;
			}
			case 15: {
				env.linenumber = Integer.parseInt(value);
				break;
			}
			}
		}
		return env;
	}

	/**
	 * Returns the String representation of this Envelope.
	 * 
	 * @param none
	 * @return String The String representation of this Envelope.
	 */
	public String toString() {
		if (toStringOutput != null) {
			return toStringOutput;
		}

		if (assertion) {
			Pattern assertPattern = Pattern
					.compile("^Assertion `(.+?)' failed.$"); //$NON-NLS-1$
			Matcher m = assertPattern.matcher(assert_message);
			if (m.matches()) {
				toStringOutput = String.format("{0}({1})", func_name, m //$NON-NLS-1$
						.group(1));
			} else {
				toStringOutput = String.format("{0}({1})", func_name, //$NON-NLS-1$
						assert_message);
			}
			return toStringOutput;
		}

		boolean first = true;
		StringBuilder sb = new StringBuilder();
		sb.append("{0}(" + this.func_name + " "); //$NON-NLS-1$ //$NON-NLS-2$

		if (src_rank_set) {
			if (!first) {
				sb.append(", "); //$NON-NLS-1$
			} else {
				first = false;
			}
			if (src_rank == -1) {
				sb.append("source = MPI_ANY_SOURCE"); //$NON-NLS-1$
			} else {
				sb.append(String.format("source = {0}", src_rank)); //$NON-NLS-1$
			}
		}

		if (dest_rank_set) {
			if (!first) {
				sb.append(", "); //$NON-NLS-1$
			} else {
				first = false;
			}
			sb.append(String.format("dest = {0}", dest_rank)); //$NON-NLS-1$
		}

		if (tag_set) {
			if (!first) {
				sb.append(", "); //$NON-NLS-1$
			} else {
				first = false;
			}
			if (tag == -1) {
				sb.append("tag = MPI_ANY_TAG"); //$NON-NLS-1$
			} else {
				sb.append(String.format("tag = {0}", tag)); //$NON-NLS-1$
			}
		}

		if (communicator_set) {
			if (!first) {
				sb.append(", "); //$NON-NLS-1$
			} else {
				first = false;
			}
			switch (communicator) {
			case 0: {
				sb.append("comm = MPI_COMM_WORLD"); //$NON-NLS-1$
				break;
			}
			case 1: {
				sb.append("comm = MPI_COMM_SELF"); //$NON-NLS-1$
				break;
			}
			case 2: {
				sb.append("comm = MPI_COMM_NULL"); //$NON-NLS-1$
				break;
			}
			default: {
				sb.append(String.format("comm = {0}", communicator)); //$NON-NLS-1$
				break;
			}
			}
		}
		sb.append(")"); //$NON-NLS-1$
		toStringOutput = sb.toString();
		return toStringOutput;
	}

	/**
	 * Return true if the call is a collective, false otherwise.
	 * 
	 * @param none
	 * @return boolean True if the call is a collective, false otherwise.
	 */
	public boolean isCollective() {
		String MPICall = this.getFunctionName();

		if (MPICall.equals("MPI_Barrier") || MPICall.equals("MPI_Gather") //$NON-NLS-1$ //$NON-NLS-2$
				|| MPICall.equalsIgnoreCase("MPI_Comm_create") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_gather") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_gatherv") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Allgather") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Allgatherv") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Reduce") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Allreduce") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Alltoall") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Alltoallv") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Scatter") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Scatterv") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Finalize") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Reduce_scatter") //$NON-NLS-1$
				|| MPICall.equalsIgnoreCase("MPI_Bcast") || MPICall.equals("MPI_Scan")) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the specified String is null or empty, false otherwise.
	 * 
	 * @param value The string that is being checked
	 * @return boolean isNullOrEmpty
	 */
	private static boolean isNullOrEmpty(String value) {
		if (value == null || value == "") { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * Parses out the ranks that are members of the communicator matched with
	 * the communicator regular expression.
	 * 
	 * @param env The Envelope being updated
	 * @param value The String holding the ranks that need to be parsed
	 */
	private static void parseCommunicator(Envelope env, String value) {
		if (value == null) {
			GemUtilities.showErrorDialog(Messages.Envelope_53,
					Messages.Envelope_54);
			return;
		}

		Matcher communicatorMatch = communicatorRegex.matcher(value);
		if (!communicatorMatch.matches() || communicatorMatch.groupCount() == 1) {
			env.communicator_set = false;
			env.communicator = -1;
		} else {
			env.communicator_set = true;
			env.communicator = Integer.parseInt(communicatorMatch.group(1));
			env.communicator_ranks_string = value.trim();

			Pattern pattern = Pattern.compile("([0-9]+):"); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(value);

			env.communicator_ranks = new ArrayList<Integer>();
			while (matcher.find()) {
				String commMemberRank = matcher.group().trim().replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
				if (!isNullOrEmpty(commMemberRank)) {
					env.communicator_ranks
							.add(Integer.parseInt(commMemberRank));
				}
			}
		}
	}

	/*
	 * Should only be called by the parse function.
	 */
	private Envelope() {
	}

}
