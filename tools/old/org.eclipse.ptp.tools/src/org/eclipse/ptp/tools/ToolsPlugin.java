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
package org.eclipse.ptp.tools;

import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;
import java.util.*;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IProcess;

/**
 * The main plugin class to be used in the desktop.
 */
public class ToolsPlugin extends AbstractUIPlugin implements IBreakpointListener, IDebugEventSetListener {
	//The shared instance.
	private static ToolsPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public ToolsPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.tools.ToolsPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		//DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		//DebugPlugin.getDefault().addDebugEventListener(this);
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		//DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		//DebugPlugin.getDefault().removeDebugEventListener(this);
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static ToolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ToolsPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	public void breakpointAdded(IBreakpoint breakpoint) {
		System.out.println("breakpoint added");
	}
	
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		System.out.println("breakpoint removed");		
	}

	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		System.out.println("breakpoint changed");		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		// TODO Auto-generated method stub
		for( int i = 0; i < events.length; i++ ) {
			DebugEvent event = events[i];
			System.out.println(event);
			System.out.println("object is " + event.getSource().getClass());
			//if (event.getKind() == DebugEvent.CHANGE && event.getSource().) {
			//}
		}

	}
}
