/****************************************************************************
 * Parallel Performance Wizard (PPW)
 * http://ppw.hcs.ufl.edu
 * 
 * Copyright (c) 2010, University of Florida
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Max Billingsley III - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.ppw;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.etfw.internal.BuildLaunchUtils;

public class PPWController {
	private final ProcessBuilder ppwpb;
	private final ProcessBuilder ppwconfigpb;
	private final PPWDataManager ppwDataManager;
	private String ppwVersion;
	private boolean ppwSupportsControl;

	public PPWController(PPWDataManager ppwDataManager, String[] args) {
		this.ppwDataManager = ppwDataManager;

		BuildLaunchUtils blt = new BuildLaunchUtils();
		String ppwToolPath = blt.getToolPath("ppw"); //$NON-NLS-1$
		String ppw = ppwToolPath + File.separator + "ppw"; //$NON-NLS-1$
		String ppwConfig = ppwToolPath + File.separator + "ppw-config"; //$NON-NLS-1$

		List<String> ppwConfigCmd = new ArrayList<String>();
		ppwConfigCmd.add(ppwConfig);
		ppwConfigCmd.add("--version"); //$NON-NLS-1$

		ppwconfigpb = new ProcessBuilder(ppwConfigCmd);
		Process ppwconf = null;
		try {
			ppwconf = ppwconfigpb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ppwVersion = null;
		ppwSupportsControl = false;

		try {
			InputStreamReader isr = new InputStreamReader(ppwconf.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			ppwVersion = br.readLine();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		// Try to figure out if our PPW version supports "control mode"
		if (ppwVersion != null) {
			int vMajor = Integer.parseInt(ppwVersion.substring(0, 1));
			String ppwVerRest = ppwVersion.substring(2);

			// PPW versions up to 2.4 didn't support "control mode"
			if (vMajor < 2) {
				ppwSupportsControl = false;
			} else if (vMajor == 2
					&& (ppwVerRest.equals("0") || ppwVerRest.equals("1") || ppwVerRest.equals("2") || ppwVerRest.equals("4"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				ppwSupportsControl = false;
			} else {
				ppwSupportsControl = true;
			}
		}

		// Build command to invoke the PPW GUI
		List<String> ppwCmd = new ArrayList<String>();
		ppwCmd.add(ppw);
		if (ppwSupportsControl) {
			ppwCmd.add("-c"); //$NON-NLS-1$
		}

		for (String s : args) {
			ppwCmd.add(s);
		}

		ppwpb = new ProcessBuilder(ppwCmd);

		Process p = null;
		try {
			p = ppwpb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		new Thread(new IOWatcher(p.getInputStream())).start();
	}

	class IOWatcher implements Runnable {
		InputStream is;

		IOWatcher(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("source")) { //$NON-NLS-1$
						String[] split = line.split(" "); //$NON-NLS-1$
						String filename = split[1];
						int linenum = Integer.parseInt(split[2]);
						ppwDataManager.highlightSourceLine(filename, linenum);
					} else if (line.startsWith("exit")) { //$NON-NLS-1$
						break;
					} else {
						System.out.println(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
