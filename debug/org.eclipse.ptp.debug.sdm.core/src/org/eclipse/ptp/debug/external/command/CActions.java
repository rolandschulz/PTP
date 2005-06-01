/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CActions extends DebugCommand {
	public CActions() {
		super("actions");
	}
	
	public CActions(String type) {
		super("actions", new String[] {type});
	}
	
	public CActions(int[] ids) {
		super("actions");
		int idsLen = ids.length;
		String [] strs = new String[idsLen];
		for (int i = 0; i < idsLen; i++)
			strs[i] = Integer.toString(ids[i]);
		
		setArgs(strs);
	}
}
