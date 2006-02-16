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
import org.eclipse.ptp.debug.core.aif.ITypeAggregate;

/**
 * @author Clement chu
 * 
 */
public abstract class TypeAggregate extends AIFType implements ITypeAggregate {
	protected List fields = new ArrayList(); 
	protected List types = new ArrayList();
	protected String name;
	private int size = 0;

	public TypeAggregate(String format) {
		parse(format);
	}
	public String getName() {
		return name;
	}
	public String[] getFields() {
		return (String[])fields.toArray(new String[0]);
	}
	public IAIFType[] getTypes() {
		return (IAIFType[])types.toArray(new IAIFType[0]);
	}
	public String getField(int index) {
		return (String)fields.get(index);
	}
	public IAIFType getType(int index) {
		return (IAIFType)types.get(index);
	}
	public int getNumberOfChildren() {
		return fields.size();
	}
	public int sizeof() {
		return size;
	}
	protected void parse(String fmt) {
		fmt = parseName(fmt);
		while (fmt.length() > 0) {
			fmt = parseField(fmt);
			fmt = parseType(fmt);
		}
	}
	
	protected String parseName(String fmt) {
		int pos = fmt.indexOf(AIFFactory.SIGN_STROKE);
		name = fmt.substring(0, pos);
		return fmt.substring(pos+1);
	}
	protected String parseField(String fmt) {
		int pos = fmt.indexOf(AIFFactory.SIGN_EQUAL);
		fields.add(fmt.substring(0, pos));
		return fmt.substring(pos+1);
	}
	protected String parseType(String fmt) {
		int pos = getSeperatePosition(fmt);
		if (pos > -1) {
			String tmp_fmt = fmt.substring(0, pos);
			if (tmp_fmt.indexOf(AIFFactory.FDS_STRUCT_CLASS) > -1) {
				pos = getSeperatePosition(fmt, AIFFactory.FDS_STRUCT_END);
				if (pos == -1) {
					pos = getSeperatePosition(fmt, AIFFactory.FDS_CLASS_END);
				}
			}
			else if (tmp_fmt.indexOf(AIFFactory.FDS_UNION) > -1) {
				pos = getSeperatePosition(fmt, AIFFactory.FDS_UNION_END);
			}
		}

		if (pos == -1) {
			pos = fmt.length();
		}
		IAIFType aifType = AIFFactory.getAIFType(fmt.substring(0, pos));
		size += aifType.sizeof();
		types.add(aifType);
		
		if (pos == fmt.length())
			return "";

		return fmt.substring(pos+1);
	}

	protected int getSeperatePosition(String fmt) {
		return getSeperatePosition(fmt, "");
	}
	protected int getSeperatePosition(String fmt, String regex) {
		int regex_len = regex.length();		
		int pos = fmt.indexOf(regex+AIFFactory.SIGN_COMMA);
		if (pos == -1) {
			if (regex_len > 0)
				pos = fmt.indexOf(regex);
		}
		if (pos > -1) {
			pos += regex_len;
		}
		return pos;
	}
	
	public String toString() {
		String content = getName() + AIFFactory.SIGN_STROKE;
		for (int i=0; i<fields.size(); i++) {
			content += (String)fields.get(i) + "=" + ((IAIFType)types.get(i)).toString();
			if (i < fields.size()-1) {
				content += ",";
			}
		}
		return content;
	}
}
