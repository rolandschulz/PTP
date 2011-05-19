package org.eclipse.ptp.rm.lml.ui.actions;

import java.util.Map;

import org.eclipse.jface.action.Action;

public class ShowViewAction extends Action {

	private String gid = null;

	public ShowViewAction(Map.Entry<String, String> gid) {
		super(gid.getKey());
		this.gid = gid.getValue();
	}

	@Override
	public void run() {
		// LMLManager.getInstance().addView(gid);
	}

}
