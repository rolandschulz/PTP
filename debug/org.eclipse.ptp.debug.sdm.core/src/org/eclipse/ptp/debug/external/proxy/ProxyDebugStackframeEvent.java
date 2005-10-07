package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugStackframeEvent extends AbstractProxyEvent implements IProxyEvent {
	private ProxyDebugStackframe[] frames;
	
	public ProxyDebugStackframeEvent(BitList set, ProxyDebugStackframe[] frames) {
		super(EVENT_DBG_FRAMES, set);
		this.frames = frames;
	}
	
	public ProxyDebugStackframe[] getFrames() {
		return this.frames;
	}
	
	public String toString() {
		String res = "EVENT_DBG_FRAMES " + this.getBitSet().toString();
		for (int i = 0; i < frames.length; i++) {
			res += "\n " + frames[i].getLevel() + " file=\"" + frames[i].getFile() + "\"";
			if (frames[i].getFunc().compareTo("") != 0)
				res += " func=" + frames[i].getFunc();
			if (frames[i].getLine() != 0)
				res += " line=" + frames[i].getLine();
			if (frames[i].getAddr().compareTo("") != 0)
				res += " addr=" + frames[i].getAddr();	
		}
		return res;
	}
}
