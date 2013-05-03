/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/

package org.eclipse.ptp.internal.rm.lml.core.events;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.events.ITableFilterEvent;
import org.eclipse.ptp.rm.lml.core.model.IPattern;

public class TableFilterEvent implements ITableFilterEvent {

	private List<IPattern> pattern;

	private final String gid;

	public TableFilterEvent(String gid, List<IPattern> pattern) {
		this.gid = gid;
		if (pattern == null) {
			pattern = new LinkedList<IPattern>();
		} else {
			this.pattern = pattern;
		}
	}

	public String getGid() {
		return gid;
	}

	public List<IPattern> getPattern() {
		return pattern;
	}

}
