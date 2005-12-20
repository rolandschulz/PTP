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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeEnum;

public class AIFTypeEnum extends AIFTypeIntegral implements IAIFTypeEnum {
	private List fields = new ArrayList();
	private List values = new ArrayList();
	private IAIFType basetype;
	private String name;
	
	//<ID|S1=V1,S2=V2,...>is4
	public AIFTypeEnum(String format, IAIFType basetype) {
		this(format, true, basetype);
	}
	public AIFTypeEnum(String format, boolean signed, IAIFType basetype) {
		super(signed);
		this.basetype = basetype;
		parse(format);
	}
	public IAIFType getBaseType() {
		return basetype;
	}
	public int sizeof() {
		return getNumberOfChildren() * basetype.sizeof();
	}
	
	protected void parse(String fmt) {
		fmt = parseName(fmt);
		String[] pairs = fmt.split(AIFFactory.SIGN_COMMA);
		for (int i=0; i<pairs.length; i++) {
			String[] results = pairs[i].split(AIFFactory.SIGN_EQUAL);
			fields.add(results[0]);
			values.add(results[1]);
		}
	}
	protected String parseName(String fmt) {
		int pos = fmt.indexOf(AIFFactory.SIGN_STROKE);
		name = fmt.substring(0, pos);
		int last_pos = fmt.lastIndexOf(AIFFactory.FDS_ENUM_END);
		return fmt.substring(pos+1, last_pos);
	}
	
	public String getName() {
		return name;
	}
	public String[] getFields() {
		return (String[])fields.toArray(new String[0]);
	}
	public String[] getTypes() {
		return (String[])values.toArray(new String[0]);
	}
	public String getField(int index) {
		return (String)fields.get(index);
	}
	public String getValue(int index) {
		return (String)values.get(index);
	}
	public int getNumberOfChildren() {
		return fields.size();
	}
	
	public String toString() {
		String content = "<" + getName() + AIFFactory.SIGN_STROKE;
		for (int i=0; i<fields.size(); i++) {
			content += (String)fields.get(i) + "=" + (String)values.get(i);
			if (i < fields.size()-1) {
				content += ",";
			}
		}
		return content + ">" + super.toString();		
	}
}
