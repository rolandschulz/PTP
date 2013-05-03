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

package org.eclipse.ptp.rm.lml.core.model;

import org.eclipse.ptp.internal.rm.lml.core.JAXBUtil;
import org.eclipse.ptp.rm.lml.core.elements.LguiType;

/**
 * The LML-Manager in class LML administrates different encapsulated classes,
 * which manage special parts of the lml-object-hierarchy. These classes, which
 * get information from the lml-tree, sort parts of the information or add
 * extra-information to the mostly redundant-free lml-tree, have some
 * functions in common. These functions are collected in this interface.
 * So every module, which handles the LML-datamodel within the LML-class
 * should extend this class.
 * 
 */
public class LguiHandler implements ILguiHandler {

	/**
	 * instance of LML, which is the surrounding handler of this
	 * 
	 * This instance is needed to notify all LMLHandler, if any data of
	 * the LguiType-instance was changed.
	 */
	protected ILguiItem lguiItem;

	/**
	 * saves current lml-model, which is partly managed by this class
	 */
	protected LguiType lgui;

	protected static final JAXBUtil jaxbUtil = JAXBUtil.getInstance();

	/**
	 * @param lguiItem
	 *            surrounding lml-manager needed to fire events, if this handler changed data
	 */
	public LguiHandler(ILguiItem lguiItem, LguiType lgui) {
		this.lguiItem = lguiItem;
		this.lgui = lgui;
	}

	public void update(LguiType lgui) {
		this.lgui = lgui;
	}
}
