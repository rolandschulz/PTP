/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.model;

import java.util.ArrayList;



/**
 * @author donny
 *
 */
public class MProcessSet {
	private ArrayList processList = null;
	private String groupName;
	
	public MProcessSet(String name) {
		groupName = name;
		processList = new ArrayList();
	}
	
	public void addProcess(MProcess proc) {
		if (!processList.contains(proc))
			processList.add(proc);
	}
	
	public void delProcess(MProcess proc) {
		if (processList.contains(proc))
			processList.remove(proc);
	}
	
	public int getSize() {
		return processList.size();
	}
	
	public String getName() {
		return groupName;
	}
	
	public MProcess getProcess(int index) {
		return (MProcess) processList.get(index);
	}
	
	public MProcess[] getProcessList() {
		return (MProcess[]) processList.toArray();
	}
	
	public void clear() {
		processList.clear();
	}
}
