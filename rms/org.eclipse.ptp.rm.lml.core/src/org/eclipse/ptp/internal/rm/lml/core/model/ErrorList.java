/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */

package org.eclipse.ptp.internal.rm.lml.core.model;

import java.util.ArrayList;

import org.eclipse.ptp.internal.rm.lml.core.messages.Messages;

/**
 * This class collects errors, which occur while parsing and checking lml-files
 * 
 */
public class ErrorList {

	private final ArrayList<String> errors;

	private boolean realerror;

	public ErrorList() {
		errors = new ArrayList<String>();
		realerror = false;
	}

	/**
	 * An error occurred. Add it to the list
	 * 
	 * @param errormsg
	 */
	public void addError(String errormsg) {
		errors.add(errormsg);
		realerror = true;
	}

	public void addMessage(String msg) {
		errors.add(msg);
	}

	/**
	 * @return true, if addError was used, else false
	 */
	public boolean isRealError() {
		return realerror;
	}

	@Override
	public String toString() {

		String res = ""; //$NON-NLS-1$

		if (realerror) {
			res = Messages.ErrorList_1;
		} else
			res = Messages.ErrorList_2;

		for (int i = 0; i < errors.size(); i++) {
			res += errors.get(i) + "\n"; //$NON-NLS-1$
		}

		return res;

	}

}
