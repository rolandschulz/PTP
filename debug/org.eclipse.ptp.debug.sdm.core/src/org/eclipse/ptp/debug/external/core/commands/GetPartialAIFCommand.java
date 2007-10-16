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
package org.eclipse.ptp.debug.external.core.commands;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.aif.AIF;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugAIF;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugPartialAIFEvent;

/**
 * @author Clement chu
 * 
 */
public class GetPartialAIFCommand extends AbstractDebugCommand {
	private String name = "";
	private String key = "";
	private boolean listChildren = false;
	private boolean express = false;
	
	public GetPartialAIFCommand(BitList tasks, String name, String key, boolean listChildren, boolean express) {
		super(tasks);
		this.name = name;
		this.key = key;
		this.listChildren = listChildren;
		this.express = express;
	}
	public GetPartialAIFCommand(BitList tasks, String name, String key, boolean listChildren) {
		this(tasks, name, key, listChildren, false);
	}
	public GetPartialAIFCommand(BitList tasks, String name, String key) {
		this(tasks, name, key, false, false);
	}
	public void preExecCommand(IAbstractDebugger debugger) throws PCDIException {
		checkBeforeExecCommand(debugger);
	}
	public void exec(IAbstractDebugger debugger) throws PCDIException {
		debugger.getPartialAIF(tasks, name, key, listChildren, express);
	}
	public IAIF getPartialAIF() throws PCDIException {
		Object res = getResultValue();
		if (res instanceof IProxyDebugPartialAIFEvent) {
			ProxyDebugAIF pa = ((IProxyDebugPartialAIFEvent)res).getData();
			return new AIF(pa.getFDS(), pa.getData(), pa.getDescription());
		}
		throw new PCDIException("No aif found.");
	}
	public String getName() throws PCDIException {
		Object res = getResultValue();
		if (res instanceof IProxyDebugPartialAIFEvent) {
			return ((IProxyDebugPartialAIFEvent)res).getName();
		}
		throw new PCDIException("No aif found.");
	}
	public String getCommandName() {
		return "Get Partial AIF: " + name;
	}
}
