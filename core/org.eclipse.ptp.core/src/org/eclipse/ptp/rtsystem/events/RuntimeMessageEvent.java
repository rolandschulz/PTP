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

package org.eclipse.ptp.rtsystem.events;

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.attributes.MessageAttributes;
import org.eclipse.ptp.core.elements.attributes.MessageAttributes.Level;


public class RuntimeMessageEvent implements IRuntimeMessageEvent {

	AttributeManager attributes;
	
	public RuntimeMessageEvent(AttributeManager attrs) {
		if (attrs.getAttribute(MessageAttributes.getLevelAttributeDefinition()) == null) {
			attrs.addAttribute(MessageAttributes.getLevelAttributeDefinition().create());
		}
		if (attrs.getAttribute(MessageAttributes.getCodeAttributeDefinition()) == null) {
			try {
				attrs.addAttribute(MessageAttributes.getCodeAttributeDefinition().create());
			} catch (IllegalValueException e) {
			}
		}
		if (attrs.getAttribute(MessageAttributes.getTextAttributeDefinition()) == null) {
			attrs.addAttribute(MessageAttributes.getTextAttributeDefinition().create());
		}
		this.attributes = attrs;
	}

	public RuntimeMessageEvent(Level level, String text) {
		AttributeManager attrs = new AttributeManager();
		attrs.addAttribute(MessageAttributes.getLevelAttributeDefinition().create(level));
		attrs.addAttribute(MessageAttributes.getTextAttributeDefinition().create(text));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeErrorEvent#getLevel()
	 */
	public MessageAttributes.Level getLevel() {
		EnumeratedAttribute<Level> level = 
			attributes.getAttribute(MessageAttributes.getLevelAttributeDefinition());
		return level.getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeErrorEvent#getCode()
	 */
	public int getCode() {
		IntegerAttribute code = 
			attributes.getAttribute(MessageAttributes.getCodeAttributeDefinition());
		return code.getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeErrorEvent#getText()
	 */
	public String getText() {
		StringAttribute text = 
			attributes.getAttribute(MessageAttributes.getTextAttributeDefinition());
		return text.getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeMessageEvent#getAttributes()
	 */
	public AttributeManager getAttributes() {
		return attributes;
	}

}
