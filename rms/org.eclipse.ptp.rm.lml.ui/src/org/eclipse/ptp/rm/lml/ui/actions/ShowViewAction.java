package org.eclipse.ptp.rm.lml.ui.actions;

import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.rm.lml.core.LMLManager;

public class ShowViewAction extends Action{
	
	private String gid = null;
	
	public ShowViewAction(Map.Entry<String, String> gid){
		super(gid.getKey());
		this.gid = gid.getValue();
	}
	
	public void run() {
		LMLManager.getInstance().addView(gid);
	}

}
