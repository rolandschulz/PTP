/*******************************************************************************
 * Copyright (c) 2014 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Flags to control the behavior of synchronization.
 * 
 * @since 4.0
 */
public enum SyncFlag {
	/**
	 * Sync local to remote
	 * @since 4.0
	 */
	SYNC_LR,
	
	/**
	 * Sync remote to local
	 * @since 4.0
	 */
	SYNC_RL;

	/**
	 * Convenience flag set for sync'ing both directions (from local to remote and from remote to local).
	 * @since 4.0
	 */
	public static final Set<SyncFlag> BOTH = Collections.unmodifiableSet(EnumSet.allOf(SyncFlag.class));

	/**
	 * Convenience flag set for sync'ing only from local to remote.
	 * @since 4.0
	 */
	public static final Set<SyncFlag> LR_ONLY = Collections.unmodifiableSet(EnumSet.of(SyncFlag.SYNC_LR));

	/**
	 * Convenience flag set for sync'ing only from remote to local.
	 * @since 4.0
	 */
	public static final Set<SyncFlag> RL_ONLY = Collections.unmodifiableSet(EnumSet.of(SyncFlag.SYNC_RL));
}