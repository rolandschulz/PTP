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
package org.eclipse.ptp.debug.core.cdi.model;

import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public interface IPCDIVariableDescriptor extends IPCDIObject {
	String getName();
	IAIFType getType() throws PCDIException;
	String getTypeName() throws PCDIException;
	int sizeof() throws PCDIException;
	String getQualifiedName() throws PCDIException;
	IPCDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws PCDIException;
	IPCDIVariableDescriptor getVariableDescriptorAsType(String type) throws PCDIException;
	boolean equals(IPCDIVariableDescriptor varDesc);

	void setCastingArrayStart(int start);
	int getCastingArrayStart();
	void setCastingArrayEnd(int end);
	int getCastingArrayEnd();
	
	void setAIF(IAIF aif);
	
	//IPCDIVariable[] getVariablesAsArray(int start, int length) throws PCDIException;
	//IPCDIVariable[] getVariables() throws PCDIException;
}
