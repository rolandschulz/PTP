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
package org.eclipse.ptp.debug.ui;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.breakpoints.PBreakpointManager;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.ui.JobManager;
import org.eclipse.ptp.ui.model.IElement;

/**
 * @author clement chu
 *
 */
public class UIDebugManager extends JobManager implements IBreakpointListener {
	public final static int PROC_SUSPEND = 6;
	public final static int PROC_HIT = 7;
	
	private PBreakpointManager bptManager = null;

	public UIDebugManager() {
		bptManager = PBreakpointManager.getDefault();
		bptManager.getBreakpointManager().addBreakpointListener(this);
	}
	
	public void shutdown() {
		bptManager.getBreakpointManager().removeBreakpointListener(this);
		super.shutdown();
	}

	public void unregisterElements(IElement[] elements) {
		for (int i=0; i<elements.length; i++) {
			//only unregister some registered elements
			if (elements[i].isRegistered()) {
				IPJob job = findJobById(getCurrentJobId());
				ICDISession session = PTPDebugCorePlugin.getDefault().getDebugSession(job);
				((IPCDISession)session).unregisterTarget(elements[i].getIDNum());
			}
		}
	}
	
	public void registerElements(IElement[] elements) {
		for (int i=0; i<elements.length; i++) {
			//only register some unregistered elements
			if (!elements[i].isRegistered()) {
				IPJob job = findJobById(getCurrentJobId());
				ICDISession session = PTPDebugCorePlugin.getDefault().getDebugSession(job);
				((IPCDISession)session).registerTarget(elements[i].getIDNum());
			}
		}
	}
	
	/******
	 * Breakpoint
	 ******/
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (PTPDebugUIPlugin.getDefault().getCurrentPerspectiveID().equals(IPTPDebugUIConstants.PERSPECTIVE_DEBUG)) {
			if (breakpoint instanceof ICLineBreakpoint) {
				try {
					bptManager.addRemoveBreakpoint((ICLineBreakpoint)breakpoint, getCurrentSetId(), getCurrentJobId());
					bptManager.getBreakpointManager().removeBreakpoint(breakpoint, true);
				} catch (CoreException e) {
					System.out.println("Err: " + e.getMessage());
				}
			}
		}
	}
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {}
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {}	
	
    		/*
    		 * Cannot unregister the extension
    		final String CDT_DEBUG_UI_ID = "org.eclipse.cdt.debug.ui";    		
		Bundle bundle = Platform.getBundle(CDT_DEBUG_UI_ID);
		if (bundle != null && bundle.getState() == Bundle.ACTIVE) {
			//ExtensionRegistry reg = (ExtensionRegistry) Platform.getExtensionRegistry();
			//reg.remove(bundle.getBundleId());
			try {
				Platform.getBundle(CDT_DEBUG_UI_ID).uninstall();
				System.out.println("Remove: " + bundle.getState());
			} catch (BundleException e) {
				System.out.println("Err: " + e.getMessage());
			}
		} 
		if (bundle != null && bundle.getState() == Bundle.UNINSTALLED) {
			try {
				Platform.getBundle(CDT_DEBUG_UI_ID).start();
				System.out.println("Add: " + bundle.getState());
			} catch (BundleException e) {
				System.out.println("Err: " + e.getMessage());
			}
		}
		*/
}
