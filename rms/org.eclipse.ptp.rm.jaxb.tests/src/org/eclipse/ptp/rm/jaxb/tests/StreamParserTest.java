/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.internal.rm.jaxb.control.core.IStreamParserTokenizer;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBRMPreferenceConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.command.ConfigurableRegexTokenizer;
import org.eclipse.ptp.internal.rm.jaxb.control.core.variables.RMVariableMap;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.TokenizerType;

public class StreamParserTest extends TestCase {

	protected class Range {

		private final String expression;
		private int[] from;
		private int[] to;
		private int len;

		public Range(String expression) {
			assert (null != rmVarMap);
			this.expression = rmVarMap.getString(uuid, expression);
		}

		public List<Object> findInRange(String[] values) {
			List<Object> found = new ArrayList<Object>();
			for (int i = 0; i < from.length; i++) {
				if (from[i] == to[i]) {
					found.add(values[from[i]]);
				} else {
					for (int j = from[i]; j < to[i]; j++) {
						found.add(values[j]);
					}
				}
			}
			return found;
		}

		public boolean isInRange(int line) {
			for (int i = 0; i < from.length; i++) {
				if ((from[i] == line && line <= to[i]) || (from[i] < line && line < to[i])) {
					return true;
				}
			}
			return false;
		}

		public void setLen(int len) {
			this.len = len;
			parse(expression);
		}

		private int maybeInterpretLength(String n) {
			int i = -1;
			if (n.indexOf(JAXBControlConstants.LEN) >= 0) {
				String[] lenExp = n.split(JAXBControlConstants.HYPH);
				if (lenExp.length == 2) {
					i = len - Integer.parseInt(lenExp[1]);
				} else {
					i = len;
				}
			} else {
				i = Integer.parseInt(n.trim());
			}
			return i;
		}

		private void parse(String expression) {
			List<String> from = new ArrayList<String>();
			List<String> to = new ArrayList<String>();
			String[] commas = expression.split(JAXBControlConstants.CM);
			for (String comma : commas) {
				String[] colon = comma.split(JAXBControlConstants.CO);
				if (colon.length == 2) {
					from.add(colon[0]);
					to.add(colon[1]);
				} else {
					from.add(colon[0]);
					to.add(colon[0]);
				}
			}

			this.from = new int[from.size()];
			this.to = new int[from.size()];

			for (int i = 0; i < this.from.length; i++) {
				this.from[i] = maybeInterpretLength(from.get(i).trim());
				this.to[i] = maybeInterpretLength(to.get(i).trim());
			}
		}
	}

	private static final String tokxml = JAXBControlConstants.DATA + "tokenizer-examples.xml"; //$NON-NLS-1$

