/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/package org.eclipse.ptp.core.tests;

import junit.framework.TestCase;

import org.eclipse.ptp.core.util.ArgumentParser;

public class ArgumentParserTests extends TestCase {
	
	public void testFileStore() {
		ArgumentParser parser = new ArgumentParser("foobar", new String[] {"arg1", "arg2", "arg\\3", "arg\"4", "arg'5", "more arguments"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		assertEquals("foobar arg1 arg2 arg\\3 arg\"4 arg'5 more\\ arguments", parser.getCommandLine(false));
		assertEquals("foobar arg1 arg2 arg\\\\3 arg\\\"4 arg\\'5 more\\ arguments", parser.getCommandLine(true));
		
		parser = new ArgumentParser(" foo");
		assertEquals("foo", parser.getCommandLine(true));
		parser = new ArgumentParser("foo ");
		assertEquals("foo", parser.getCommandLine(true));
		parser = new ArgumentParser("foo");
		assertEquals("foo", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser("foo  a");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a ");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser(" foo a");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser(" foo a ");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser(" foo   a ");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser("foo	a");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a	");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser("foo	a	");
		assertEquals("foo a", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a b");
		assertEquals("foo a b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a b ");
		assertEquals("foo a b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a b c ");
		assertEquals("foo a b c", parser.getCommandLine(true));
		parser = new ArgumentParser("foo\\ a b");
		assertEquals("foo\\ a b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo \\ab");
		assertEquals("foo ab", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a\\ b\\ c ");
		assertEquals("foo a\\ b\\ c", parser.getCommandLine(true));
		parser = new ArgumentParser("\\foo a b c ");
		assertEquals("foo a b c", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a b c\\");
		assertEquals("foo a b c", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \\b c");
		assertEquals("foo a b c", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a\\'c b");
		assertEquals("foo a\\'c b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a\\\"c b");
		assertEquals("foo a\\\"c b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \\'c b");
		assertEquals("foo a \\'c b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \\\"c b");
		assertEquals("foo a \\\"c b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a\\' c b");
		assertEquals("foo a\\' c b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a\\\" c b");
		assertEquals("foo a\\\" c b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \\' c b");
		assertEquals("foo a \\' c b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \\\" c b");
		assertEquals("foo a \\\" c b", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a c b\\'");
		assertEquals("foo a c b\\'", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a c b\\\"");
		assertEquals("foo a c b\\\"", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a c b \\'");
		assertEquals("foo a c b \\\'", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a c b \\\"");
		assertEquals("foo a c b \\\"", parser.getCommandLine(true));
		parser = new ArgumentParser("\\'foo a c b");
		assertEquals("\\\'foo a c b", parser.getCommandLine(true));
		parser = new ArgumentParser("\\\"foo a c b");
		assertEquals("\\\"foo a c b", parser.getCommandLine(true));
		parser = new ArgumentParser("\\' foo a c b");
		assertEquals("\\\' foo a c b", parser.getCommandLine(true));
		parser = new ArgumentParser("\\\" foo a c b");		
		assertEquals("\\\" foo a c b", parser.getCommandLine(true));
		parser = new ArgumentParser("'foo a' b c d");
		assertEquals("foo\\ a b c d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a b 'c d'");
		assertEquals("foo a b c\\ d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a 'b c' d");
		assertEquals("foo a b\\ c d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a 'b\\e' d");
		assertEquals("foo a b\\\\e d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \"b\\e\" d");
		assertEquals("foo a b\\\\e d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a 'b c d");
		assertEquals("foo a b\\ c\\ d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \"b c d");
		assertEquals("foo a b\\ c\\ d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \"b c\" d");
		assertEquals("foo a b\\ c d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \"b c\"d");
		assertEquals("foo a b\\ cd", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a 'b c' d");
		assertEquals("foo a b\\ c d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a 'b c'd");
		assertEquals("foo a b\\ cd", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a 'b \" c' d");
		assertEquals("foo a b\\ \\\"\\ c d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \"b ' c\" d");
		assertEquals("foo a b\\ \\'\\ c d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a 'b \\\" c' d");
		assertEquals("foo a b\\ \\\\\\\"\\ c d", parser.getCommandLine(true));
		parser = new ArgumentParser("foo a \"b \' c\" d");		
		assertEquals("foo a b\\ \\'\\ c d", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {});
		assertEquals("", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a"});
		assertEquals("a", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"av"});
		assertEquals("av", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a d"});
		assertEquals("a\\ d", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a", "a"});
		assertEquals("a a", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"av", "a"});
		assertEquals("av a", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a d", "a"});
		assertEquals("a\\ d a", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a", "b b"});
		assertEquals("a b\\ b", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"av", "b b"});
		assertEquals("av b\\ b", parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a d", "b b"});
		assertEquals("a\\ d b\\ b", parser.getCommandLine(true));
	}
}
