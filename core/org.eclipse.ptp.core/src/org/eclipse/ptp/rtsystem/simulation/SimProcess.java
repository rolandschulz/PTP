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
package org.eclipse.ptp.rtsystem.simulation;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.INodeEvent;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IProcessEvent;
import org.eclipse.ptp.core.IProcessListener;
import org.eclipse.ptp.core.NodeEvent;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.ProcessEvent;
import org.eclipse.ptp.internal.core.CoreUtils;
import org.eclipse.ptp.internal.core.OutputTextFile;
import org.eclipse.ptp.internal.core.PElement;
import org.eclipse.ptp.internal.core.PElementInfo;
import org.eclipse.search.ui.ISearchPageScoreComputer;

public class SimProcess extends Process implements IPProcess, IPElement, Comparable {
	InputStream err;
	InputStream in;
	OutputStream out;
	SimThread[] threads;
	SimQueue commands;
	protected HashMap attribs = null;
	protected int ID = -1;
	private PElementInfo elementInfo = null;
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
	private List listeners = new ArrayList();
	/*
	 * the node that this process is running on, or was scheduled on / will be, etc
	 */
	protected IPNode node;
	Thread procThread;
	
	final int numThreads = 1;

	public SimProcess(IPElement element, String name, String key, String pid, int taskId, String status, String exitCode, String signalName) {
		attribs = new HashMap();
		ID = PTPCorePlugin.getDefault().getNewID();
		attribs.put(AttributeConstants.ATTRIB_PARENT, element);
		attribs.put(AttributeConstants.ATTRIB_NAME, name);
		attribs.put(AttributeConstants.ATTRIB_TYPE, new Integer(P_PROCESS));
		System.out.println("NEW PElement - ID = " + ID);
		this.pid = pid;
		attribs.put(AttributeConstants.ATTRIB_TASKID, new Integer(taskId));
		this.exitCode = exitCode;
		setStatus(status);
		setOutputStore();
		outputFile = new OutputTextFile(name, outputDirPath, storeLine);
		SimQueue cmds = null;
		if (cmds == null) {
			commands = new SimQueue();
			initCommands(commands);
		}
		threads = new SimThread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new SimThread(this, i, taskId);
		}
		err = null;
		in = null;
		out = null;
		procThread = new Thread() {
			public void run() {
				/* to give time for others to register the listener to
				this process */
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				outerWhile: while (true) {
					try {
						if (isTerminated)
							break;
						
						for (int i = 0; i < numThreads; i++) {
							if (threads[i].state == threads[i].SUSPENDED) {
								Thread.sleep(2000);
								continue outerWhile;
							}
						}
						ArrayList command = (ArrayList) commands.removeItem();
						String destination = (String) command.get(0);
						String cmd = (String) command.get(1);
						String arg = (String) command.get(2);
						if (!destination.equals("-1")) {
							threads[Integer.parseInt(destination)].runCommand(cmd, arg);
						} else {
							if (cmd.equals("sleep")) {
								Thread.sleep(Integer.parseInt(arg));
							} else if (cmd.equals("exitProcess")) {
								break;
							}
						}
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
				// isTerminated = true;
				for (int i = 0; i < numThreads; i++) {
					threads[i].terminate();
				}
			}
		};
		procThread.start();
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
		IPElement current = this;
		do {
			if (current instanceof IPJob)
				return (IPJob) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	public String getProcessNumber() {
		return "" + getTaskId() + "";
	}
	private void fireEvent(IProcessEvent event) {
		for (Iterator i=listeners.iterator(); i.hasNext();) {
			IProcessListener listener = (IProcessListener)i.next();
			listener.processEvent(event);
		}
	}
	public void setStatus(String status) {
		this.status = status == null ? "unknown" : status;
		if (status != null) {
			fireEvent(new ProcessEvent(getJob().getIDString(), getIDString(), IProcessEvent.STATUS_CHANGE_TYPE, status));
			if (node != null && status.equals(IPProcess.EXITED))
				node.fireEvent(new NodeEvent(node.getMachine().getIDString(), node.getIDString(), INodeEvent.STATUS_UPDATE_TYPE, null));
		}
	}
	public void setExitCode(String exitCode) {
		this.exitCode = exitCode;
		if (exitCode != null) {
			fireEvent(new ProcessEvent(getJob().getIDString(), getIDString(), IProcessEvent.STATUS_EXIT_TYPE, exitCode));
			if (node != null)
				node.fireEvent(new NodeEvent(node.getMachine().getIDString(), node.getIDString(), INodeEvent.STATUS_UPDATE_TYPE, null));
		}
	}
	public void setSignalName(String signalName) {
		this.signalName = signalName;
		if (signalName != null)
			fireEvent(new ProcessEvent(getJob().getIDString(), getIDString(), IProcessEvent.STATUS_SIGNALNAME_TYPE, signalName));
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
		((IPNode) getParent()).removeChild(this);
	}
	public void setTerminated(boolean isTerminated) {
		this.isTerminated = isTerminated;
	}
	public void addOutput(String output) {
		// outputList.add(output);
		// outputList.add("random output from process: " + (counter++));
		outputFile.write(output + "\n");
		fireEvent(new ProcessEvent(getJob().getIDString(), getIDString(), IProcessEvent.ADD_OUTPUT_TYPE, output + "\n"));
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
	public void addProcessListener(IProcessListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	public void removerProcessListener(IProcessListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	public boolean isAllStop() {
		return getStatus().startsWith(EXITED);
	}
	public void setNode(IPNode node) {
		this.node = node;
		if (node != null)
			node.addChild(this);
	}
	public IPNode getNode() {
		return this.node;
	}
	public int getTaskId() {
		return ((Integer) attribs.get(AttributeConstants.ATTRIB_TASKID)).intValue();
	}
	public void addChild(IPElement member) {
		getElementInfo().addChild(member);
	}
	public void removeChild(IPElement member) {
		getElementInfo().removeChild(member);
	}
	public IPElement findChild(String key) {
		return getElementInfo().findChild(key);
	}
	public void removeChildren() {
		getElementInfo().removeChildren();
	}
	public Collection getCollection() {
		PElementInfo info = getElementInfo();
		if (info != null)
			return info.getCollection();
		return null;
	}
	public IPElement[] getChildren() {
		PElementInfo info = getElementInfo();
		if (info != null)
			return info.getChildren();
		return new IPElement[] {};
	}
	public IPElement[] getSortedChildren() {
		IPElement[] elements = getChildren();
		sort(elements);
		return elements;
	}
	public List getChildrenOfType(int type) {
		IPElement[] children = getChildren();
		int size = children.length;
		ArrayList list = new ArrayList(size);
		for (int i = 0; i < size; ++i) {
			PElement elt = (PElement) children[i];
			if (elt.getElementType() == type) {
				list.add(elt);
			}
		}
		return list;
	}
	public boolean hasChildren() {
		return getElementInfo().hasChildren();
	}
	private void quickSort(IPElement element[], int low, int high) {
		int lo = low;
		int hi = high;
		int mid;
		if (high > low) {
			mid = element[(low + high) / 2].getID();
			while (lo <= hi) {
				while ((lo < high) && (element[lo].getID() < mid))
					++lo;
				while ((hi > low) && (element[hi].getID() > mid))
					--hi;
				if (lo <= hi) {
					swap(element, lo, hi);
					++lo;
					--hi;
				}
			}
			if (low < hi)
				quickSort(element, low, hi);
			if (lo < high)
				quickSort(element, lo, high);
		}
	}
	private void swap(IPElement element[], int i, int j) {
		IPElement tempElement;
		tempElement = element[i];
		element[i] = element[j];
		element[j] = tempElement;
	}
	public void sort(IPElement element[]) {
		quickSort(element, 0, element.length - 1);
	}
	protected PElementInfo getElementInfo() {
		if (elementInfo == null)
			elementInfo = new PElementInfo(this);
		return elementInfo;
	}
	/*
	 * public String getKey() { return fKey; }
	 */
	public Object getAttribute(String key) {
		return attribs.get(key);
	}
	public String getElementName() {
		// return NAME_TAG + getKey();
		return (String) attribs.get(AttributeConstants.ATTRIB_NAME);
	}
	public int getID() {
		return ID;
	}
	public String getIDString() {
		return "" + ID + "";
	}
	/**
	 * @param name
	 *            The Name to set.
	 */
	public void setElementName(String name) {
		attribs.put(AttributeConstants.ATTRIB_NAME, name);
	}
	/**
	 * @return Returns the Parent.
	 */
	public IPElement getParent() {
		return (IPElement) attribs.get(AttributeConstants.ATTRIB_PARENT);
	}
	/**
	 * @param parent
	 *            The Parent to set.
	 */
	public void setParent(IPElement parent) {
		attribs.put(AttributeConstants.ATTRIB_PARENT, parent);
	}
	/**
	 * @return Returns the Type.
	 */
	public int getElementType() {
		Integer i = (Integer) attribs.get(AttributeConstants.ATTRIB_TYPE);
		if (i == null)
			return P_TYPE_ERROR;
		else
			return i.intValue();
	}
	/**
	 * @param type
	 *            The Type to set.
	 */
	public void setElementType(int type) {
		attribs.put(AttributeConstants.ATTRIB_TYPE, new Integer(type));
	}
	public String toString() {
		return getElementName();
	}
	public int size() {
		return getElementInfo().size();
	}
	public int compareTo(Object obj) {
		if (obj instanceof IPElement) {
			int my_rank = getID();
			int his_rank = ((IPElement) obj).getID();
			if (my_rank < his_rank)
				return -1;
			if (my_rank == his_rank)
				return 0;
			if (my_rank > his_rank)
				return 1;
		}
		return 0;
	}
	public int computeScore(String pageId, Object element) {
		if (!CoreUtils.PTP_SEARCHPAGE_ID.equals(pageId))
			return ISearchPageScoreComputer.UNKNOWN;
		if (element instanceof IPElement)
			return 90;
		return ISearchPageScoreComputer.LOWEST;
	}
	public InputStream getErrorStream() {
		return err;
	}
	public InputStream getInputStream() {
		return in;
	}
	public OutputStream getOutputStream() {
		return out;
	}
	public void destroy() {
		setTerminated(true);
	}
	private void initCommands(SimQueue cmds) {
		ArrayList cmd, /*cmd2,*/ cmd3/*, cmd4*/;
		cmd = new ArrayList();
		cmd.add(0, "0");
		cmd.add(1, "print");
		cmd.add(2, "ProcessOutput");
		/*
		 * cmd2 = new ArrayList(); cmd2.add(0, "0"); cmd2.add(1, "break"); cmd2.add(2, "5");
		 */
		cmd3 = new ArrayList();
		cmd3.add(0, "-1");
		cmd3.add(1, "exitProcess");
		cmd3.add(2, "");
		// cmds.addItem(cmd2);
		for (int j = 0; j < 20; j++) {
			cmds.addItem(cmd);
		}
		cmds.addItem(cmd3);
	}
	public int exitValue() {
		if (isTerminated)
			return 0;
		else
			throw new IllegalThreadStateException();
	}
	public int waitFor() throws InterruptedException {
		try {
			procThread.join();
		} catch (InterruptedException e) {
		}
		return 0;
	}
	public SimThread getThread(int tId) {
		return threads[tId];
	}
	public SimThread[] getThreads() {
		return threads;
	}
	public int getThreadCount() {
		return threads.length;
	}
	public void setAttribute(String key, Object o) {
		attribs.put(key, o);
	}
}
