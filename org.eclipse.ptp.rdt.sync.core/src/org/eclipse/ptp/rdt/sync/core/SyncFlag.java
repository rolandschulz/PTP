package org.eclipse.ptp.rdt.sync.core;

import java.util.EnumSet;

public enum SyncFlag {
	FORCE_SYNC_TO_LOCAL, FORCE_SYNC_TO_REMOTE;

	public static final EnumSet<SyncFlag> NO_FORCE = EnumSet.noneOf(SyncFlag.class);
	public static final EnumSet<SyncFlag> FORCE = EnumSet.allOf(SyncFlag.class);
}
