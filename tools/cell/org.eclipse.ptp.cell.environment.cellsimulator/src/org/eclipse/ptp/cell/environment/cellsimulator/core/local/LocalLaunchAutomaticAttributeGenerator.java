/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.cellsimulator.core.local;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.Parameters;
import org.eclipse.ptp.remotetools.utils.network.MacAddress;

/**
 * @author Richard Maciel
 * @since 3.0
 *
 */
public class LocalLaunchAutomaticAttributeGenerator {
	// Singleton pointer
	static LocalLaunchAutomaticAttributeGenerator autoAttrKeeper = null;
	
	// Maps that associates IDs to generated attribute info
	//Map attributesMap;
	private HashMap portsMap;
	private HashMap addressesMap;
	
	final static int host_offset = 1;
	final static int simulator_offset = 2;
	
	final static int MAC_RADIX = 16; 
	
	// Preferences keys
	public final static String ATTR_BASE_NETWORK = "attribute-generator-base-ip"; //$NON-NLS-1$
	public final static String ATTR_BASE_NETMASK = "attribute-generator-base-netmask";//$NON-NLS-1$
	public final static String ATTR_BASE_MACADDRESS = "attribute-generator-base-macaddress";//$NON-NLS-1$
	public final static String ATTR_MIN_PORTVALUE = "attribute-generator-min-portvalue";//$NON-NLS-1$
	public final static String ATTR_MAX_PORTVALUE = "attribute-generator-max-portvalue";//$NON-NLS-1$
	
