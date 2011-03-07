package org.eclipse.ptp.rm.jaxb.core.data.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Target;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class TargetImpl implements IJAXBNonNLSConstants {

	private final String ref;
	private final Integer fromId;
	private final String type;
	private Object target;

	public TargetImpl(Target target) {
		ref = target.getRef();
		type = target.getType();
		fromId = target.getIdFrom();
	}

	public void clear() {
		/*
		 * Store target if anonymous, which should have a name by now
		 */
		if (ref == null) {
			if (fromId == null && target != null) {
				RMVariableMap vmap = RMVariableMap.getActiveInstance();
				if (PROPERTY.equals(type)) {
					vmap.getDiscovered().put(((Property) target).getName(), target);
				} else if (ATTRIBUTE.equals(type)) {
					vmap.getDiscovered().put(((JobAttribute) target).getName(), target);
				}
			}
			target = null; // only if not a reference
		}
	}

	public Object getTarget(String[] tokens) throws CoreException {
		if (target != null) {
			return target;
		}
		RMVariableMap vmap = RMVariableMap.getActiveInstance();
		if (ref != null) {
			String name = vmap.getString(ref);
			target = vmap.getVariables().get(name);
			if (target == null) {
				throw CoreExceptionUtils.newException(Messages.StreamParserNoSuchVariableError + ref, null);
			}
		} else {
			String name = null;
			if (fromId != null) {
				if (tokens != null) {
					name = tokens[fromId];
				}
				if (name != null) {
					target = vmap.getDiscovered().get(name);
				}
			}

			if (target == null) {
				if (PROPERTY.equals(type)) {
					Property p = new Property();
					if (name != null) {
						p.setName(name);
						vmap.getDiscovered().put(name, p);
					}
					target = p;
				} else if (ATTRIBUTE.equals(type)) {
					JobAttribute ja = new JobAttribute();
					if (name != null) {
						ja.setName(name);
						vmap.getDiscovered().put(name, ja);
					}
					target = ja;
				} else {
					throw CoreExceptionUtils.newException(Messages.StreamParserMissingTargetType + ref, null);
				}
			}
		}
		return target;
	}
}