	private static InputStream getImplicitOrdering() {
		String content = "jobAttribute_1" + JAXBControlConstants.LINE_SEP + "java.lang.String" + JAXBControlConstants.LINE_SEP + JAXBControlConstants.LINE_SEP + JAXBControlConstants.LINE_SEP + "value_1" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "jobAttribute_2" + JAXBControlConstants.LINE_SEP + "java.lang.String" + JAXBControlConstants.LINE_SEP + "meaingless attribute" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "ignore this attribute" + JAXBControlConstants.LINE_SEP + "value_2" //$NON-NLS-1$ //$NON-NLS-2$
				+ JAXBControlConstants.LINE_SEP;
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getImplicitWithTags() {
		String content = "JAXBRMConstants.PROPERTY" + JAXBControlConstants.LINE_SEP + "value:423.4" + JAXBControlConstants.LINE_SEP + "name:x" + JAXBControlConstants.LINE_SEP + "JAXBRMConstants.ATTRIBUTE" + JAXBControlConstants.LINE_SEP + "name:y" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ JAXBControlConstants.LINE_SEP
				+ "value:-130.42" + JAXBControlConstants.LINE_SEP + "JAXBRMConstants.PROPERTY" + JAXBControlConstants.LINE_SEP + "name:z" + JAXBControlConstants.LINE_SEP + "value:-1.7" + JAXBControlConstants.LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getJobStates() {
		String content = "blah blah xxxx blah blah xxxx blah blah xxxx blah blah xx" //$NON-NLS-1$
				+ "<job>304823:RUNNING</job>fooblah blah xxxx\n  blah blah xxxx blah blah xxxx blah " //$NON-NLS-1$
				+ " blah x\nx<job>312042:DONE</job>blah xxxx blah blah xxxx blah b" //$NON-NLS-1$
				+ "blah blah xxxx foobarfoobr 231028388 <job>338831:SUJAXBRMConstants.SPENDED" //$NON-NLS-1$
				+ "</job>fooroiqEXIT\npoiewmr<job>318388:QUEUED</job>blah blah xxxx"; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getMergedOrdering() {
		String content = "name:foo0;value:bar0" + JAXBControlConstants.LINE_SEP + "name:foo1;value:bar1" + JAXBControlConstants.LINE_SEP + "name:foo0;default:baz0" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ JAXBControlConstants.LINE_SEP;
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getNoiseBeforeJobId() {
		String content = "abe and lincoln will be down for 3 hours Saturday May 14, 2013 from 00:00:00 to 03:00:00" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$
				+ "There is currently no way to know what your shell is.  Please look in /etc/passwd" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$
				+ "To get your default account, click your heels three times and say, 'There's no place like home!'" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$
				+ "429324.honest1" + JAXBControlConstants.LINE_SEP; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getOpenMPIOut() {
		String content = "ompi:version:full:1.8" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mca:base:param:mca_component_disable_dlopen:status:writable" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mca:base:param:mca_component_disable_dlopen:help:Whether to attempt to disable opening dynamic components or not" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mca:base:param:mca_component_disable_dlopen:deprecated:no" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:value:1" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:data_source:default value" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:status:read-only" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:help:Whether you want MPI API parameters checked at run-time or not.  Possible values are 0 (no checking) and 1 (perform checking at run-time)" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:deprecated:no" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:value:-1" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:data_source:default value" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:status:writable" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:help:Yield the processor when waiting for MPI communication (for MPI processes, will default to 1 when oversubscribing nodes)" //$NON-NLS-1$
				+ JAXBControlConstants.LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:deprecated:no" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$
				+ "mca:mpi:base:param:mpi_event_tick_rate:value:-1" + JAXBControlConstants.LINE_SEP; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getPropertyDefs() {
		String content = "<name>pnameA</name><value>pvalueA</value>" + JAXBControlConstants.LINE_SEP + "<name>pnameB</name><value>pvalueB</value>" //$NON-NLS-1$ //$NON-NLS-2$
				+ JAXBControlConstants.LINE_SEP
				+ "<name>pnameC</name><value>pvalueC</value>" + JAXBControlConstants.LINE_SEP + "<name>pnameD</name><value>pvalueD</value>" //$NON-NLS-1$ //$NON-NLS-2$
				+ JAXBControlConstants.LINE_SEP + "<value>pvalueW</value><name>pnameW</name>" + JAXBControlConstants.LINE_SEP; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getQstat() {
		String content = "42226.ember       g_zn_ph2         enoey             665:51:4 E normal  \n";//$NON-NLS-1$ 
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getQstatOut() {
		String content = "normal" + JAXBControlConstants.LINE_SEP + "iacat2" + JAXBControlConstants.LINE_SEP + "indprio" + JAXBControlConstants.LINE_SEP + "lincoln_nomss" + JAXBControlConstants.LINE_SEP + "cap1" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ JAXBControlConstants.LINE_SEP
				+ "lincoln_debug" + JAXBControlConstants.LINE_SEP + "long" + JAXBControlConstants.LINE_SEP + "iacat" + JAXBControlConstants.LINE_SEP + "industrial" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ "lincoln" + JAXBControlConstants.LINE_SEP + "wide" + JAXBControlConstants.LINE_SEP + "nomss" + JAXBControlConstants.LINE_SEP + "debug" + JAXBControlConstants.LINE_SEP + "iacat3" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ "lincoln_cuda3.2" + JAXBControlConstants.LINE_SEP + "fernsler" + JAXBControlConstants.LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getStaggered() {
		String content = "value:1" + JAXBControlConstants.LINE_SEP + "value:2" + JAXBControlConstants.LINE_SEP + "name:1" + JAXBControlConstants.LINE_SEP + "name:2" + JAXBControlConstants.LINE_SEP + "name:3" + JAXBControlConstants.LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ "name:4" + JAXBControlConstants.LINE_SEP + "value:3" + JAXBControlConstants.LINE_SEP + "value:4" + JAXBControlConstants.LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return new ByteArrayInputStream(content.getBytes());
	}

	private String uuid;

	private String lineRange;

	private boolean[] oracle;

	private String[] values;

	private final boolean verbose = true;
	private final boolean logging = false;

	private String target;

	private List<CommandType> startup;

	private CommandType getStatus;

	private RMVariableMap rmVarMap;

	@Override
	public void setUp() {
		rmVarMap = new RMVariableMap();
		String name = getName();
		if (name.equals("testRange")) { //$NON-NLS-1$
			// the second number is <, not <=
			lineRange = "3,8,12:15,21, 24:29,40:N-3"; //$NON-NLS-1$
			oracle = new boolean[] { false, false, false, true, false, false, false, false, true, false, false, false, true, true,
					true, false, false, false, false, false, false, true, false, false, true, true, true, true, true, false, false,
					false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, false,
					false, false };
			values = new String[oracle.length];
		} else {
			Preferences.setBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN, logging);
			Preferences.setBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.MATCH_STATUS, logging);
			Preferences.setBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.ACTIONS, logging);
			Preferences.setBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.CREATED_PROPERTIES, logging);
			try {
				JAXBTestsPlugin.validate(tokxml);
				ResourceManagerData rmdata = JAXBInitializationUtils.initializeRMData(JAXBTestsPlugin.getURL(tokxml));
				if (rmdata != null) {
					ControlType cd = rmdata.getControlData();
					startup = cd.getStartUpCommand();
					getStatus = cd.getGetJobStatus();
				}
			} catch (Throwable t) {
				t.printStackTrace();
				fail(t.getMessage());
			}
		}
	}

	@Override
	public void tearDown() {

	}

	public void test00ParseQstat() {
		target = "queues"; //$NON-NLS-1$
		AttributeType p = new AttributeType();
		p.setName(target);
		rmVarMap.getAttributes().put(target, p);
		runTokenizer(startup.get(0).getStdoutParser(), getQstatOut());
		p = rmVarMap.getAttributes().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(target + " = " + p.getValue()); //$NON-NLS-1$
		}
	}

	public void test01JobId() {
		uuid = UUID.randomUUID().toString();
		target = uuid;
		AttributeType p = new AttributeType();
		p.setName(target);
		p.setValue(target);
		rmVarMap.getAttributes().put(target, p);
		runTokenizer(startup.get(1).getStdoutParser(), getNoiseBeforeJobId());
		p = rmVarMap.getAttributes().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(target + " = " + p.getValue()); //$NON-NLS-1$
		}
	}

	public void test02OpenMPI() {
		target = JAXBControlConstants.ATTRIBUTE;
		runTokenizer(startup.get(2).getStdoutParser(), getOpenMPIOut());
		Map<String, AttributeType> d = rmVarMap.getDiscovered();
		for (AttributeType ja : d.values()) {
			if (verbose) {
				System.out.println("DISCOVERED JAXBRMConstants.ATTRIBUTE:"); //$NON-NLS-1$
				System.out.println("name " + ja.getName()); //$NON-NLS-1$
				System.out.println("value " + ja.getValue()); //$NON-NLS-1$
				System.out.println("tooltip " + ja.getTooltip()); //$NON-NLS-1$
				System.out.println("status " + ja.getStatus()); //$NON-NLS-1$
				System.out.println("readOnly " + ja.isReadOnly()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void test03ImplicitWithTags() {
		runTokenizer(startup.get(3).getStdoutParser(), getImplicitWithTags());
		Map<String, AttributeType> d = rmVarMap.getDiscovered();
		for (AttributeType ja : d.values()) {
			if (verbose) {
				System.out.println("DISCOVERED JAXBRMConstants.ATTRIBUTE:"); //$NON-NLS-1$
				System.out.println("name " + ja.getName()); //$NON-NLS-1$
				System.out.println("value " + ja.getValue()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void test04ImplicitOrdering() {
		target = JAXBControlConstants.ATTRIBUTE;
		runTokenizer(startup.get(4).getStdoutParser(), getImplicitOrdering());
		Map<String, AttributeType> d = rmVarMap.getDiscovered();
		for (AttributeType ja : d.values()) {
			if (verbose) {
				System.out.println("DISCOVERED JAXBRMConstants.ATTRIBUTE:"); //$NON-NLS-1$
				System.out.println("name " + ja.getName()); //$NON-NLS-1$
				System.out.println("type " + ja.getType()); //$NON-NLS-1$
				System.out.println("tooltip " + ja.getTooltip()); //$NON-NLS-1$
				System.out.println("description " + ja.getDescription()); //$NON-NLS-1$
				System.out.println("value " + ja.getValue()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void test05ImplicitWithTagsDotall() {
		runTokenizer(startup.get(5).getStdoutParser(), getImplicitWithTags());
		Map<String, AttributeType> d = rmVarMap.getDiscovered();
		for (AttributeType ja : d.values()) {
			if (verbose) {
				System.out.println("DISCOVERED JAXBRMConstants.ATTRIBUTE:"); //$NON-NLS-1$
				System.out.println("name " + ja.getName()); //$NON-NLS-1$
				System.out.println("value " + ja.getValue()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void test06PropertyDefsSingleLine() {
		target = JAXBControlConstants.PROPERTY;
		runTokenizer(startup.get(6).getStdoutParser(), getPropertyDefs());
		Map<String, AttributeType> d = rmVarMap.getDiscovered();
		for (AttributeType p : d.values()) {
			if (verbose) {
				System.out.println("DISCOVERED JAXBRMConstants.PROPERTY:"); //$NON-NLS-1$
				System.out.println("name " + p.getName()); //$NON-NLS-1$
				System.out.println("value " + p.getValue()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void test07JobStates() {
		target = "jobStates"; //$NON-NLS-1$
		AttributeType p = new AttributeType();
		p.setName(target);
		rmVarMap.getAttributes().put(target, p);
		runTokenizer(startup.get(7).getStdoutParser(), getJobStates());
		p = rmVarMap.getAttributes().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(target + " = " + p.getValue()); //$NON-NLS-1$
		}
	}

	public void test08Staggered() {
		runTokenizer(startup.get(8).getStdoutParser(), getStaggered());
		Map<String, AttributeType> d = rmVarMap.getDiscovered();
		for (AttributeType ja : d.values()) {
			if (verbose) {
				System.out.println("DISCOVERED JAXBRMConstants.ATTRIBUTE:"); //$NON-NLS-1$
				System.out.println("name " + ja.getName()); //$NON-NLS-1$
				System.out.println("value " + ja.getValue()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void test09Merged() {
		target = JAXBControlConstants.PROPERTY;
		runTokenizer(startup.get(9).getStdoutParser(), getMergedOrdering());
		Map<String, AttributeType> d = rmVarMap.getDiscovered();
		for (AttributeType p : d.values()) {
			if (verbose) {
				System.out.println("DISCOVERED JAXBRMConstants.PROPERTY:"); //$NON-NLS-1$
				System.out.println("name " + p.getName()); //$NON-NLS-1$
				System.out.println("value " + p.getValue()); //$NON-NLS-1$
				System.out.println("default " + p.getDefault()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void test10ExitOn() {
		target = "jobStates"; //$NON-NLS-1$
		AttributeType p = new AttributeType();
		p.setName(target);
		rmVarMap.getAttributes().put(target, p);
		runTokenizer(startup.get(10).getStdoutParser(), getJobStates());
		p = rmVarMap.getAttributes().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(target + " = " + p.getValue()); //$NON-NLS-1$
		}
	}

	public void test11ExitAfter() {
		target = "jobStates"; //$NON-NLS-1$
		AttributeType p = new AttributeType();
		p.setName(target);
		rmVarMap.getAttributes().put(target, p);
		runTokenizer(startup.get(11).getStdoutParser(), getJobStates());
		p = rmVarMap.getAttributes().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(target + " = " + p.getValue()); //$NON-NLS-1$
		}
	}

	public void test12GetStatus() {
		target = "42226";//$NON-NLS-1$
		uuid = target;
		AttributeType p = new AttributeType();
		p.setName(target);
		rmVarMap.getAttributes().put(target, p);
		runTokenizer(getStatus.getStdoutParser(), getQstat());
		p = rmVarMap.getAttributes().get(this.target);
		assertNotNull(p);
		System.out.println(p.getName() + JAXBControlConstants.CM + JAXBControlConstants.SP + p.getValue());
	}

	public void testRange() {
		Range range = new Range(lineRange);
		range.setLen(50);
		int trues = 0;

		for (int i = 0; i < 50; i++) {
			boolean b = range.isInRange(i);
			if (oracle[i]) {
				trues++;
			}
			if (verbose) {
				System.out.println(" line " + i + " in range: " + b + ", should be " + oracle[i]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			assertEquals(oracle[i], b);
			values[i] = "" + oracle[i]; //$NON-NLS-1$
		}

		List<Object> valid = range.findInRange(values);
		for (Object o : valid) {
			assertEquals("true", o); //$NON-NLS-1$
			assertEquals(trues, valid.size());
		}
	}

	private void runTokenizer(TokenizerType tokenizer, InputStream stream) {
		IStreamParserTokenizer t = new ConfigurableRegexTokenizer(tokenizer);
		t.initialize(uuid, rmVarMap, null);
		t.setInputStream(stream);
		Thread thr = new Thread(t);
		thr.start();
		try {
			thr.join();
		} catch (InterruptedException ignored) {
		}
		Throwable throwable = t.getInternalError();
		if (throwable != null) {
			throwable.printStackTrace();
		}
		assertNull(throwable);
	}
}
