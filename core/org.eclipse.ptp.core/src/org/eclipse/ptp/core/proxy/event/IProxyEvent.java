package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.proxy.FastBitSet;

public interface IProxyEvent {
	public static final int EVENT_OK = 0;
	public static final int EVENT_ERROR = 1;
	
	public static final int DBG_EVENT_OFFSET = 100;
	public static final int EVENT_DBG_BPHIT = DBG_EVENT_OFFSET + 0;
	public static final int EVENT_DBG_SIGNAL = DBG_EVENT_OFFSET + 1;
	public static final int EVENT_DBG_EXIT = DBG_EVENT_OFFSET + 2;
	public static final int EVENT_DBG_STEP = DBG_EVENT_OFFSET + 3;
	public static final int EVENT_DBG_BPSET = DBG_EVENT_OFFSET + 4;
	public static final int EVENT_DBG_FRAMES = DBG_EVENT_OFFSET + 5;
	public static final int EVENT_DBG_DATA = DBG_EVENT_OFFSET + 6;
	public static final int EVENT_DBG_TYPE = DBG_EVENT_OFFSET + 7;
	public static final int EVENT_DBG_VARS = DBG_EVENT_OFFSET + 8;
	public static final int EVENT_DBG_INIT = DBG_EVENT_OFFSET + 9;

	public int getEventID();
	public FastBitSet getBitSet();
}
