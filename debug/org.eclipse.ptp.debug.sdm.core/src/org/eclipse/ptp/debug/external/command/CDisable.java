/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CDisable extends DebugCommand {
	public CDisable(String type) {
		super("disable", new String[] {type});
	}
	
	public CDisable(int[] ids) {
		super("disable");
		int idsLen = ids.length;
		String [] strs = new String[idsLen];
		for (int i = 0; i < idsLen; i++)
			strs[i] = Integer.toString(ids[i]);
		
		setArgs(strs);
	}
}
