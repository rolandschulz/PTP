/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceCategory;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.internal.core.ServiceConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Note, you need to run this without any other plugins that
 * contribute to the service model.
 *
 */
public class ServiceModelManagerTests {
	
	private static final String
		SERVICE_A = "ServiceA",
		SERVICE_B = "ServiceB";
	
	private static final String
		PROVIDER_A_1 = "ProviderA_1",
		PROVIDER_A_2 = "ProviderA_2",
		PROVIDER_A_3 = "ProviderA_3",
		PROVIDER_B_1 = "ProviderB_1";
	
	private static final String
		CATEGORY_1 = "Category1",
		CATEGORY_2 = "Category2";
	
	
	IProject fProject;
	IServiceConfiguration fConfig;
	IService fService1;
	IService fService2;
	
	void persistAndReplaceModel() throws CoreException, IOException {
		File file = File.createTempFile("serviceModelTest", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$
		
		ServiceModelManager manager = ServiceModelManager.getInstance();
		
		Writer writer = new BufferedWriter(new FileWriter(file));
		try {
			manager.saveModelConfiguration(writer);
		} finally {
			writer.close();
		}
		
		Reader reader = new BufferedReader(new FileReader(file));
		try {
			manager.loadModelConfiguration(reader);
		} finally {
			reader.close();
		}
	}
	
	IServiceProvider addProvider(String providerId, IService service, IServiceConfiguration config) {
		IServiceModelManager manager = ServiceModelManager.getInstance();
		IServiceProviderDescriptor descriptor = service.getProviderDescriptor(providerId);
		IServiceProvider provider = manager.getServiceProvider(descriptor);
		config.setServiceProvider(service, provider);
		return provider;
	}
	
	@Before
	public void setUp() throws Exception {
		fProject = ResourcesPlugin.getWorkspace().getRoot().getProject("testProject");
		fProject.create(new NullProgressMonitor());
		fConfig = ServiceModelManager.getInstance().newServiceConfiguration("myconf"); //$NON-NLS-1$
	}
	
	@After
	public void tearDown() throws Exception {
		fProject.delete(true, new NullProgressMonitor());
	}
	
	
	@Test
	public void testMultipleServices() throws Exception {
		IServiceModelManager manager = ServiceModelManager.getInstance();
		
		fService1 = manager.getService(SERVICE_A); 
		addProvider(PROVIDER_A_1, fService1, fConfig); 
		fService2 = manager.getService(SERVICE_B); 
		addProvider(PROVIDER_B_1, fService2, fConfig); 

		manager.addConfiguration(fProject, fConfig);
		
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
		
		fService1 = manager.getService(SERVICE_A); 
		addProvider(PROVIDER_A_2, fService1, fConfig); 
		manager.addConfiguration(fProject, fConfig);
		
		persistAndReplaceModel();
		
		IServiceConfiguration config = manager.getConfiguration(fProject, fConfig.getName());
		assertNotSame(fConfig, config);
		Set<IService> services = fConfig.getServices();
		assertEquals(1, services.size());
		IService service = services.iterator().next();
		assertServicesEquals(fService1, service, fConfig, config);
	}
	
	@Test
	public void testFormerProviders() throws Exception {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		ServiceConfiguration config = (ServiceConfiguration) smm.newServiceConfiguration("blah");
		
		fService1 = smm.getService(SERVICE_A); 
		IServiceProvider provider1 = addProvider(PROVIDER_A_1, fService1, config); 
		provider1.putString("key1", "val1");
		provider1.putString("key2", "val2");
		
		IServiceProvider provider2 = addProvider(PROVIDER_A_2, fService1, config); 
		provider2.putString("key1", "val1_2");
		
		IServiceProvider provider3 = addProvider(PROVIDER_A_3, fService1, config); 
		provider3.putString("key1", "provider3");
		
		
		persistAndReplaceModel();
		
		
		Set<IServiceProvider> providers = config.getFormerServiceProviders(fService1);
		assertNotNull(providers);
		assertEquals(2, providers.size());
		Iterator<IServiceProvider> iter = providers.iterator();
		
		IServiceProvider disabledProvider = iter.next();
		assertEquals(PROVIDER_A_1, disabledProvider.getId());
		assertEquals("val1", disabledProvider.getString("key1", null));
		assertEquals("val2", disabledProvider.getString("key2", null));
		
		disabledProvider = iter.next();
		assertEquals(PROVIDER_A_2, disabledProvider.getId());
		assertEquals("val1_2", disabledProvider.getString("key1", null));
		
		IServiceProvider provider = config.getServiceProvider(fService1);
		assertEquals(PROVIDER_A_3, provider.getId());
		assertEquals("provider3", provider.getString("key1", null));
		
		
		addProvider(PROVIDER_A_1, fService1, config);
		
		
		providers = config.getFormerServiceProviders(fService1);
		assertNotNull(providers);
		assertEquals(2, providers.size());
		iter = providers.iterator();
		
		disabledProvider = iter.next();
		assertEquals(PROVIDER_A_2, disabledProvider.getId());
		assertEquals("val1_2", disabledProvider.getString("key1", null));
		
		disabledProvider = iter.next();
		assertEquals(PROVIDER_A_3, disabledProvider.getId());
		assertEquals("provider3", disabledProvider.getString("key1", null));
		
		
		config.disable(fService1);
		assertEquals(0, config.getServices().size());
		
		// should still remember the former providers even if the service is disabled
		providers = config.getFormerServiceProviders(fService1);
		assertNotNull(providers);
		assertEquals(3, providers.size());
	}
	
	@Test
	public void testServiceCategories() {
		Set<IServiceCategory> categories = ServiceModelManager.getInstance().getCategories();
		assertNotNull(categories);
		assertEquals(2, categories.size());
	}
	
	@Test
	public void testNullProvider() {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IService serviceA = smm.getService(SERVICE_A);
		IService serviceB = smm.getService(SERVICE_B);
		IServiceConfiguration config = smm.newServiceConfiguration("blah");
		config.setServiceProvider(serviceA, null);
		config.setServiceProvider(serviceB, null);
		
		assertNull(config.getServiceProvider(serviceA));
		assertNotNull(config.getServiceProvider(serviceB));
	}
}


