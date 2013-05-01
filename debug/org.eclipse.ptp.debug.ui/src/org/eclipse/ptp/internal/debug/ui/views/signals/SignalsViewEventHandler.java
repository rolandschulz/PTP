/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui.views.signals;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.internal.debug.ui.views.AbstractDebugEventHandler;

/**
 * @author Clement chu
 */
public class SignalsViewEventHandler extends AbstractDebugEventHandler {
	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view signals view
	 */
	public SignalsViewEventHandler(AbstractDebugView view) {
		super(view);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	protected void doHandleDebugEvents(DebugEvent[] events) {
		for(int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			switch(event.getKind()) {
				case DebugEvent.CREATE:
				case DebugEvent.TERMINATE:
					if (event.getSource() instanceof IDebugTarget || event.getSource() instanceof IPSignal)
						refresh();
					break;
				case DebugEvent.SUSPEND:
					refresh();
					break;
				case DebugEvent.CHANGE:
					if (event.getSource() instanceof IPSignal)
						refresh(event.getSource());
					break;
			}
		}
	}
}
