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
package org.eclipse.ptp.internal.debug.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement chu
 */
public class DebugJobStorage {
	private static Map<String, DebugJobStorage> storages = new HashMap<String, DebugJobStorage>();

	private final String name;
	private final Map<String, Storage> jobMap;

	public DebugJobStorage(String name) {
		jobMap = new HashMap<String, Storage>();
		if (containsKey(name)) {
			throw new IllegalArgumentException(NLS.bind(Messages.DebugJobStorage_0, name));
		}

		this.name = name;
		addDebugStorage(this.name, this);
	}

	public void closeDebugJobStorage() {
		final Storage[] storages = jobMap.values().toArray(new Storage[0]);
		for (final Storage st : storages) {
			st.clean();
		}
		jobMap.clear();
		removeDebugStorage(name);
	}

	private Storage getJobStorage(String job_id) {
		synchronized (jobMap) {
			if (!jobMap.containsKey(job_id)) {
				jobMap.put(job_id, new Storage());
			}
			return jobMap.get(job_id);
		}
	}

	public void removeJobStorage(String job_id) {
		synchronized (jobMap) {
			final Storage storage = jobMap.remove(job_id);
			if (storage != null) {
				storage.clean();
			}
		}
	}

	public Collection<Object> getJobValueCollection() {
		synchronized (jobMap) {
			final List<Object> values = new ArrayList<Object>();
			for (final Storage st : jobMap.values()) {
				values.addAll(st.getValueCollection());
			}
			return values;
		}
	}

	public Collection<Object> getValueCollection(String job_id) {
		synchronized (jobMap) {
			return getJobStorage(job_id).getValueCollection();
		}
	}

	public Object getValue(String job_id, String key) {
		synchronized (jobMap) {
			return getJobStorage(job_id).getStore(key);
		}
	}

	public void addValue(String job_id, String key, Object value) {
		synchronized (jobMap) {
			getJobStorage(job_id).addStore(key, value);
		}
	}

	public Object removeValue(String job_id, String key) {
		synchronized (jobMap) {
			return getJobStorage(job_id).removeStore(key);
		}
	}

	/***********
	 * Storage *
	 ***********/
	class Storage {
		Map<String, Object> aMap = new HashMap<String, Object>();

		void clean() {
			aMap.clear();
		}

		boolean containsStore(String key) {
			synchronized (aMap) {
				return aMap.containsKey(key);
			}
		}

		void addStore(String key, Object value) {
			synchronized (aMap) {
				aMap.put(key, value);
			}
		}

		Object removeStore(String key) {
			synchronized (aMap) {
				return aMap.remove(key);
			}
		}

		Object getStore(String key) {
			synchronized (aMap) {
				return aMap.get(key);
			}
		}

		Collection<Object> getValueCollection() {
			synchronized (aMap) {
				return aMap.values();
			}
		}

		String[] getKeys() {
			synchronized (aMap) {
				return aMap.keySet().toArray(new String[0]);
			}
		}
	}

	/***************************
	 * static functions
	 ***************************/
	public static boolean containsKey(String name) {
		synchronized (storages) {
			return storages.containsKey(name);
		}
	}

	public static DebugJobStorage getDebugStorage(String name) {
		synchronized (storages) {
			return storages.get(name);
		}
	}

	public static void addDebugStorage(String name, DebugJobStorage storage) {
		synchronized (storages) {
			storages.put(name, storage);
		}
	}

	public static DebugJobStorage[] getDebugStorages() {
		synchronized (storages) {
			return storages.values().toArray(new DebugJobStorage[0]);
		}
	}

	public static void removeDebugStorage(String name) {
		synchronized (storages) {
			final DebugJobStorage storage = storages.remove(name);
			if (storage != null) {
				storage.closeDebugJobStorage();
			}
		}
	}

	public static void removeDebugStorages() {
		synchronized (storages) {
			final DebugJobStorage[] dStoreages = storages.values().toArray(new DebugJobStorage[0]);
			for (final DebugJobStorage dStoreage : dStoreages) {
				dStoreage.closeDebugJobStorage();
			}
			storages.clear();
		}
	}
}
