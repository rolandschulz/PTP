/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.swt.widgets.Composite;


/**
 * This class is super-class of all Composites showing LML-components with SWT. Every Composite
 * administrates an instance of LML to access important data in an
 * easy way. Every component should initialize the lml-instance by calling
 * the LMLWidget-constructor.
 */ 
public class LMLWidget extends Composite{
	
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
	public LMLWidget(ILguiItem lguiItem, Composite parent, int style){
		super(parent, style);
		this.lguiItem = lguiItem;
	}

}