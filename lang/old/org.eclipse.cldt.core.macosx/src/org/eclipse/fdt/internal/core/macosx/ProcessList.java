package org.eclipse.fdt.internal.core.macosx;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.fdt.core.IProcessInfo;
import org.eclipse.fdt.core.IProcessList;
import org.eclipse.fdt.utils.spawner.ProcessFactory;

/**
 * Insert the type's description here.
 * @see IProcessList
 */
public class ProcessList implements IProcessList {
	
	ProcessInfo[] empty = new ProcessInfo[0];
	
	public ProcessList() {
	}
	
	/**
	 * Insert the method's description here.
	 * @see IProcessList#getProcessList
	 */
	public IProcessInfo [] getProcessList()  {
		Process ps;
		BufferedReader psOutput;
		String[] args = {"/bin/ps", "-a", "-o", "pid,command"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		try {
			ps = ProcessFactory.getFactory().exec(args);
			psOutput = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		} catch(Exception e) {
			return new IProcessInfo[0];
		}
		
		//Read the output and parse it into an array list
		ArrayList procInfo = new ArrayList();

		try {
			String lastline;
			while ((lastline = psOutput.readLine()) != null) {
				//The format of the output should be 
				//PID space name

				lastline = lastline.trim();
				int index = lastline.indexOf(' ');
				if (index != -1) {
					String pidString = lastline.substring(0, index).trim();
					try {
						int pid = Integer.parseInt(pidString);
						String arg = lastline.substring(index + 1);
						procInfo.add(new ProcessInfo(pid, arg));
					} catch (NumberFormatException e) {
					}
				}
			}
		
		} catch(Exception e) {
			/* Ignore */
		}
		
		ps.destroy();
		return (IProcessInfo [])procInfo.toArray(new IProcessInfo[procInfo.size()]);
	}
	
}
