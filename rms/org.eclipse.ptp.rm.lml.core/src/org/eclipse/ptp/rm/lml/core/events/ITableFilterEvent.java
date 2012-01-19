/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/

package org.eclipse.ptp.rm.lml.core.events;

import java.util.List;

import org.eclipse.ptp.rm.lml.core.model.IPattern;

public interface ITableFilterEvent {

	public String getGid();

	public List<IPattern> getPattern();

}
