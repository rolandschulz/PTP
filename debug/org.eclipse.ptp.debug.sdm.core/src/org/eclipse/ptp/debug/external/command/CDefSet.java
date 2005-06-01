/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CDefSet extends DebugCommand {
	public CDefSet(String name, int[] ids) {
		super("defSet");
		int idsLen = ids.length;
		String[] strs = new String[1 + idsLen];
		strs[0] = name;
		for (int i = 0; i < idsLen; i++)
			strs[i + 1] = Integer.toString(ids[i]);
		
		setArgs(strs);
	}
}
