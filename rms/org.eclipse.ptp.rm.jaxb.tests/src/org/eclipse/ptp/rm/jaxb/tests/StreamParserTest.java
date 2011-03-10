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

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.Append;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Match;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Put;
import org.eclipse.ptp.rm.jaxb.core.data.Read;
import org.eclipse.ptp.rm.jaxb.core.data.Regex;
import org.eclipse.ptp.rm.jaxb.core.data.Set;
import org.eclipse.ptp.rm.jaxb.core.data.Target;
import org.eclipse.ptp.rm.jaxb.core.data.Test;
import org.eclipse.ptp.rm.jaxb.core.data.impl.AbstractRangeAssign;
import org.eclipse.ptp.rm.jaxb.core.runnable.ConfigurableRegexTokenizer;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class StreamParserTest extends TestCase implements IJAXBNonNLSConstants {

	private class TestRangeAssign extends AbstractRangeAssign {

		@Override
		protected Object[] getValue(Object previous, String[] values) {
			return null;
		}

		private List<Object> getValid(String[] values) {
			return range.findInRange(values);
		}

		private void setRange(String exp) {
			range = new Range(exp);
		}

		private void setRLen(int len) {
			range.setLen(len);
		}

		private boolean test(int i) {
			return range.isInRange(i);
		}
	}

	private String uuid;
	private String lineRange;
	private boolean[] oracle;
	private String[] values;
	private final boolean verbose = true;
	private final boolean redirect = false;
	private String target;
	private List<Read> read;

	@Override
	public void setUp() {
		RMVariableMap.setActiveInstance(null);
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
			read = new ArrayList<Read>();
		}
	}

	@Override
	public void tearDown() {

	}

	public void testImplicitOrdering() {
		target = ATTRIBUTE;
		read.add(getRead(getName()));
		runTokenizer(getImplicitOrdering());
		Map<String, Object> d = RMVariableMap.getActiveInstance().getDiscovered();
		for (Object o : d.values()) {
			JobAttribute ja = (JobAttribute) o;
			if (verbose) {
				System.out.println("DISCOVERED ATTRIBUTE:"); //$NON-NLS-1$
				System.out.println("name " + ja.getName()); //$NON-NLS-1$
				System.out.println("type " + ja.getType()); //$NON-NLS-1$
				System.out.println("tooltip " + ja.getTooltip()); //$NON-NLS-1$
				System.out.println("description " + ja.getDescription()); //$NON-NLS-1$
				System.out.println("value " + ja.getValue()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void testImplicitWithTags1() {
		Read rd = new Read();
		/* 1 */
		read.add(rd);
		rd.setDelim("\n"); //$NON-NLS-1$
		Match match = new Match();
		rd.getMatch().add(match);
		Regex regex = new Regex();
		match.setExpression(regex);
		regex.setContent("PROPERTY"); //$NON-NLS-1$
		/* 2 */
		rd = new Read();
		read.add(rd);
		rd.setDelim("\n"); //$NON-NLS-1$
		rd.setMode(AND);
		match = new Match();
		rd.getMatch().add(match);
		regex = new Regex();
		match.setExpression(regex);
		regex.setContent("name:(.*)"); //$NON-NLS-1$
		Target target = new Target();
		match.setTarget(target);
		target.setType(PROPERTY);
		Set set = new Set();
		match.getAddOrAppendOrPut().add(set);
		set.setField("name"); //$NON-NLS-1$
		set.setGroup(1);
		/* 3 */
		match = new Match();
		rd.getMatch().add(match);
		regex = new Regex();
		match.setExpression(regex);
		regex.setContent("value:(.*)"); //$NON-NLS-1$
		set = new Set();
		match.getAddOrAppendOrPut().add(set);
		set.setField("value"); //$NON-NLS-1$
		set.setGroup(1);
		rd = new Read();
		/* 4 */
		read.add(rd);
		rd.setDelim("\n"); //$NON-NLS-1$
		match = new Match();
		rd.getMatch().add(match);
		regex = new Regex();
		match.setExpression(regex);
		regex.setContent("ATTRIBUTE"); //$NON-NLS-1$
		/* 5 */
		rd = new Read();
		read.add(rd);
		rd.setDelim("\n"); //$NON-NLS-1$
		rd.setMode(AND);
		match = new Match();
		rd.getMatch().add(match);
		regex = new Regex();
		match.setExpression(regex);
		regex.setContent("name:(.*)"); //$NON-NLS-1$
		target = new Target();
		match.setTarget(target);
		target.setType(ATTRIBUTE);
		set = new Set();
		match.getAddOrAppendOrPut().add(set);
		set.setField("name"); //$NON-NLS-1$
		set.setGroup(1);
		/* 6 */
		match = new Match();
		rd.getMatch().add(match);
		regex = new Regex();
		match.setExpression(regex);
		regex.setContent("value:(.*)"); //$NON-NLS-1$
		set = new Set();
		match.getAddOrAppendOrPut().add(set);
		set.setField("value"); //$NON-NLS-1$
		set.setGroup(1);
		runTokenizer(getImplicitWithTags());
		Map<String, Object> d = RMVariableMap.getActiveInstance().getDiscovered();
		for (Object o : d.values()) {
			if (o instanceof Property) {
				Property p = (Property) o;
				if (verbose) {
					System.out.println("DISCOVERED PROPERTY:"); //$NON-NLS-1$
					System.out.println("name " + p.getName()); //$NON-NLS-1$
					System.out.println("value " + p.getValue()); //$NON-NLS-1$
					System.out.println("*********************************"); //$NON-NLS-1$
				}
			} else if (o instanceof JobAttribute) {
				JobAttribute ja = (JobAttribute) o;
				if (verbose) {
					System.out.println("DISCOVERED ATTRIBUTE:"); //$NON-NLS-1$
					System.out.println("name " + ja.getName()); //$NON-NLS-1$
					System.out.println("value " + ja.getValue()); //$NON-NLS-1$
					System.out.println("*********************************"); //$NON-NLS-1$
				}
			}
		}
	}

	public void testImplicitWithTags2() {
		Read rd = new Read();
		read.add(rd);
		rd.setMaxMatchLen(32);
		/* 1 */
		Match m = new Match();
		rd.getMatch().add(m);
		Regex r = new Regex();
		m.setExpression(r);
		r.setContent(".*PROPERTY.*name:(\\w*).*value:([\\d.-]*).*"); //$NON-NLS-1$
		r.setFlags("DOTALL"); //$NON-NLS-1$
		Target t = new Target();
		m.setTarget(t);
		t.setType(PROPERTY);
		Set set = new Set();
		m.getAddOrAppendOrPut().add(set);
		set.setField("name"); //$NON-NLS-1$
		set.setGroup(1);
		set = new Set();
		m.getAddOrAppendOrPut().add(set);
		set.setField("value"); //$NON-NLS-1$
		set.setGroup(2);
		/* 2 */
		m = new Match();
		rd.getMatch().add(m);
		r = new Regex();
		m.setExpression(r);
		r.setContent(".*PROPERTY.*value:([\\d.-]*).*name:(\\w*).*"); //$NON-NLS-1$
		r.setFlags("DOTALL"); //$NON-NLS-1$
		t = new Target();
		m.setTarget(t);
		t.setType(PROPERTY);
		set = new Set();
		m.getAddOrAppendOrPut().add(set);
		set.setField("value"); //$NON-NLS-1$
		set.setGroup(1);
		set = new Set();
		m.getAddOrAppendOrPut().add(set);
		set.setField("name"); //$NON-NLS-1$
		set.setGroup(2);
		/* 3 */
		m = new Match();
		rd.getMatch().add(m);
		r = new Regex();
		m.setExpression(r);
		r.setContent(".*ATTRIBUTE.*name:(\\w*).*value:([\\d.-]*).*"); //$NON-NLS-1$
		r.setFlags("DOTALL"); //$NON-NLS-1$
		t = new Target();
		m.setTarget(t);
		t.setType(ATTRIBUTE);
		set = new Set();
		m.getAddOrAppendOrPut().add(set);
		set.setField("name"); //$NON-NLS-1$
		set.setGroup(1);
		set = new Set();
		m.getAddOrAppendOrPut().add(set);
		set.setField("value"); //$NON-NLS-1$
		set.setGroup(2);
		/* 2 */
		m = new Match();
		rd.getMatch().add(m);
		r = new Regex();
		m.setExpression(r);
		r.setContent(".*ATTRIBUTE.*value:([\\d.-]*).*name:(\\w*).*"); //$NON-NLS-1$
		r.setFlags("DOTALL"); //$NON-NLS-1$
		t = new Target();
		m.setTarget(t);
		t.setType(ATTRIBUTE);
		set = new Set();
		m.getAddOrAppendOrPut().add(set);
		set.setField("value"); //$NON-NLS-1$
		set.setGroup(1);
		set = new Set();
		m.getAddOrAppendOrPut().add(set);
		set.setField("name"); //$NON-NLS-1$
		set.setGroup(2);
		runTokenizer(getImplicitWithTags());
		Map<String, Object> d = RMVariableMap.getActiveInstance().getDiscovered();
		for (Object o : d.values()) {
			if (o instanceof Property) {
				Property p = (Property) o;
				if (verbose) {
					System.out.println("DISCOVERED PROPERTY:"); //$NON-NLS-1$
					System.out.println("name " + p.getName()); //$NON-NLS-1$
					System.out.println("value " + p.getValue()); //$NON-NLS-1$
					System.out.println("*********************************"); //$NON-NLS-1$
				}
			} else if (o instanceof JobAttribute) {
				JobAttribute ja = (JobAttribute) o;
				if (verbose) {
					System.out.println("DISCOVERED ATTRIBUTE:"); //$NON-NLS-1$
					System.out.println("name " + ja.getName()); //$NON-NLS-1$
					System.out.println("value " + ja.getValue()); //$NON-NLS-1$
					System.out.println("*********************************"); //$NON-NLS-1$
				}
			}
		}
	}

	public void testJobId() {
		uuid = UUID.randomUUID().toString();
		target = uuid;
		Property p = new Property();
		p.setName(target);
		RMVariableMap.getActiveInstance().getVariables().put(target, p);
		read.add(getRead(getName()));
		runTokenizer(getNoiseBeforeJobId());
		p = (Property) RMVariableMap.getActiveInstance().getVariables().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(target + " = " + p.getValue()); //$NON-NLS-1$
		}
	}

	public void testJobStates() {
		target = "jobStates"; //$NON-NLS-1$
		Property p = new Property();
		p.setName(target);
		RMVariableMap.getActiveInstance().getVariables().put(target, p);
		read.add(getRead(getName()));
		runTokenizer(getJobStates());
		p = (Property) RMVariableMap.getActiveInstance().getVariables().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(target + " = " + p.getValue()); //$NON-NLS-1$
		}
	}

	public void testOpenMPI() {
		target = ATTRIBUTE;
		read.add(getRead(getName()));
		runTokenizer(getOpenMPIOut());
		Map<String, Object> d = RMVariableMap.getActiveInstance().getDiscovered();
		for (Object o : d.values()) {
			JobAttribute ja = (JobAttribute) o;
			if (verbose) {
				System.out.println("DISCOVERED ATTRIBUTE:"); //$NON-NLS-1$
				System.out.println("name " + ja.getName()); //$NON-NLS-1$
				System.out.println("value " + ja.getValue()); //$NON-NLS-1$
				System.out.println("tooltip " + ja.getTooltip()); //$NON-NLS-1$
				System.out.println("status " + ja.getStatus()); //$NON-NLS-1$
				System.out.println("visible " + ja.isVisible()); //$NON-NLS-1$
				System.out.println("readOnly " + ja.isReadOnly()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void testParseQstat() {
		target = "available_queues"; //$NON-NLS-1$
		Property p = new Property();
		p.setName(target);
		RMVariableMap.getActiveInstance().getVariables().put(target, p);
		read.add(getRead(getName()));
		runTokenizer(getQstatOut());
		p = (Property) RMVariableMap.getActiveInstance().getVariables().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(target + " = " + p.getValue()); //$NON-NLS-1$
		}
	}

	public void testPropertyDefs() {
		target = PROPERTY;
		read.add(getRead(getName()));
		runTokenizer(getPropertyDefs());
		Map<String, Object> d = RMVariableMap.getActiveInstance().getDiscovered();
		for (Object o : d.values()) {
			Property p = (Property) o;
			if (verbose) {
				System.out.println("DISCOVERED PROPERTY:"); //$NON-NLS-1$
				System.out.println("name " + p.getName()); //$NON-NLS-1$
				System.out.println("value " + p.getValue()); //$NON-NLS-1$
				System.out.println("*********************************"); //$NON-NLS-1$
			}
		}
	}

	public void testRange() {
		TestRangeAssign range = new TestRangeAssign();
		range.setRange(lineRange);
		range.setRLen(50);
		int trues = 0;

		for (int i = 0; i < 50; i++) {
			boolean b = range.test(i);
			if (oracle[i]) {
				trues++;
			}
			if (verbose) {
				System.out.println(" line " + i + " in range: " + b + ", should be " + oracle[i]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			assertEquals(oracle[i], b);
			values[i] = "" + oracle[i]; //$NON-NLS-1$
		}

		List<Object> valid = range.getValid(values);
		for (Object o : valid) {
			assertEquals("true", o); //$NON-NLS-1$
			assertEquals(trues, valid.size());
		}
	}

	private Read getRead(String name) {
		Read read = new Read();
		if (name.equals("testParseQstat")) { //$NON-NLS-1$
			read.setDelim("\n"); //$NON-NLS-1$
			Match match = new Match();
			read.getMatch().add(match);
			Regex regex = new Regex();
			match.setExpression(regex);
			regex.setContent(".*"); //$NON-NLS-1$
			Target target = new Target();
			match.setTarget(target);
			target.setRef(this.target);
			Append append = new Append();
			match.getAddOrAppendOrPut().add(append);
			append.setField("value"); //$NON-NLS-1$
			append.setSeparator(", "); //$NON-NLS-1$
			append.setGroups("0"); //$NON-NLS-1$
		} else if (name.equals("testJobStates")) { //$NON-NLS-1$
			read.setMaxMatchLen(32);
			Match match = new Match();
			read.getMatch().add(match);
			Regex regex = new Regex();
			match.setExpression(regex);
			regex.setContent(".*<job>([\\d]*):([\\w]*)</job>.*"); //$NON-NLS-1$
			regex.setFlags("DOTALL"); //$NON-NLS-1$
			Target target = new Target();
			match.setTarget(target);
			target.setRef(this.target);
			Put append = new Put();
			match.getAddOrAppendOrPut().add(append);
			append.setField("value"); //$NON-NLS-1$
			append.setKeyGroups("1"); //$NON-NLS-1$
			append.setValueGroups("2"); //$NON-NLS-1$
		} else if (name.equals("testPropertyDefs")) { //$NON-NLS-1$
			read.setDelim("\n"); //$NON-NLS-1$
			Match match = new Match();
			read.getMatch().add(match);
			Regex regex = new Regex();
			match.setExpression(regex);
			regex.setContent("<name>(.*)</name><value>(.*)</value>"); //$NON-NLS-1$
			Target target = new Target();
			match.setTarget(target);
			target.setType(this.target);
			Set set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("name"); //$NON-NLS-1$
			set.setGroup(1);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("value"); //$NON-NLS-1$
			set.setGroup(2);
		} else if (name.equals("testJobId")) { //$NON-NLS-1$
			read.setDelim("\n"); //$NON-NLS-1$
			read.setAll(true);
			read.setSave(1);
			Match match = new Match();
			read.getMatch().add(match);
			Regex regex = new Regex();
			match.setExpression(regex);
			regex.setContent("([\\d]*)[.].*"); //$NON-NLS-1$
			Target target = new Target();
			match.setTarget(target);
			target.setRef(this.target);
			Set set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("value"); //$NON-NLS-1$
			set.setGroup(1);
		} else if (name.equals("testOpenMPI")) { //$NON-NLS-1$
			read.setDelim("\n"); //$NON-NLS-1$
			/* 1 */
			Match match = new Match();
			read.getMatch().add(match);
			Regex regex = new Regex();
			match.setExpression(regex);
			regex.setContent("mca:.*:param:([^:]*):value:(.*)"); //$NON-NLS-1$
			Target target = new Target();
			match.setTarget(target);
			target.setType(this.target);
			target.setIdFrom(1);
			Set set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("name"); //$NON-NLS-1$
			set.setGroup(1);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("value"); //$NON-NLS-1$
			set.setGroup(2);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("visible"); //$NON-NLS-1$
			set.setValue(TRUE);
			/* 2 */
			match = new Match();
			read.getMatch().add(match);
			regex = new Regex();
			match.setExpression(regex);
			regex.setContent("mca:.*:param:([^:]*):status:(.*)"); //$NON-NLS-1$
			target = new Target();
			match.setTarget(target);
			target.setType(this.target);
			target.setIdFrom(1);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("status"); //$NON-NLS-1$
			set.setGroup(2);
			Test test = new Test();
			match.getTest().add(test);
			test.setOp("EQ");//$NON-NLS-1$
			test.getValue().add("this.status");//$NON-NLS-1$
			test.getValue().add("read-only");//$NON-NLS-1$
			test.setSet("readOnly");//$NON-NLS-1$
			/* 3 */
			match = new Match();
			read.getMatch().add(match);
			regex = new Regex();
			match.setExpression(regex);
			regex.setContent("mca:.*:param:([^:]*):help:(.*)"); //$NON-NLS-1$
			target = new Target();
			match.setTarget(target);
			target.setType(this.target);
			target.setIdFrom(1);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("tooltip"); //$NON-NLS-1$
			set.setGroup(2);
			/* 4 */
			match = new Match();
			read.getMatch().add(match);
			regex = new Regex();
			match.setExpression(regex);
			regex.setContent("(.*):([^:]*)"); //$NON-NLS-1$
			target = new Target();
			match.setTarget(target);
			target.setType(this.target);
			target.setIdFrom(1);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("name"); //$NON-NLS-1$
			set.setGroup(1);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("value"); //$NON-NLS-1$
			set.setGroup(2);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("readOnly"); //$NON-NLS-1$
			set.setValue(TRUE);
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("visible"); //$NON-NLS-1$
			set.setValue(FALSE);
		} else if (name.equals("testImplicitOrdering")) { //$NON-NLS-1$
			read.setDelim("\n"); //$NON-NLS-1$
			read.setMode(AND);
			/* 1 */
			Match match = new Match();
			read.getMatch().add(match);
			Regex regex = new Regex();
			match.setExpression(regex);
			regex.setContent(".*"); //$NON-NLS-1$
			Target target = new Target();
			match.setTarget(target);
			target.setType(this.target);
			Set set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("name"); //$NON-NLS-1$
			set.setGroup(0);
			/* 2 */
			match = new Match();
			read.getMatch().add(match);
			regex = new Regex();
			match.setExpression(regex);
			regex.setContent(".*"); //$NON-NLS-1$
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("type"); //$NON-NLS-1$
			set.setGroup(0);
			/* 3 */
			match = new Match();
			read.getMatch().add(match);
			regex = new Regex();
			match.setExpression(regex);
			regex.setContent(".*"); //$NON-NLS-1$
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("description"); //$NON-NLS-1$
			set.setGroup(0);
			/* 4 */
			match = new Match();
			read.getMatch().add(match);
			regex = new Regex();
			match.setExpression(regex);
			regex.setContent(".*"); //$NON-NLS-1$
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("tooltip"); //$NON-NLS-1$
			set.setGroup(0);
			/* 5 */
			match = new Match();
			read.getMatch().add(match);
			regex = new Regex();
			match.setExpression(regex);
			regex.setContent(".*"); //$NON-NLS-1$
			set = new Set();
			match.getAddOrAppendOrPut().add(set);
			set.setField("value"); //$NON-NLS-1$
			set.setGroup(0);
		}
		return read;
	}

	private void runTokenizer(InputStream stream) {
		IStreamParserTokenizer t = new ConfigurableRegexTokenizer(uuid, read);
		t.setInputStream(stream);
		if (redirect) {
			t.setRedirectStream(System.out);
		}
		Thread thr = new Thread(t);
		thr.start();
		try {
			thr.join();
		} catch (InterruptedException ignored) {
		}
	}

	private static InputStream getImplicitOrdering() {
		String content = "jobAttribute_1" + LINE_SEP + "java.lang.String" + LINE_SEP + LINE_SEP + LINE_SEP + "value_1" + LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "jobAttribute_2" + LINE_SEP + "java.lang.String" + LINE_SEP + "meaingless attribute" + LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "ignore this attribute" + LINE_SEP + "value_2" + LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getImplicitWithTags() {
		String content = "PROPERTY" + LINE_SEP + "value:423.4" + LINE_SEP + "name:x" + LINE_SEP + "ATTRIBUTE" + LINE_SEP + "name:y" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ LINE_SEP + "value:-130.42" + LINE_SEP + "PROPERTY" + LINE_SEP + "name:z" + LINE_SEP + "value:-1.7" + LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getJobStates() {
		String content = "blah blah xxxx blah blah xxxx blah blah xxxx blah blah xx" //$NON-NLS-1$
				+ "<job>304823:RUNNING</job>fooblah blah xxxx\n  blah blah xxxx blah blah xxxx blah " //$NON-NLS-1$
				+ " blah x\nx<job>312042:DONE</job>blah xxxx blah blah xxxx blah b" //$NON-NLS-1$
				+ "blah blah xxxx foobarfoobr 231028388 <job>338831:SUSPENDED" //$NON-NLS-1$
				+ "</job>fooroiq\npoiewmr<job>318388:QUEUED</job>blah blah xxxx"; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getNoiseBeforeJobId() {
		String content = "abe and lincoln will be down for 3 hours Saturday May 14, 2013 from 00:00:00 to 03:00:00" + LINE_SEP //$NON-NLS-1$
				+ "There is currently no way to know what your shell is.  Please look in /etc/passwd" + LINE_SEP //$NON-NLS-1$
				+ "To get your default account, click your heels three times and say, 'There's no place like home!'" + LINE_SEP //$NON-NLS-1$
				+ "429324.honest1" + LINE_SEP; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getOpenMPIOut() {
		String content = "mca:mca:base:param:mca_component_disable_dlopen:status:writable" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mca:base:param:mca_component_disable_dlopen:help:Whether to attempt to disable opening dynamic components or not" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mca:base:param:mca_component_disable_dlopen:deprecated:no" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:value:1" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:data_source:default value" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:status:read-only" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:help:Whether you want MPI API parameters checked at run-time or not.  Possible values are 0 (no checking) and 1 (perform checking at run-time)" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:deprecated:no" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:value:-1" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:data_source:default value" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:status:writable" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:help:Yield the processor when waiting for MPI communication (for MPI processes, will default to 1 when oversubscribing nodes)" //$NON-NLS-1$
				+ LINE_SEP + "mca:mpi:base:param:mpi_yield_when_idle:deprecated:no" + LINE_SEP //$NON-NLS-1$
				+ "mca:mpi:base:param:mpi_event_tick_rate:value:-1" + LINE_SEP; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getPropertyDefs() {
		String content = "<name>pnameA</name><value>pvalueA</value>" + LINE_SEP + "<name>pnameB</name><value>pvalueB</value>" //$NON-NLS-1$ //$NON-NLS-2$
				+ LINE_SEP + "<name>pnameC</name><value>pvalueC</value>" + LINE_SEP + "<name>pnameD</name><value>pvalueD</value>" //$NON-NLS-1$ //$NON-NLS-2$
				+ LINE_SEP + "<value>pvalueW</value><name>pnameW</name>" + LINE_SEP; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getQstatOut() {
		String content = "normal" + LINE_SEP + "iacat2" + LINE_SEP + "indprio" + LINE_SEP + "lincoln_nomss" + LINE_SEP + "cap1" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ LINE_SEP + "lincoln_debug" + LINE_SEP + "long" + LINE_SEP + "iacat" + LINE_SEP + "industrial" + LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ "lincoln" + LINE_SEP + "wide" + LINE_SEP + "nomss" + LINE_SEP + "debug" + LINE_SEP + "iacat3" + LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ "lincoln_cuda3.2" + LINE_SEP + "fernsler" + LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$
		return new ByteArrayInputStream(content.getBytes());
	}
}
