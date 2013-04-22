/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.core.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rdt.sync.core.messages.messages"; //$NON-NLS-1$

	public static String AbstractSynchronizeService_Change_remote_connection;

	public static String AbstractSynchronizeService_Change_remote_location;

	public static String BCM_LocalConnectionError;

	public static String BCM_LocalServiceError;

	public static String SyncUtils_Unable_to_flush;

	public static String PathResourceMatcher_0;

	public static String RegexResourceMatcher_0;

	public static String RemoteContentProvider_0;

	public static String RemoteContentProvider_1;

	public static String ResourceMatcher_0;

	public static String ResourceMatcher_1;

	public static String ResourceMatcher_2;

	public static String ResourceMatcher_3;

	public static String WildcardResourceMatcher_Wildcard_pattern_not_found_in_preference_node;

	public static String SyncConfigManager_Unable_to_save;

	public static String SyncFileFilter_1;

	public static String SyncFileFilter_2;

	public static String SynchronizeServiceDescriptor_Invalid_class;

	public static String SyncManager_10;

	public static String SyncManager_3;

	public static String SyncManager_4;

	public static String SyncManager_6;

	public static String SyncManager_8;

	static {
		initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
