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
package org.eclipse.ptp.internal.debug.core.pdi.aif;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFormatException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

public class AIFTypeAggregate extends AIFType implements IAIFTypeAggregate {
	private class AIFField {
		public String name;
		public IAIFType type;
	}

	private final List<AIFField> fPrivateFields = new ArrayList<AIFField>();
	private final List<AIFField> fProtectedFields = new ArrayList<AIFField>();
	private final List<AIFField> fPackageFields = new ArrayList<AIFField>();
	private final List<AIFField> fPublicFields = new ArrayList<AIFField>();

	private String fName = ""; //$NON-NLS-1$
	private int fSize = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate#getFieldNames
	 * (int)
	 */
	public String[] getFieldNames(int access) {
		List<AIFField> fields = getFields(access);
		String[] names = new String[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			names[i] = fields.get(i).name;
		}
		return names;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate#getFieldTypes
	 * (int)
	 */
	public IAIFType[] getFieldTypes(int access) {
		List<AIFField> fields = getFields(access);
		IAIFType[] types = new IAIFType[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			types[i] = fields.get(i).type;
		}
		return types;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType#sizeof()
	 */
	public int sizeof() {
		return fSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String content = String.valueOf(AIFFactory.FDS_AGGREGATE);
		content += getName() + AIFFactory.FDS_TYPENAME_END;
		content += getFieldsString(fPublicFields) + AIFFactory.FDS_AGGREGATE_ACCESS_SEP;
		content += getFieldsString(fProtectedFields) + AIFFactory.FDS_AGGREGATE_ACCESS_SEP;
		content += getFieldsString(fPrivateFields) + AIFFactory.FDS_AGGREGATE_ACCESS_SEP;
		content += getFieldsString(fPackageFields) + AIFFactory.FDS_AGGREGATE_END;
		return content;

	}

	private String getFieldsString(List<AIFField> fields) {
		String res = ""; //$NON-NLS-1$

		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				res += AIFFactory.FDS_AGGREGATE_FIELD_SEP;
			}
			AIFField field = fields.get(i);
			res += field.name + AIFFactory.FDS_AGGREGATE_FIELD_NAME_END + field.type.toString();
		}

		return res;
	}

	private List<AIFField> getFields(int access) {
		List<AIFField> fields = new ArrayList<AIFField>();
		if ((access & IAIFTypeAggregate.AIF_CLASS_ACCESS_PUBLIC) == IAIFTypeAggregate.AIF_CLASS_ACCESS_PUBLIC) {
			fields.addAll(fPublicFields);
		}
		if ((access & IAIFTypeAggregate.AIF_CLASS_ACCESS_PROTECTED) == IAIFTypeAggregate.AIF_CLASS_ACCESS_PROTECTED) {
			fields.addAll(fProtectedFields);
		}
		if ((access & IAIFTypeAggregate.AIF_CLASS_ACCESS_PRIVATE) == IAIFTypeAggregate.AIF_CLASS_ACCESS_PRIVATE) {
			fields.addAll(fPrivateFields);
		}
		if ((access & IAIFTypeAggregate.AIF_CLASS_ACCESS_PACKAGE) == IAIFTypeAggregate.AIF_CLASS_ACCESS_PACKAGE) {
			fields.addAll(fPackageFields);
		}
		return fields;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.aif.AIFType#parse(java.lang.String
	 * )
	 */
	@Override
	public String parse(String fmt) throws AIFFormatException {
		fmt = parseName(fmt);
		fmt = parseFields(fmt, fPublicFields);
		fmt = parseFields(fmt, fProtectedFields);
		fmt = parseFields(fmt, fPrivateFields);
		fmt = parseFields(fmt, fPackageFields);
		if (fmt.charAt(0) != AIFFactory.FDS_AGGREGATE_END) {
			throw new AIFFormatException(Messages.AIFTypeAggregate_0);
		}
		return fmt.substring(1);
	}

	private String parseField(String fmt, List<AIFField> fields) throws AIFFormatException {
		AIFField field = new AIFField();

		int pos = fmt.indexOf(AIFFactory.FDS_AGGREGATE_FIELD_NAME_END);
		if (pos == -1) {
			throw new AIFFormatException(Messages.AIFTypeAggregate_1);
		}
		field.name = fmt.substring(0, pos);
		fmt = fmt.substring(pos + 1);

		fmt = AIFFactory.parseType(fmt);

		IAIFType aifType = AIFFactory.getType();
		fSize += aifType.sizeof();
		field.type = aifType;
		fields.add(field);

		return fmt;
	}

	private String parseFields(String fmt, List<AIFField> fields) throws AIFFormatException {
		while (fmt.length() > 0 && fmt.charAt(0) != AIFFactory.FDS_AGGREGATE_ACCESS_SEP
				&& fmt.charAt(0) != AIFFactory.FDS_AGGREGATE_END) {
			fmt = parseField(fmt, fields);
			if (fmt.charAt(0) == AIFFactory.FDS_AGGREGATE_FIELD_SEP) {
				fmt = fmt.substring(1);
			}
		}
		if (fmt.charAt(0) == AIFFactory.FDS_AGGREGATE_ACCESS_SEP) {
			return fmt.substring(1);
		}
		return fmt;
	}

	private String parseName(String fmt) throws AIFFormatException {
		int pos = fmt.indexOf(AIFFactory.FDS_TYPENAME_END);
		if (pos == -1) {
			throw new AIFFormatException(Messages.AIFTypeAggregate_2);
		}
		fName = fmt.substring(0, pos);
		return fmt.substring(pos + 1);
	}
}
