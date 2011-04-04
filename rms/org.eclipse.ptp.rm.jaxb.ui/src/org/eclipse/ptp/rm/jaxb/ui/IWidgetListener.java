package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;

public interface IWidgetListener extends SelectionListener, ModifyListener, ICheckStateListener {
	void valueChanged();
}
