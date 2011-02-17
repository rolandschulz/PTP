/*******************************************************************************
 * Copyright (c) 2010 University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Albert L. Rossi (NCSA) - design and implementation (bug 310188)
 *     						  - modified 05/11/2010
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.pbs.core.attributes.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplate;

/**
 * Used to populate viewers whose model is the AttributePlaceholder. <br>
 * <br>
 * Assumes top-level PBSBatchScriptTemplate and also maps whose values are
 * AttributePlaceholders.
 * 
 * @see org.eclipse.ptp.rm.pbs.core.attributes.AttributePlaceholder
 * @author arossi
 */
public class AttributeContentProvider implements IStructuredContentProvider {
	public void dispose() {
	}

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof PBSBatchScriptTemplate) {
			PBSBatchScriptTemplate t = (PBSBatchScriptTemplate) inputElement;
			List<AttributePlaceholder> all = new ArrayList<AttributePlaceholder>();
			all.addAll(t.getPbsJobAttributes().values());
			all.addAll(t.getInternalAttributes().values());
			return all.toArray(new AttributePlaceholder[0]);
		} else if (inputElement instanceof Map<?, ?>) {
			Map<?, ?> m = (Map<?, ?>) inputElement;
			Collection<AttributePlaceholder> c = (Collection<AttributePlaceholder>) m.values();
			return c.toArray(new AttributePlaceholder[0]);
		} else if (inputElement instanceof AttributePlaceholder)
			return new Object[] { inputElement };
		return new Object[0];
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
