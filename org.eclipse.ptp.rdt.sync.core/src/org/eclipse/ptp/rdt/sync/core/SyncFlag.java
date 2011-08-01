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

public enum SyncFlag {
	/**
	 * @since 1.0
	 */
	DISABLE_SYNC, FORCE_SYNC_TO_LOCAL, FORCE_SYNC_TO_REMOTE;

	/**
	 * @since 1.0
	 */
	public static final EnumSet<SyncFlag> NO_SYNC = EnumSet.of(SyncFlag.DISABLE_SYNC);
	public static final EnumSet<SyncFlag> NO_FORCE = EnumSet.noneOf(SyncFlag.class);
	public static final EnumSet<SyncFlag> FORCE = EnumSet.of(SyncFlag.FORCE_SYNC_TO_LOCAL, SyncFlag.FORCE_SYNC_TO_REMOTE);
}
