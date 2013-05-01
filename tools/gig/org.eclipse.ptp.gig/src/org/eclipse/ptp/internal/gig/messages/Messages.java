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
package org.eclipse.ptp.internal.gig.messages;

import org.eclipse.osgi.util.NLS;

/*
 * Used for strings that should be translated into different languages
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.gig.messages.messages"; //$NON-NLS-1$

	public static String GIG_PREFERENCE_PAGE_0;
	public static String GIG_PREFERENCE_PAGE_1;
	public static String USERNAME;
	public static String PASSWORD;
	public static String IO_EXCEPTION;
	public static String INTERRUPTED_EXCEPTION;
	public static String RUN_GKLEE;
	public static String PART_INIT_EXCEPTION;
	public static String PARSE_EXCEPTION;
	public static String MEMORY_COALESCING;
	public static String BANK_CONFLICTS;
	public static String WARP_DIVERGENCE;
	public static String DEADLOCKS;
	public static String NO_DEADLOCK;
	public static String THREADS_THAT_WAIT_AT_EXPLICIT_SYNC_THREADS;
	public static String THREADS_THAT_WAIT_AT_THE_RECONVERGENT_POINT;
	public static String UPDATE_GIG;
	public static String WARP_DIVERGES_INTO_FOLLOWING_SETS;
	public static String WARP_DOES_NOT_DIVERGE;
	public static String SET;
	public static String READ_READ_BANK_CONFLICT;
	public static String WRITE_WRITE_BANK_CONFLICT;
	public static String READ_WRITE_BANK_CONFLICT;
	public static String BANK_CONFLICTS_WITHIN_A_WARP;
	public static String BANK_CONFLICTS_ACROSS_WARPS;
	public static String THREAD_BANK_CONFLICT_THREAD_INFO_TO_STRING;
	public static String MEMORY_COALESCING_BY_WHOLE_WARP;
	public static String MEMORY_COALESCING_BY_WHOLE_WARP_TO_STRING;
	public static String SERVER_HAD_AN_IOEXCEPTION;
	public static String SEND_TO_SERVER;
	public static String CORE_EXCEPTION;
	public static String IMPORT;
	public static String REFRESH;
	public static String DELETE_REMOTE_FILE;
	public static String RESET_SERVER_VIEW;
	public static String SERVER_NAME;
	public static String LOG_EXCEPTION;
	public static String BLOCK;
	public static String THREAD_INFO_FORMAT;
	public static String NONCOALESCED_GLOBAL_MEMORY_ACCESSES;
	public static String NO;
	public static String NONCOALESCED_GLOBAL_MEMORY_ACCESS;
	public static String POTENTIAL_DEADLOCK_SAME_LENGTH;
	public static String POTENTIAL_DEADLOCK_VARIED_LENGTH;
	public static String RACES;
	public static String OTHER;
	public static String WWRWB;
	public static String WWRW;
	public static String WWRAWB;
	public static String WWRAW;
	public static String RWRAW;
	public static String WWBDB;
	public static String WWBD;
	public static String RWBD;
	public static String RW;
	public static String WW;
	public static String ASSERTION_VIOLATION;
	public static String MISSING_VOLATILE;
	public static String WARP_NUMBER;
	public static String THREAD_NUMBER;
	public static String STATISTICS;
	public static String WARP_STATISTIC;
	public static String BLOCK_STATISTIC;
	public static String CANCEL;
	public static String ILLEGAL_COMMAND;
	public static String ILLEGAL_COMMAND_MESSAGE;
	public static String INCORRECT_PASSWORD;
	public static String INCORRECT_PASSWORD_MESSAGE;
	public static String BANK_OR_WARP;
	public static String BANK_CONFLICT_LOW;
	public static String BANK_CONFLICT_HIGH;
	public static String MEMORY_COALESCING_LOW;
	public static String MEMORY_COALESCING_HIGH;
	public static String WARP_DIVERGENCE_LOW;
	public static String WARP_DIVERGENCE_HIGH;
	public static String INCORRECT_FILE_EXTENSION;
	public static String CHANGE_FILE_EXTENSION;
	public static String SELECTION_ERROR;
	public static String SELECT_ONE_FILE;
	public static String TARGET_PROJECT;
	public static String PROJECT_NOT_FOUND;
	public static String PROJECT_NOT_FOUND_MESSAGE;
	public static String BAD_LOG_FILE;
	public static String EXCEPTION_WHILE_PARSING;
	public static String COMPILATION_ERROR;
	public static String SEE_CONSOLE;
	public static String RUNTIME_ERROR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
