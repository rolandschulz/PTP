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
package org.eclipse.ptp.internal.core.elements;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.core.elements.events.IProcessChangedEvent;
import org.eclipse.ptp.core.elements.listeners.IProcessListener;
import org.eclipse.ptp.core.util.OutputTextFile;
import org.eclipse.ptp.internal.core.elements.events.ProcessChangedEvent;

public class PProcess extends Parent implements IPProcessControl {

	private final ListenerList elementListeners = new ListenerList();
	private OutputTextFile outputFile = null;
	private String outputDirPath = null;
	private int storeLines = 0;
	/*
	 * the node that this process is running on, or was scheduled on / will be, etc
	 */
	private IPNodeControl node;

	public PProcess(String id, IPJobControl job, IAttribute[] attrs) {
		super(id, job, P_PROCESS, attrs);
		setOutputStore();
		
		/*
		 * Derive a unique name for the output file
		 */
		String name = job.getQueue().getResourceManager().getName()
						+ ":" + job.getQueue().getName()
						+ ":" + job.getName()
						+ ":" + getName();
	
		outputFile = new OutputTextFile(name, 
							ProcessAttributes.getStdoutAttributeDefinition().getId(), 
							outputDirPath, storeLines);
		
		/*
		 * Make sure we always have a state.
		 */
		EnumeratedAttribute<State> procState = getStateAttribute();
		if (procState == null) {
			procState = ProcessAttributes.getStateAttributeDefinition().create();
			addAttribute(procState);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#addElementListener(org.eclipse.ptp.core.elements.listeners.IProcessListener)
	 */
	public void addElementListener(IProcessListener listener) {
		elementListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#addNode(org.eclipse.ptp.core.elements.IPNode)
	 */
	public void addNode(IPNode node) {
		this.node = (IPNodeControl) node;
		if (node != null) {
			this.node.addProcess(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#clearOutput()
	 */
	public void clearOutput() {
		outputFile.delete();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#getExitCode()
	 */
	public int getExitCode() {
		IntegerAttribute attr = (IntegerAttribute) getAttribute(ProcessAttributes.getExitCodeAttributeDefinition());
		if (attr != null) {
			return attr.getValue();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#getJob()
	 */
	public IPJob getJob() {
		return getJobControl();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPProcessControl#getJobControl()
	 */
	public IPJobControl getJobControl() {
		IPElementControl current = this;
		do {
			if (current instanceof IPJobControl)
				return (IPJobControl) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#getNode()
	 */
	public IPNode getNode() {
		return this.node;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#getPid()
	 */
	public int getPid() {
		IntegerAttribute attr = (IntegerAttribute) getAttribute(ProcessAttributes.getPIDAttributeDefinition());
		if (attr != null) {
			return attr.getValue();
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#getProcessIndex()
	 */
	public String getProcessIndex() {
		IntegerAttribute attr = (IntegerAttribute) getAttribute(ProcessAttributes.getIndexAttributeDefinition());
		if (attr != null) {
			return attr.getValueAsString();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#getSavedOutput(org.eclipse.ptp.core.attributes.IAttributeDefinition)
	 */
	public String getSavedOutput(IAttributeDefinition attrDef) {
		if (attrDef == ProcessAttributes.getStdoutAttributeDefinition()) {
			return outputFile.getContents();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#getSignalName()
	 */
	public String getSignalName() {
		StringAttribute attr = (StringAttribute) getAttribute(ProcessAttributes.getSignalNameAttributeDefinition());
		if (attr != null) {
			return attr.getValue();
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#getState()
	 */
	public State getState() {
		EnumeratedAttribute<State> attr = getStateAttribute();
		return attr.getValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#isTerminated()
	 */
	public boolean isTerminated() {
		EnumeratedAttribute<State> procState = getStateAttribute();
		State state = procState.getValue();
		if (state == State.ERROR || state == State.EXITED || state == State.EXITED_SIGNALLED) {
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#removeElementListener(org.eclipse.ptp.core.elements.listeners.IProcessListener)
	 */
	public void removeElementListener(IProcessListener listener) {
		elementListeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#removeNode()
	 */
	public void removeNode() {
		if (node != null) {
			node.removeProcess(this);
		}
		node = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#setState(org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State)
	 */
	public void setState(State state) {
		EnumeratedAttribute<State> procState = getStateAttribute();
		procState.setValue(state);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPProcess#setTerminated(boolean)
	 */
	public void setTerminated(boolean isTerminated) {
		setState(State.EXITED);
	}

	/**
	 * @param output
	 */
	private void addOutput(String output) {
		outputFile.write(output + "\n");
	}

	/**
	 * @param attrs
	 */
	private void fireChangedProcess(Collection<IAttribute> attrs) {
		IProcessChangedEvent e = 
			new ProcessChangedEvent(this, attrs);
		
		for (Object listener : elementListeners.getListeners()) {
			((IProcessListener)listener).handleEvent(e);
		}
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private EnumeratedAttribute<State> getStateAttribute() {
		return (EnumeratedAttribute<State>) getAttribute(
				ProcessAttributes.getStateAttributeDefinition());
	}

	/**
	 * 
	 */
	private void setOutputStore() {
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		outputDirPath = preferences.getString(PreferenceConstants.OUTPUT_DIR);
		storeLines = preferences.getInt(PreferenceConstants.STORE_LINE);
		if (outputDirPath == null || outputDirPath.length() == 0) {
			outputDirPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(PreferenceConstants.DEF_OUTPUT_DIR_NAME).toOSString();
		}
		if (storeLines == 0) {
			storeLines = PreferenceConstants.DEF_STORE_LINE;
		}
		File outputDirectory = new File(outputDirPath);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java.util.List)
	 */
	@Override
	protected void doAddAttributeHook(List<IAttribute> attrs) {
		for (IAttribute attr : attrs) {
			if (attr.getDefinition() == ProcessAttributes.getStdoutAttributeDefinition()) {
				addOutput(attr.getValueAsString());
			}
		}
		fireChangedProcess(attrs);
	}

}
