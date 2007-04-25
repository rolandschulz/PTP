/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.core.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public final class EnumeratedAttributeDefinition extends AbstractAttributeDefinition implements IEnumeratedAttributeDefinition {

	private final ArrayList<String> enumerations = new ArrayList<String>();
	private final int defaultValue;
    final Class<? extends Enum> enumClass;

    public <T extends Enum<T>> EnumeratedAttributeDefinition(final String uniqueId,
            final String name,
            final String description, final Enum<T> defaultValue,
            final Enum<T>[] values) {
        super(uniqueId, name, description);
        this.enumClass = defaultValue.getClass();
        enumerations.addAll(stringArray(values));
        this.defaultValue = defaultValue.ordinal();
    }
    
    public EnumeratedAttributeDefinition(final String uniqueId, final String name,
            final String description, final String defaultValue,
            final String[] values) throws IllegalValueException {
        super(uniqueId, name, description);
        enumerations.addAll(Arrays.asList(values));
        this.enumClass = null;
        if (getEnumerations().indexOf(defaultValue) < 0) {
            throw new IllegalValueException("default enumerated value is not in set");
        }
        this.defaultValue = getEnumerations().indexOf(defaultValue);
    }
    
  	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public IEnumeratedAttribute create() throws IllegalValueException {
		return new EnumeratedAttribute(this, defaultValue);
	}

    public IEnumeratedAttribute create(Enum value) throws IllegalValueException {
        return new EnumeratedAttribute(this, value.ordinal());
    }

    public IEnumeratedAttribute create(int value) throws IllegalValueException {
        return new EnumeratedAttribute(this, value);
    }

    public IEnumeratedAttribute create(String value) throws IllegalValueException {
        return new EnumeratedAttribute(this, value);
    }

	public Class<? extends Enum> getEnumClass() {
        return enumClass;
    }

	public List<String> getEnumerations() {
		return Collections.unmodifiableList(enumerations);
	}

    private Collection<? extends String> stringArray(Enum[] values) {
  	    ArrayList<String> strings = new ArrayList<String>(values.length);
        for (Enum value : values) {
            strings.add(value.toString());
        }
        return strings;
    }
	
}
