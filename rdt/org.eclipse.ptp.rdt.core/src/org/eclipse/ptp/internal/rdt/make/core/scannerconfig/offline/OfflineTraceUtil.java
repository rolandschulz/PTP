/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.make.core.scannerconfig.offline;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ptp.internal.rdt.core.miners.StandaloneLogService;



/**
 * Tracebility related utility functions
 *
 * @author vhirsl
 * @since 4.0
 */
/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.make.core/src
 * Class: org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil
 * Version:
 */	
public class OfflineTraceUtil {
	public static final String EOL = System.getProperty("line.separator"); //$NON-NLS-1$
	public static boolean SCANNER_CONFIG = false;
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		
		super.finalize();
	}
	
	public static boolean isTracing() {
		return SCANNER_CONFIG;
	}
	
	public static void outputTrace(String prefix, String[] tokens, String postfix) {
		if (isTracing()) {
			System.out.print(prefix + ' ');
			for (int i = 0; i < tokens.length; i++) {
				System.out.print(tokens[i] + ' ');
				
			}
			System.out.println(postfix);
		}
	}

	public static void outputTrace(String prefix, String msg, String postfix) {
		if (isTracing()) {
			System.out.println(prefix + ' ' + msg + ' ' + postfix);
		}
	}
	
	/**
	 * For traces of type:
	 *     Title:
	 *         Subtitle1:
	 *             item1[0]
	 *             item1[1]
	 *             ...
	 *         Subtitle2:
	 *             item2[0]
	 *             item2[1]
	 *             ...
	 */
	public static void outputTrace(String title, String subtitle1, List<String> item1, List<String> item1new, String subtitle2, List<String> item2) {
		if (isTracing()) {
			//System.out.println();
			System.out.println(title);
			final String prefix = "  ";	//$NON-NLS-1$
			final String doublePrefix = "    ";	//$NON-NLS-1$
			System.out.println(prefix + subtitle1 + " (" + item1.size() + "):");	//$NON-NLS-1$ //$NON-NLS-2$
			int count = 0;
			for (Iterator<String> i = item1.iterator(), j = item1new.iterator(); i.hasNext(); ) {
				System.out.println(doublePrefix + String.valueOf(++count) + "\t\'" +i.next() + (j.hasNext()?"\' -> \'" + j.next():"") + '\'');	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			System.out.println(prefix + subtitle2 + " (" + item2.size() + "):");	//$NON-NLS-1$ //$NON-NLS-2$
			count = 0;
			for (Iterator<String> i = item2.iterator(); i.hasNext(); ) {
				System.out.println(doublePrefix + String.valueOf(++count) + "\t\'" + i.next() + '\'');	//$NON-NLS-1$
			}
		}
	}

	public static void outputError(String string, String line) {
		if (isTracing()) {
			System.out.println();
			System.out.println("Error: " + string + line); //$NON-NLS-1$
		}
	}

	public static void outputError(String string, String[] tokens) {
		if (isTracing()) {
			System.out.println();
			System.out.print("Error: " + string); //$NON-NLS-1$
			for (int i = 0; i < tokens.length; i++) {
				System.out.print(tokens[i] + ' ');
			}
			System.out.println();
		}
	}

	public static void metricsTrace(String title, String subtitlePrefix, String subtitlePostfix,
		Map<String, List<Map<String, List<String>>>> directoryCommandListMap) {
	
		StandaloneLogService.getInstance().traceLog(" *** NEW METRICS TRACE ***"); //$NON-NLS-1$
	
		Set<String> dirs = directoryCommandListMap.keySet();
		for (String dir : dirs) {
			StandaloneLogService.getInstance().traceLog(title + dir + ":"); //$NON-NLS-1$
			List<Map<String, List<String>>> directoryCommandList = directoryCommandListMap.get(dir);
			if (directoryCommandList == null) {
				StandaloneLogService.getInstance().traceLog("  --- empty ---" + EOL); //$NON-NLS-1$
				return;
			}
			for (Map<String, List<String>> command21FileListMap : directoryCommandList) {
				String[] commands = command21FileListMap.keySet().toArray(new String[1]);
				StandaloneLogService.getInstance().traceLog("  " + subtitlePrefix + commands[0] + subtitlePostfix); //$NON-NLS-1$
				List<String> fileList = command21FileListMap.get(commands[0]);
				for (String fileName : fileList) {
					StandaloneLogService.getInstance().traceLog("    " + fileName); //$NON-NLS-1$
				}
			}
		}
			
		
		
	}

	public static void summaryTrace(String title, int workingDirsN, int commandsN, int filesN) {
		
		
		StandaloneLogService.getInstance().traceLog(" *** METRICS SUMMARY ***"); //$NON-NLS-1$
		
		StandaloneLogService.getInstance().traceLog(title);
		StandaloneLogService.getInstance().traceLog("  Number of directories visited: " + Integer.toString(workingDirsN)); //$NON-NLS-1$
		StandaloneLogService.getInstance().traceLog("  Number of generic commands:    " + Integer.toString(commandsN)); //$NON-NLS-1$
		StandaloneLogService.getInstance().traceLog("  Number of compiled files:      " + Integer.toString(filesN)); //$NON-NLS-1$

		
		
	}

	/**
	 * @param trace : String
	 */
	public static void metricsTrace(String trace) {
		
		
		StandaloneLogService.getInstance().traceLog(" *** NEW METRICS TRACE 2 ***"); //$NON-NLS-1$
		
		StandaloneLogService.getInstance().traceLog(trace);
		
		
		
	}

}

