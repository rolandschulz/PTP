/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CDelete extends DebugCommand {
	public CDelete(String type) {
		super("delete", new String[] {type});
	}
	
	public CDelete(int[] ids) {
		super("delete");
		int idsLen = ids.length;
		String [] strs = new String[idsLen];
		for (int i = 0; i < idsLen; i++)
			strs[i] = Integer.toString(ids[i]);
		
		setArgs(strs);
	}
}
