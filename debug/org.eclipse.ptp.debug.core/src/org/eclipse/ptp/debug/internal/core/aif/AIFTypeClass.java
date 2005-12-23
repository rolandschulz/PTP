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
package org.eclipse.ptp.debug.internal.core.aif;

import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFTypeClass;

public class AIFTypeClass extends TypeAggregate implements IAIFTypeClass {
	//{ID|N1=F1,...; N2=F2,...; N3=F3,...; N4=F4,...}
	public AIFTypeClass(String format) {
		super(format);
	}
	public String toString() {
		return AIFFactory.FDS_STRUCT + super.toString() + AIFFactory.FDS_CLASS_END;
	}
	
	/*
	private int 				id = -1;
	private AIFTypeField[]	publicFields;
	private AIFTypeField[]	protectedFields;
	private AIFTypeField[]	privateFields;
	private AIFTypeField[]	hiddenFields;
	
	public AIFTypeClass(AIFTypeField[] pub, AIFTypeField[] prot, AIFTypeField[] priv, AIFTypeField[] hid) {
		this.publicFields = pub;
		this.protectedFields = prot;
		this.privateFields = priv;
		this.hiddenFields = hid;
	}

	public AIFTypeClass(int id, AIFTypeField[] pub, AIFTypeField[] prot, AIFTypeField[] priv, AIFTypeField[] hid) {
		this(pub, prot, priv, hid);
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public String toString() {
		String res = "{";
		
		if (this.id >= 0)
			res += Integer.toString(this.id) + "|";
		
		for (int i = 0; i < this.publicFields.length; i++) {
			if (i > 0)
				res += ",";
			res += this.publicFields[i].toString();
		}
		
		res += ";";
		
		for (int i = 0; i < this.protectedFields.length; i++) {
			if (i > 0)
				res += ",";
			res += this.protectedFields[i].toString();
		}
		
		res += ";";
		
		for (int i = 0; i < this.privateFields.length; i++) {
			if (i > 0)
				res += ",";
			res += this.privateFields[i].toString();
		}
		
		res += ";";
		
		for (int i = 0; i < this.hiddenFields.length; i++) {
			if (i > 0)
				res += ",";
			res += this.hiddenFields[i].toString();
		}
		
		return res + "}";
	}
	*/
}
