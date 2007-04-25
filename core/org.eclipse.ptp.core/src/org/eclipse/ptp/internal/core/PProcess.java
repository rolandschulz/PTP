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
package org.eclipse.ptp.internal.core;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.util.OutputTextFile;

public class PProcess extends Parent implements IPProcessControl {
	protected String NAME_TAG = "process ";
	private String pid = null;
	private String status = null;
	private String exitCode = null;
	private String signalName = null;
	private boolean isTerminated = false;
	// private List outputList = new ArrayList();
	private OutputTextFile outputFile = null;
	protected String outputDirPath = null;
	protected int storeLine = 0;
	/*
	 * the node that this process is running on, or was scheduled on / will be, etc
	 */
	protected IPNodeControl node;

	public PProcess(int id, IPJobControl job, IAttribute[] attrs) {
		super(id, job, P_PROCESS, attrs);
		this.pid = getPID(attrs);
		this.setAttribute(AttributeConstants.ATTRIB_TASKID, new Integer(getTaskID(attrs)));
		this.setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(false));
		this.status = getStatus(attrs);
		setOutputStore();
		outputFile = new OutputTextFile(getElementName(), outputDirPath, storeLine);
	}
	
	private static String getStatus(IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			if (attr.getDefinition() == AttributeDefinitionManager.getNameAttributeDefinition()) {
				return attr.getValueAsString();
			}
		}
		return "";
	}
	
	private static String getPID(IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			if (attr.getDefinition() == AttributeDefinitionManager.getNameAttributeDefinition()) {
				return attr.getValueAsString();
			}
		}
		return "";
	}
	
	private static String getTaskID(IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			if (attr.getDefinition() == AttributeDefinitionManager.getNameAttributeDefinition()) {
				return attr.getValueAsString();
			}
		}
		return "";
	}
	
	private void setOutputStore() {
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		outputDirPath = preferences.getString(PreferenceConstants.OUTPUT_DIR);
		storeLine = preferences.getInt(PreferenceConstants.STORE_LINE);
		if (outputDirPath == null || outputDirPath.length() == 0)
			outputDirPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(PreferenceConstants.DEF_OUTPUT_DIR_NAME).toOSString();
		if (storeLine == 0)
			storeLine = PreferenceConstants.DEF_STORE_LINE;
		File outputDirectory = new File(outputDirPath);
		if (!outputDirectory.exists())
			outputDirectory.mkdir();
	}
	
	public IPJob getJob() {
		IPElementControl current = this;
		do {
			if (current instanceof IPJobControl)
				return (IPJobControl) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	
	public String getProcessNumber() {
		return "" + getTaskId() + "";
	}
	
	public void setStatus(String status) {
		if (status == null) {
			status = "unknown";
		}
		if (!isTerminated)
			this.status = status;

		if (this.status.equals(ERROR) || this.status.startsWith(EXITED)) {
			isTerminated = true;
		}
	}
	
	public void setExitCode(String exitCode) {
		this.exitCode = exitCode;
	}
	
	public void setSignalName(String signalName) {
		this.signalName = signalName;
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public String getPid() {
		return pid;
	}
	
	public String getExitCode() {
		return exitCode;
	}
	
	public String getSignalName() {
		return signalName;
	}
	
	public String getStatus() {
		return status;
	}
	
	public boolean isTerminated() {
		return isTerminated;
	}
	
	public void removeProcess() {
		final IPNodeControl parent = (IPNodeControl) getParent();
		if (parent != null)
			parent.removeProcess(this);
	}
	
	public void setTerminated(boolean isTerminated) {
		this.isTerminated = isTerminated;
	}
	
	public void addOutput(String output) {
		// outputList.add(output);
		// outputList.add("random output from process: " + (counter++));
		outputFile.write(output + "\n");
	}
	
	public String getContents() {
		// String[] array = new String[outputList.size()];
		// return (String[]) outputList.toArray( array );
		return outputFile.getContents();
	}
	
	public String[] getOutputs() {
		// String[] array = new String[outputList.size()];
		// return (String[]) outputList.toArray( array );
		return null;
	}
	
	public void clearOutput() {
		outputFile.delete();
		// outputList.clear();
	}
	
	public boolean isAllStop() {
		return (getStatus().startsWith(EXITED) || getStatus().startsWith(ERROR));
	}
	
	public void setNode(IPNode node) {
		this.node = (IPNodeControl) node;
		if (node != null)
			this.node.addProcess(this);
	}
	
	public IPNode getNode() {
		return this.node;
	}
	
	// TODO Should not be a method!
	public int getTaskId() {
		return ((Integer) this.getAttribute(AttributeConstants.ATTRIB_TASKID)).intValue();
	}
	
	public String getName() {
		return getElementName();
	}
	
	public IPProcess getParentProcess() {
		return (IPProcess) getParent();
	}
	
	public int getNumChildProcesses() {
		return size();
	}
}
