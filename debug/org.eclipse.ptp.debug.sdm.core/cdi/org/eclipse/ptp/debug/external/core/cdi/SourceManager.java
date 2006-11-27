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
package org.eclipse.ptp.debug.external.core.cdi;

import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;
import org.eclipse.ptp.debug.external.core.commands.GetAIFCommand;

/**
 * @author Clement chu
 * 
 */
public class SourceManager extends Manager {
	public SourceManager(Session session) {
		super(session, false);
	}
	public void update(Target target) throws PCDIException {
		//Do dothing here
	}
	public void shutdown() {}
	
	public IAIF getDetailAIFFromVariable(StackFrame frame, String name) throws PCDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			return getAIF(target, name);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}
	public IAIF getAIF(Target target, String name) throws PCDIException {
		GetAIFCommand command = new GetAIFCommand(target.getTask(), name);
		target.getDebugger().postCommand(command);
		return command.getAIF();
	}
	
	public void setSourcePaths(Target target, String[] dirs) throws PCDIException {
		throw new PCDIException (" SourceManager - setSourcePaths not implemented yet");
		/*
		Session session = (Session)getSession();
		MIEnvironmentDirectory dir = factory.createMIEnvironmentDirectory(true, dirs);
		try {
			session.getDebugger().postCommand(dir);
			dir.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		*/
	}
	public String[] getSourcePaths(Target target) throws PCDIException {
		throw new PCDIException (" SourceManager - getSourcePaths not implemented yet");
		/*
		Session session = (Session)getSession();
		MIGDBShowDirectories dir = factory.createMIGDBShowDirectories();
		try {
			mi.postCommand(dir);
			MIGDBShowDirectoriesInfo info = dir.getMIGDBShowDirectoriesInfo();
			return info.getDirectories();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		*/
	}	
}
