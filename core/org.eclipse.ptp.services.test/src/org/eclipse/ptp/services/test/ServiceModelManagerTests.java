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
package org.eclipse.ptp.services.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.junit.After;
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
		
		Writer writer = new BufferedWriter(new FileWriter(file));
		try {
			manager.saveModelConfiguration(fProject, writer);
		} finally {
			writer.close();
		}
		
		Reader reader = new BufferedReader(new FileReader(file));
		try {
			manager.loadModelConfiguration(fProject, reader);
		} finally {
			reader.close();
		}
	}
	
	void addProvider(String providerId, IService service, IServiceConfiguration config) {
		ServiceModelManager manager = ServiceModelManager.getInstance();
		IServiceProviderDescriptor descriptor = service.getProviderDescriptor(providerId);
		IServiceProvider provider = manager.getServiceProvider(descriptor);
		config.setServiceProvider(service, provider);
	}
	
	@Before
	public void setUp() throws Exception {
		fProject = ResourcesPlugin.getWorkspace().getRoot().getProject("testProject");
		fProject.create(new NullProgressMonitor());
		fConfig = new ServiceConfiguration("myconf"); //$NON-NLS-1$
	}
	
	@After
	public void tearDown() throws Exception {
		fProject.delete(true, new NullProgressMonitor());
	}
	
	
	@Test
	public void testMultipleServices() throws Exception {
		IServiceModelManager manager = ServiceModelManager.getInstance();
		
		fService1 = manager.getService("TestService1"); //$NON-NLS-1$
		addProvider("TestProvider2", fService1, fConfig); //$NON-NLS-1$

		fService2 = manager.getService("TestService2"); //$NON-NLS-1$
		addProvider("TestProvider3", fService2, fConfig); //$NON-NLS-1$

		manager.putConfiguration(fProject, fConfig);
		
		persistAndReplaceModel();
		
		IServiceConfiguration config = manager.getConfiguration(fProject, fConfig.getName());
		assertNotSame(fConfig, config);
		Set<IService> services = fConfig.getServices();
		assertEquals(2, services.size());
		for (IService service : services) {
			if (service.getId().equals(fService1.getId())) {
				assertServicesEquals(fService1, service, fConfig, config);
			} else {
				assertServicesEquals(fService2, service, fConfig, config);
			}
		}
	}
	
	private static void assertServicesEquals(IService expected, IService actual, IServiceConfiguration expectedConfig, IServiceConfiguration actualConfig) {
		assertEquals(expected.getId(), actual.getId());
		IServiceProvider provider = expectedConfig.getServiceProvider(actual);
		IServiceProvider provider2 = actualConfig.getServiceProvider(actual);
		assertEquals(provider.getId(), provider2.getId());
	}

	@Test
	public void testOneProvider() throws Exception {
		IServiceModelManager manager = ServiceModelManager.getInstance();
		
		fService1 = manager.getService("TestService1"); //$NON-NLS-1$
		addProvider("TestProvider2", fService1, fConfig); //$NON-NLS-1$
		manager.putConfiguration(fProject, fConfig);
		
		persistAndReplaceModel();
		
		IServiceConfiguration config = manager.getConfiguration(fProject, fConfig.getName());
		assertNotSame(fConfig, config);
		Set<IService> services = fConfig.getServices();
		assertEquals(1, services.size());
		IService service = services.iterator().next();
		assertServicesEquals(fService1, service, fConfig, config);
	}
}
