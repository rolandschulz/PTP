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
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IEnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IIntegerAttribute;
import org.eclipse.ptp.core.attributes.IStringAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.core.util.OutputTextFile;

public class PProcess extends Parent implements IPProcessControl {
	protected String NAME_TAG = "process ";
	private boolean isTerminated = false;
	private OutputTextFile outputFile = null;
	protected String outputDirPath = null;
	protected int storeLine = 0;
	/*
	 * the node that this process is running on, or was scheduled on / will be, etc
	 */
	protected IPNodeControl node;

	public PProcess(int id, IPJobControl job, IAttribute[] attrs) {
		super(id, job, P_PROCESS, attrs);
		setOutputStore();
		outputFile = new OutputTextFile(getName(), outputDirPath, storeLine);
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
	
	public void setStatus(ProcessAttributes.State state) {
		IEnumeratedAttribute procState = (IEnumeratedAttribute) getAttribute(ProcessAttributes.getStateAttributeDefinition());
		try {
			if (procState == null) {
				procState = ProcessAttributes.getStateAttributeDefinition().create(state);
			} else {
				procState.setValue(state);
			}
		} catch (IllegalValueException e) {
		}
		if (isAllStop()) {
			isTerminated = true;
		}
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
		outputFile.write(output + "\n");
	}
	
	public String getContents() {
		return outputFile.getContents();
	}
	
	public String[] getOutputs() {
		return null;
	}
	
	public void clearOutput() {
		outputFile.delete();
	}
	
	public boolean isAllStop() {
		ProcessAttributes.State state = getStatus();
		return (state == State.ERROR || state == State.EXITED || state == State.EXITED_SIGNALLED);
	}
	
	public void setNode(IPNode node) {
		this.node = (IPNodeControl) node;
		if (node != null)
			this.node.addProcess(this);
	}
	
	public IPNode getNode() {
		return this.node;
	}

	public int getExitCode() {
		IIntegerAttribute attr = (IIntegerAttribute) getAttribute(ProcessAttributes.getExitCodeAttributeDefinition());
		if (attr != null) {
			return attr.getValue();
		}
		return 0;
	}

	public int getPid() {
		IIntegerAttribute attr = (IIntegerAttribute) getAttribute(ProcessAttributes.getPIDAttributeDefinition());
		if (attr != null) {
			return attr.getValue();
		}
		return 0;
	}

	public String getSignalName() {
		IStringAttribute attr = (IStringAttribute) getAttribute(ProcessAttributes.getSignalNameAttributeDefinition());
		if (attr != null) {
			return attr.getValue();
		}
		return "";
	}

	public State getStatus() {
		IEnumeratedAttribute attr = (IEnumeratedAttribute) getAttribute(ProcessAttributes.getStateAttributeDefinition());
		if (attr != null) {
			return (State) attr.getEnumValue();
		}
		return State.ERROR;
	}

	public int getTaskId() {
		IIntegerAttribute attr = (IIntegerAttribute) getAttribute(ProcessAttributes.getTaskIdAttributeDefinition());
		if (attr != null) {
			return attr.getValue();
		}
		return 0;
	}
}
