/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.parameters;

import java.util.HashMap;
import java.util.Map;

public class Parameters implements Cloneable {
	public class Parameter implements Cloneable {
		private String name;
		private String value = "";
		private String help = "";
		private boolean readOnly = false;

		public Parameter(String name) {
			this.name = name;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Parameter clone() throws CloneNotSupportedException {
			Parameter param = new Parameter(name);
			param.setValue(value);
			param.setHelp(help);
			param.setReadOnly(readOnly);
			return param;
		}

		/**
		 * @return the help
		 */
		public String getHelp() {
			return help;
		}

		/**
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @return the readOnly
		 */
		public boolean isReadOnly() {
			return readOnly;
		}

		/**
		 * @param help the help to set
		 */
		public void setHelp(String help) {
			this.help = help;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		/**
		 * @param readOnly the readOnly to set
		 */
		public void setReadOnly(boolean readOnly) {
			this.readOnly = readOnly;
		}
		
		/**
		 * @param value
		 */
		public void setValue(String value) {
			this.value = value;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return getName();
		}
	}

	private Map<String, Parameter> params = new HashMap<String, Parameter>();
	
	/**
	 * @param name
	 */
	public Parameter addParameter(Parameter param) {
		params.put(param.getName(), param);
		return param;
	}
	
	/**
	 * @param name
	 */
	public Parameter addParameter(String name) {
		Parameter param = new Parameter(name);
		params.put(name, param);
		return param;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Parameters clone() throws CloneNotSupportedException {
		Parameters newParams = new Parameters();
		for (Parameter param : getParameters()) {
			newParams.addParameter(param.clone());
		}
		return newParams;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public Parameter getParameter(String name) {
		return params.get(name);
	}
	
	/**
	 * @return
	 */
	public int getParameterCount() {
		return params.size();
	}
	
	/**
	 * @return
	 */
	public Parameter[] getParameters() {
		return params.values().toArray(new Parameter[params.size()]);
	}

}
