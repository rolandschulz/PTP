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
	FORCE_SYNC_TO_LOCAL, FORCE_SYNC_TO_REMOTE;

	public static final EnumSet<SyncFlag> NO_FORCE = EnumSet.noneOf(SyncFlag.class);
	public static final EnumSet<SyncFlag> FORCE = EnumSet.allOf(SyncFlag.class);
}
