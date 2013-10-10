/*******************************************************************************
 * Copyright (c) 2012 Sage Electronic Engineering, LLC. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jason Litton (Sage Electronic Engineering, LLC) - initial API and implementation
 *    Greg Watson (IBM) - adapted for remote core
 *******************************************************************************/

package org.eclipse.ptp.internal.ui;

import java.util.Hashtable;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;

public class PTPUIDebugOptions implements DebugOptionsListener {
	public static final String DEBUG_JOB_TRACING = "/debug/views/jobs/tracing"; //$NON-NLS-1$

	private static DebugTrace fDebugTrace;
	private static DebugOptions fDebugOptions;
	private static PTPUIDebugOptions fUIDebugOptions;

	public static void configure(BundleContext context) {
		if (fUIDebugOptions == null) {
			fUIDebugOptions = new PTPUIDebugOptions(context);
		}
	}

	private PTPUIDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(DebugOptions.LISTENER_SYMBOLICNAME, PTPUIPlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.osgi.service.debug.DebugOptionsListener#optionsChanged(org.eclipse.osgi.service.debug.DebugOptions)
	 */
	@Override
	public void optionsChanged(DebugOptions options) {
		fDebugOptions = options;
		fDebugTrace = options.newDebugTrace(PTPUIPlugin.getUniqueIdentifier());
	}

	public static boolean isDebugging() {
		return PTPUIPlugin.getDefault().isDebugging();
	}

	public static boolean isDebugging(String option) {
		if (fDebugOptions == null) {
			return false;
		}
		return fDebugOptions.getBooleanOption(PTPUIPlugin.getUniqueIdentifier() + option, false);
	}

	public static void setDebugging(String option, boolean value) {
		if (fDebugOptions != null) {
			if (value) {
				fDebugOptions.setDebugEnabled(true);
			}
			fDebugOptions.setOption(option, Boolean.toString(value));
		}
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 * 
	 * @param message
	 *            the message or <code>null</code>
	 */
	public static void trace(String message) {
		trace(null, message);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 * 
	 * @param option
	 *            the option to determine if tracing is displayed
	 * @param message
	 *            the message or <code>null</code>
	 * @param arguments
	 *            optional arguments for the message or <code>null</code>
	 */
	public static void trace(String option, String message, String... arguments) {
		String traceMsg = message;
		if (arguments.length > 0) {
			traceMsg = NLS.bind(message, arguments);
		}
		if ((option != null && isDebugging(option)) || isDebugging()) {
			System.out.println(traceMsg);
			if (fDebugTrace != null) {
				fDebugTrace.trace(option, traceMsg, null);
			}
		}
	}

}
