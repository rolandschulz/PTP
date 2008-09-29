/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.ui.views.spu.event;

import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.AbstractDebugView;


/**
 * Updates the spu event view.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUEventViewEventHandler extends AbstractDebugEventHandler {

	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view spu events view
	 */
	public SPUEventViewEventHandler( AbstractDebugView view ) {
		super( view );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	protected void doHandleDebugEvents( DebugEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			DebugEvent event = events[i];
			switch( event.getKind() ) {
				case DebugEvent.CREATE:
				case DebugEvent.TERMINATE:
					if ( event.getSource() instanceof IDebugTarget || event.getSource() instanceof IThread )
						refresh();
					break;
				case DebugEvent.SUSPEND:
					refresh();
					break;
				case DebugEvent.CHANGE:
					if ( event.getSource() instanceof IThread )
						refresh();
					break;
			}
		}
	}
}