	// Min values
	/*static String addrMinValue = "172.0.20.1";
	static int portMinValue;
	static int portMaxValue;
	static String macMinValue = "02:00:00:00:00:01";*/
	
	
	// Actual values
	Inet4Address currentNetworkAddress;
	Inet4Address netmask;
	int currentPortValue;
	int maxPortValue;
	MacAddress currentMacAddress;

	
	/**
	 * 
	 */
	LocalLaunchAutomaticAttributeGenerator() {
		// Get the store from the preferences
		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		
		// Initialize map
		addressesMap = new HashMap();
		portsMap = new HashMap();
		
		// Initialize values from preference store and from default class.
		try {
			currentNetworkAddress = (Inet4Address)Inet4Address.getByName(store.getString(ATTR_BASE_NETWORK));
			netmask = (Inet4Address)Inet4Address.getByName(Parameters.SIMULATOR_NETMASK);
			currentMacAddress = MacAddress.createMacAddress(store.getString(ATTR_BASE_MACADDRESS));
			currentPortValue = store.getInt(ATTR_MIN_PORTVALUE);
			maxPortValue = store.getInt(ATTR_MAX_PORTVALUE);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Return the AutomaticAttributeKeeper, building one, if necessary.
	 * 
	 * @return
	 */
	static public LocalLaunchAutomaticAttributeGenerator getAutomaticAttributeGenerator() {
		if(autoAttrKeeper == null)
			autoAttrKeeper = new LocalLaunchAutomaticAttributeGenerator();
		
		return autoAttrKeeper;
	}
	
	public int getJavaAPIPort(String id) {
		return getPortAttribute(id).apiPort;
	}

	public int getConsolePort(String id) {
		return getPortAttribute(id).consolePort;
	}
	
	public String getHostAddress(String id) {
		return getAddressAttribute(id).hostAddress;
	}
	
	public String getSimulatorAddress(String id) {
		return getAddressAttribute(id).simulatorAddress;
	}
	
	public String getMacAddress(String id) {
		return getAddressAttribute(id).macAddress;
	}
	
	public String getNetmask(String id) {
		return ATTR_BASE_NETMASK;
	}
	
	public int getAnotherJavaAPIPort(String id) {
		int newPort = generatePortValue();
		getPortAttribute(id).apiPort = newPort;
		
		return newPort;
	}
	
	public int getAnotherConsolePort(String id) {
		int newPort = generatePortValue();
		getPortAttribute(id).consolePort = newPort;
		
		return newPort;
	}

	/**
	 * Generate and return a new host/simulator address pair.
	 * 
	 * @return String [] An array containing the host and the simulator address respectively
	 */
	private String [] generateAddressPair() {
		BigInteger addr = new BigInteger(currentNetworkAddress.getAddress());
		BigInteger mask = new BigInteger(netmask.getAddress());
		// get network address
		BigInteger currentNet = addr.and(mask);
		
		// Add one unit to generate the next network address.
		int netindex = mask.getLowestSetBit();
		BigInteger nextNet = currentNet.add(BigInteger.ONE.shiftLeft(netindex));
			
		// Generate the actual host and simulator IP addresses
		BigInteger currentHostAddr = currentNet.add(BigInteger.valueOf(host_offset));
		BigInteger currentSimAddr = currentNet.add(BigInteger.valueOf(simulator_offset));
		
		Inet4Address host = null, sim = null;
		try {
			// Generate the host and simulator addresses from the current network address.
			host = (Inet4Address)Inet4Address.getByAddress(currentHostAddr.toByteArray());
			sim = (Inet4Address)Inet4Address.getByAddress(currentSimAddr.toByteArray());
			
			// Update the current network address
			currentNetworkAddress = (Inet4Address)Inet4Address.getByAddress(nextNet.toByteArray());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		// Return the generated IP addresses
		String [] addrPair = {host.getHostAddress(), sim.getHostAddress()};
		return addrPair;
	}
	
	/**
	 * Generate and return a new Mac Address
	 * 
	 * @return
	 */
	private String generateMacAddress() {
		String macStr = currentMacAddress.getStringRepresentation();
		BigInteger nextMac = currentMacAddress.getBigIntegerRepresentation();
		
		currentMacAddress.setValue(nextMac.add(BigInteger.ONE));
		
		return macStr;
	}
	
	/**
	 * Generate and return a new port value
	 * 
	 */
	private int generatePortValue() {
		boolean validPort = false;
		int chosenPort = currentPortValue;
		if(currentPortValue == -1)
			return currentPortValue;
		else if(chosenPort > maxPortValue) {
			// No port available
			currentPortValue = -1;
			return currentPortValue;
		}
	
		// Check if port is being used. If the answer is yes, then increment currentPortValue and
		// try it
		Socket lockSocket = new Socket();
		InetAddress localAddr = null;
		try {
			localAddr = Inet4Address.getLocalHost();
		} catch (Exception e) {
			// If local address is not available, we're helpless.
			throw new RuntimeException(e);
		}
		do
		{
			SocketAddress sAddr = new InetSocketAddress(localAddr, chosenPort);
			try {
				lockSocket.bind(sAddr);
				lockSocket.close();
				break;
			} catch (IOException e) {}
			chosenPort++;
		} while(!validPort);
		
		// Update currentPort with the next port (not necessarily available)
		currentPortValue = chosenPort + 1;
		
		return chosenPort;
	}
	
	private PortsKeeper getPortAttribute(String id) {
		if(portsMap.containsKey(id)) {
			return (PortsKeeper)portsMap.get(id);
		}
		return generateAndStorePorts(id);
	}
	
	private AddressesKeeper getAddressAttribute(String id) {
		if(addressesMap.containsKey(id)) {
			return (AddressesKeeper)addressesMap.get(id);
		}
		return generateAndStoreAddresses(id);
	}
	
	/**
	 * Create (and store) a new {@link AddressesKeeper} object filled with 
	 * automatically-generated Host address and Simulator address, unless
	 * there is already an object linked to that id. In that case, return 
	 * the stored object.
	 * 
	 * @param id Id information linked to the created (or stored) {@link AddressesKeeper} object.
	 * @return a new (if there isn't a linked {@link AddressesKeeper} to that id) or an old (if already linked) 
	 */
	private synchronized AddressesKeeper generateAndStoreAddresses(String id) {
		String [] addrPair = generateAddressPair();
		String macAddr = generateMacAddress();
		
		AddressesKeeper ak = new AddressesKeeper(addrPair[0], addrPair[1], macAddr);
		
		addressesMap.put(id, ak);
		
		return ak;
	}
	
	/**
	 * Create (and store) a new {@link PortsKeeper} object filled with 
	 * automatically-generated console and simulator ports, unless
	 * there is already an object linked to that id. In that case, return 
	 * the stored object.
	 * 
	 * @param id Id information linked to the created (or stored) {@link PortsKeeper} object.
	 * @return a new (if there isn't a linked {@link PortsKeeper} to that id) or an old (if already linked) 
	 */
	private synchronized PortsKeeper generateAndStorePorts(String id) {
		int apiPort = generatePortValue();
		int consolePort = generatePortValue();

		PortsKeeper ak = new PortsKeeper(consolePort, apiPort);
		
		portsMap.put(id, ak);
		
		return ak;
	}

	/**
	 * Remove the {@link AddressesKeeper} object associated with the given id (if any).
	 * @param id
	 */
	public synchronized void removeAddresses(String id) {
		addressesMap.remove(id);
	}
	
	/**
	 * Remove the {@link PortsKeeper} object associated with the given id (if any).
	 * @param id
	 */
	public synchronized void removePorts(String id) {
		portsMap.remove(id);
	}
}
