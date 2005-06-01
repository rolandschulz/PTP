/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CEnable extends DebugCommand {
	public CEnable(String type) {
		super("enable", new String[] {type});
	}
	
	public CEnable(int[] ids) {
		super("enable");
		int idsLen = ids.length;
		String [] strs = new String[idsLen];
		for (int i = 0; i < idsLen; i++)
			strs[i] = Integer.toString(ids[i]);
		
		setArgs(strs);
	}
}
