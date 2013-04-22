/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.util.EnumSet;

/**
 * Flags to control the behavior of synchronization. Currently, the individual FORCE_SYNC_TO_LOCAL and FORCE_SYNC_TO_REMOTE flags
 * are not used. Instead, the EnumSets are used as defined below.
 * 
 * Please note that the synchronization protocol is a work in progress, and the meanings of these flags are subject to change.
 * 
 * @since 3.0
 */
public enum SyncFlag {
	DISABLE_SYNC, FORCE_SYNC_TO_LOCAL, FORCE_SYNC_TO_REMOTE;

	// Do not actually transfer files. Just do any necessary bookkeeping. (This is used, for example, when files change on the
	// system, but the user has disabled sync'ing.)
	public static final EnumSet<SyncFlag> NO_SYNC = EnumSet.of(SyncFlag.DISABLE_SYNC);

	// Transfer files "if needed". For example, if a resource change has affected the local repository. (The meaning of "if needed"
	// is subject to change.
	public static final EnumSet<SyncFlag> NO_FORCE = EnumSet.noneOf(SyncFlag.class);

	// Force transferring of files. This could be necessary, for example, to download remote changes.
	public static final EnumSet<SyncFlag> FORCE = EnumSet.of(SyncFlag.FORCE_SYNC_TO_LOCAL, SyncFlag.FORCE_SYNC_TO_REMOTE);
}
