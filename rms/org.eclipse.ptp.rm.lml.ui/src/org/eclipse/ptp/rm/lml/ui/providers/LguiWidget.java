package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.swt.widgets.Composite;

public class LguiWidget extends Composite{
	
	private static final long serialVersionUID = 1L;
	
	protected ILguiItem lguiItem;//wrapper instance around LguiType-instance -- provides easy access to lml-information
	
	/**
	 * Create the smallest possible LMLWidget by passing the lml-model
	 * wrapper class plml, which manages all important data.
	 * 
	 * @param lguiItem LML-Manager
	 * @param parent parent of this component
	 * @param style SWT-Style
	 */
	public LguiWidget(ILguiItem lguiItem, Composite parent, int style){
		super(parent, style);
		this.lguiItem = lguiItem;
	}

}