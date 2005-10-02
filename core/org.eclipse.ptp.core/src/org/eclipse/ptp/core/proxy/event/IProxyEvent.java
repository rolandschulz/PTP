package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.proxy.FastBitSet;

public interface IProxyEvent {
	public static final int EVENT_OK = 0;
	public static final int EVENT_ERROR = 1;
	public static final int EVENT_DBG_BPHIT = 2;
	public static final int EVENT_DBG_SIGNAL = 3;
	public static final int EVENT_DBG_EXIT = 4;
	public static final int EVENT_DBG_STEP = 5;
	public static final int EVENT_DBG_BPSET = 6;
	public static final int EVENT_DBG_FRAMES = 7;
	public static final int EVENT_DBG_DATA = 8;
	public static final int EVENT_DBG_TYPE = 9;
	public static final int EVENT_DBG_VARS = 10;
	public static final int EVENT_DBG_INIT = 11;

	public int getEventID();
	public FastBitSet getBitSet();
}
