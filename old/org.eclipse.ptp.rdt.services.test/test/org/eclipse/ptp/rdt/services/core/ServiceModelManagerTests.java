/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.services.core;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceModelManagerTests {
	IProject fProject;
	ServiceConfiguration fConfig;
	IService fService1;
	IService fService2;
	
	void persistAndReplaceModel() throws CoreException, IOException {
		File file = File.createTempFile("serviceModelTest", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$
		
		ServiceModelManager manager = ServiceModelManager.getInstance();
		manager.saveModelConfiguration(file);
		manager.loadModelConfiguration(file);
	}
	
	void addProvider(String providerId, IService service, IServiceConfiguration config) {
		ServiceModelManager manager = ServiceModelManager.getInstance();
		IServiceProviderDescriptor descriptor = service.getProviderDescriptor(providerId);
		IServiceProvider provider = manager.getServiceProvider(descriptor);
		config.setServiceProvider(service, provider);
	}
	
	@Before
	public void setUp() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		String projectName = "testProject"; //$NON-NLS-1$
		fProject = root.getProject(projectName);
		fConfig = new ServiceConfiguration("myconf"); //$NON-NLS-1$
	}
	
	@Test
	public void testMultipleServices() throws Exception {
		ServiceModelManager manager = ServiceModelManager.getInstance();
		
		fService1 = manager.getService("TestService1"); //$NON-NLS-1$
		addProvider("TestProvider2", fService1, fConfig); //$NON-NLS-1$

		fService2 = manager.getService("TestService2"); //$NON-NLS-1$
		addProvider("TestProvider3", fService2, fConfig); //$NON-NLS-1$

		manager.addConfiguration(fProject, fConfig);
		
		persistAndReplaceModel();
		
		IServiceConfiguration config = manager.getConfiguration(fProject, fConfig.getName());
		Assert.assertNotSame(fConfig, config);
		Set<IService> services = fConfig.getServices();
		Assert.assertEquals(2, services.size());
		for (IService service : services) {
			if (service.getId().equals(fService1.getId())) {
				assertEquals(fService1, service, fConfig, config);
			} else {
				assertEquals(fService2, service, fConfig, config);
			}
		}
	}
	
	private void assertEquals(IService expected, IService actual, IServiceConfiguration expectedConfig, IServiceConfiguration actualConfig) {
		Assert.assertEquals(expected.getId(), actual.getId());
		IServiceProvider provider = expectedConfig.getServiceProvider(actual);
		IServiceProvider provider2 = actualConfig.getServiceProvider(actual);
		Assert.assertEquals(provider.getId(), provider2.getId());
	}

	@Test
	public void testOneProvider() throws Exception {
		ServiceModelManager manager = ServiceModelManager.getInstance();
		
		fService1 = manager.getService("TestService1"); //$NON-NLS-1$
		addProvider("TestProvider2", fService1, fConfig); //$NON-NLS-1$
		manager.addConfiguration(fProject, fConfig);
		
		persistAndReplaceModel();
		
		IServiceConfiguration config = manager.getConfiguration(fProject, fConfig.getName());
		Assert.assertNotSame(fConfig, config);
		Set<IService> services = fConfig.getServices();
		Assert.assertEquals(1, services.size());
		IService service = services.iterator().next();
		assertEquals(fService1, service, fConfig, config);
	}
}
