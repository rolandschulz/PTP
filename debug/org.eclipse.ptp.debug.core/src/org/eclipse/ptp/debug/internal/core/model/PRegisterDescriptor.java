/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.ptp.debug.core.model.IPRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterGroup;

/**
 * @author clement
 *
 */
public class PRegisterDescriptor implements IPRegisterDescriptor {
	private String fName;
	private String fGroupName;
	private IPDIRegisterDescriptor fPDIDescriptor = null;

	public PRegisterDescriptor(String name, String groupName) {
		fName = name;
		fGroupName = groupName;
	}
	public PRegisterDescriptor(IPDIRegisterGroup group, IPDIRegisterDescriptor desc) {
		fName = desc.getName();
		fGroupName = group.getName();
		fPDIDescriptor = desc;
	}
	public String getName() {
		return fName;
	}
	public String getGroupName() {
		return fGroupName;
	}
	public IPDIRegisterDescriptor getPDIDescriptor() {
		return fPDIDescriptor;
	}
	public void setPDIDescriptor(IPDIRegisterDescriptor descriptor) {
		fPDIDescriptor = descriptor;
	}
}
