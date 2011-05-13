package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;

public class ShowViewAction extends Action{
	
	private String gid = null;
	
	public ShowViewAction(String gid){
		super(gid);
		this.gid = gid;
	}
	
	public void run() {
		LMLCorePlugin.getDefault().getLMLManager().addView(gid);
	}

}
