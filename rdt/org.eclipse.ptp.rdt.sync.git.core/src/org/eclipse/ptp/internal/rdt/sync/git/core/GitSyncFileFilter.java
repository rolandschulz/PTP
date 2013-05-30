package org.eclipse.ptp.internal.rdt.sync.git.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.core.resources.IResource;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncManager;

public class GitSyncFileFilter extends AbstractSyncFileFilter {

	private Repository repository;
	
	public class GitIgnoreRule extends AbstractIgnoreRule {
		private org.eclipse.jgit.ignore.IgnoreRule rule;
		
		public GitIgnoreRule(String pattern, boolean exclude) {
			if (!exclude) pattern = "!" + pattern; //$NON-NLS-1$
			rule = new org.eclipse.jgit.ignore.IgnoreRule(pattern);
		}
		
		public GitIgnoreRule(IResource resource, boolean exclude) {
			//TODO: is toString the correct method? or should it be toPortableString
			//TODO: !,*,[,? need to be escaped 
			String pattern = resource.getProjectRelativePath().toString();
			if (resource.getType()==IResource.FOLDER) pattern += "/"; //$NON-NLS-1$
			if (!exclude) pattern = "!" + pattern; //$NON-NLS-1$
			rule = new org.eclipse.jgit.ignore.IgnoreRule(pattern);
		}
		
		private GitIgnoreRule(org.eclipse.jgit.ignore.IgnoreRule rule) {
			this.rule = rule;
		}
		
		@Override
		public boolean isMatch(IResource target) {
			return rule.isMatch(target.getProjectRelativePath().toString(), target.getType()==IResource.FOLDER);
		}

		@Override
		public boolean getResult() {
			return rule.getResult();
		}
		
		@Override
		public String toString() {
			return rule.getPattern();
		}
		
		@Override
		public String getPattern() {
			String pattern = rule.getPattern();
			if (pattern.charAt(0)=='!')
				pattern.substring(1);
			return pattern;
		}
	}
	
	GitSyncFileFilter(Repository repository) {
		this.repository = repository;
	}
	
	@Override
	public void addPattern(String pattern, boolean exclude, int index) {
		rules.add(index, new GitIgnoreRule(pattern, exclude));

	}

	@Override
	public void addPattern(IResource resource, boolean exclude, int index) {
		rules.add(index, new GitIgnoreRule(resource, exclude));

	}

	public void saveFilter() throws IOException  {
		File exclude = repository.getFS().resolve(repository.getDirectory(),
				Constants.INFO_EXCLUDE);
		FileOutputStream file = new FileOutputStream(exclude);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(file,Constants.CHARSET));
		try {
			for (AbstractIgnoreRule rule : rules) {
				out.write(rule.toString());
				out.newLine();
			}
		} finally {
			out.close();
		}
	}
	
	public void loadFilter() throws IOException {
		File exclude = repository.getFS().resolve(repository.getDirectory(),
				Constants.INFO_EXCLUDE);
		if (exclude.exists()) {
			FileInputStream in = new FileInputStream(exclude);
			try {
				IgnoreNode node = new IgnoreNode();
				node.parse(in);
				for (org.eclipse.jgit.ignore.IgnoreRule rule : node.getRules())
					rules.add(new GitIgnoreRule(rule));
			} finally {
				in.close();
			}
		} else {
			clone(SyncManager.getDefaultFileFilter());
			rules.add(new GitIgnoreRule("/.ptp-sync/", true)); //$NON-NLS-1$
		}
	}

	@Override
	public void clone(AbstractSyncFileFilter fileFilter) {
		//
		for (AbstractIgnoreRule rule : fileFilter.rules)
			rules.add(new GitIgnoreRule(rule.getPattern(),rule.getResult()));
	}

}
