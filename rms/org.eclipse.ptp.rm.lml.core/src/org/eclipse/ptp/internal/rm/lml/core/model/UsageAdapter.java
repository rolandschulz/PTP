/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core.model;

import java.math.BigInteger;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.elements.JobType;
import org.eclipse.ptp.rm.lml.core.elements.UsageType;
import org.eclipse.ptp.rm.lml.core.elements.UsagebarType;

/**
 * This class functions as adapter for the classes UsagebarType and UsageType
 * from the core.elements-package. It provides a superset of functions of
 * both classes. This allows to put either a UsagebarType or a UsageType into
 * this adapter and use the superset API without the if-statements controlling
 * which type is inside. UsagebarType and UsageType are similar classes, but differ
 * in their usage. A UsagebarType is a stand-alone graphical component, while a UsageType
 * is only used within nodedisplays. This adapter is needed, because it is not
 * possible to extend both classes from the same super-class.
 * 
 * The class just forwards API calls to the corresponding functions of the adapted
 * instances.
 * 
 * 
 */
public class UsageAdapter {

	/**
	 * One of the adapted instances.
	 * Used as standalone graphical component's model.
	 */
	private UsagebarType usagebar;
	/**
	 * One of the adapted instances.
	 * Used within a nodedisplay for level of detail tree cuts.
	 */
	private UsageType usage;

	/**
	 * Create the adapter for a usagebar.
	 * 
	 * @param usagebar
	 *            adapted instance.
	 */
	public UsageAdapter(UsagebarType usagebar) {
		this.usagebar = usagebar;
	}

	/**
	 * Create the adapter for a usage-instance from inside of a nodedisplay.
	 * 
	 * @param usage
	 *            adapted instance.
	 */
	public UsageAdapter(UsageType usage) {
		this.usage = usage;
	}

	/**
	 * @return amount of cpu covered by this usage-instance
	 */
	public BigInteger getCpuCount() {
		if (usage == null) {
			return usagebar.getCpucount();
		}
		else {
			return usage.getCpucount();
		}
	}

	/**
	 * @return default amount of cpu in each node
	 */
	public BigInteger getCpuPerNode() {
		if (usage == null) {
			return usagebar.getCpupernode();
		}
		else {
			return usage.getCpupernode();
		}
	}

	/**
	 * @return the id of the usagebar if this adapts a UsagebarType, otherwise an empty ID
	 */
	public String getId() {
		if (usage == null) {
			return usagebar.getId();
		}
		else {
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * @return list of jobs shown in the usagebar
	 */
	public List<JobType> getJob() {
		if (usage == null) {
			return usagebar.getJob();
		}
		else {
			return usage.getJob();
		}
	}

	/**
	 * 
	 * @return true, if this adapter is fed by a usagetype without layout definition, false for a usagebartype
	 */
	public boolean isUsageType() {
		return usage != null;
	}

	/**
	 * Set the amount of cpu covered by this usage-instance.
	 * 
	 * @param value
	 *            amount of cpu
	 */
	public void setCpuCount(BigInteger value) {
		if (usage == null) {
			usagebar.setCpucount(value);
		}
		else {
			usage.setCpucount(value);
		}
	}

	/**
	 * Set default amount of cpu in each node.
	 * 
	 * @param value
	 *            default amount of cpu in each node
	 */
	public void setCpuPerNode(BigInteger value) {
		if (usage == null) {
			usagebar.setCpupernode(value);
		}
		else {
			usage.setCpupernode(value);
		}
	}
}
