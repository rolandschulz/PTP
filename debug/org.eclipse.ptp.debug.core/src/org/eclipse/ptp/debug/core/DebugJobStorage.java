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
package org.eclipse.ptp.debug.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Clement chu
 */
public class DebugJobStorage {
	private static Map storages = new HashMap();
	
	private String name;
	private Map jobMap;
	
	public DebugJobStorage(String name) {
		this.name = name;
		jobMap = new HashMap();
		addDebugStorage(name, this);
	}
	public void closeDebugJobStorage() {
		for (Iterator i=getJobValueIterator(); i.hasNext();) {
			((Storage)i.next()).clean();
		}
		jobMap.clear();
		removeDebugStorage(name);
	}
	private Storage getJobStorage(String job_id) {
		synchronized (jobMap) {
			if (!jobMap.containsKey(job_id)) {
				jobMap.put(job_id, new Storage());
			}
			return (Storage)jobMap.get(job_id);
		}
	}
	public void removeJobStorage(String job_id) {
		synchronized (jobMap) {
			Storage storage = (Storage)jobMap.remove(job_id);
			if (storage != null) {
				storage.clean();
			}
		}
	}
	public Iterator getJobValueIterator() {
		synchronized (jobMap) {
			List values = new ArrayList();
			for (Iterator i=jobMap.values().iterator(); i.hasNext();) {
				values.addAll(((Storage)i.next()).getValues());
			}
			return values.iterator();
		}
	}
	public Iterator getValueIterator(String job_id) {
		synchronized (jobMap) {
			return getJobStorage(job_id).getValueIterator();
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
	
	private class Storage {
		Map aMap = new HashMap();
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
		Collection getValues() {
			synchronized (aMap) {
				return aMap.values();
			}
		}
		Iterator getValueIterator() {
			synchronized (aMap) {
				return aMap.values().iterator();
			}
		}
		Iterator getKeyIterator() {
			synchronized (aMap) {
				return aMap.keySet().iterator();
			}
		}
	}
	/***************************
	 * static functions
	 ***************************/
	public static DebugJobStorage getDebugStorage(String name) {
		synchronized (storages) {
			return (DebugJobStorage)storages.get(name);
		}
	}
	public static void addDebugStorage(String name, DebugJobStorage storage) {
		synchronized (storages) {
			storages.put(name, storage);
		}
	}
	public static void removeDebugStorage(String name) {
		synchronized (storages) {
			DebugJobStorage storage = (DebugJobStorage)storages.remove(name);
			if (storage != null) {
				storage.closeDebugJobStorage();
			}
		}
	}
	public static void removeDebugStorages() {
		synchronized (storages) {
			for (Iterator i=storages.values().iterator(); i.hasNext();) {
				((DebugJobStorage)i.next()).closeDebugJobStorage();
			}
		}
		storages.clear();
	}
}

