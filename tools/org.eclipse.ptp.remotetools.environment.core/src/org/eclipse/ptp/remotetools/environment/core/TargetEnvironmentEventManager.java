/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetControlEventListener;
import org.eclipse.ptp.remotetools.environment.control.ITargetControlEventProvider;
import org.eclipse.ptp.remotetools.environment.control.PoolingTargetControlEventProvider;


/**
 * Manager class to support different event provider per each 
 * target control instance.
 * 
 * @author Ricardo M. Matinata
 * @since 1.2
 *
 */
public class TargetEnvironmentEventManager implements ITargetControlEventListener, ITargetEnvironmentEventListener {
	
	private TargetEnvironmentManager model = null;
	private ITargetControlEventProvider defaultProvider = new PoolingTargetControlEventProvider();
	private Map controls = new HashMap();
	
	/**
	 * Constructor
	 */
	public TargetEnvironmentEventManager(TargetEnvironmentManager model) {
		this.model = model;
		model.addModelChangedListener(this);
	}
	

	public void handleStateChangeEvent(int event, ITargetControl from) {
		
		ControlInfo info = (ControlInfo) controls.get(from);
		model.fireElementEvent(event, info.getElement());
		
	}
	
	public void elementAdded(TargetElement element) {
		
		try {
			ITargetControl control = element.getControl();
			ITargetControlEventProvider provider = null;
			
			if(IAdaptable.class.isAssignableFrom(control.getClass())) {
				provider = (ITargetControlEventProvider) ((IAdaptable)control).getAdapter(ITargetControlEventProvider.class);
			}
			
			if (provider == null) {
				provider = defaultProvider;
			}
			
			controls.put(control, new ControlInfo(element,provider));
			provider.registerControlAndListener(control,this);
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


	public void elementRemoved(ITargetElement element) {
		
		try {
			
			ITargetControl control = element.getControl();
			ControlInfo info = (ControlInfo) controls.get(control);
			ITargetControlEventProvider provider = info.getProvider();
			provider.unregisterControlAndListener(control,this);
			controls.remove(control);
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	class ControlInfo {
		
		private TargetElement element;
		private ITargetControlEventProvider provider;
		
		public ControlInfo(TargetElement element, ITargetControlEventProvider provider) {
			this.element = element;
			this.provider = provider;
		}
		
		public TargetElement getElement() {
			return element;
		}
		
		public void setElement(TargetElement element) {
			this.element = element;
		}
		
		public ITargetControlEventProvider getProvider() {
			return provider;
		}
		
		public void setProvider(ITargetControlEventProvider provider) {
			this.provider = provider;
		}
		
	}

	
}
