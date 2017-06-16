package com.redhat.ceylon.maven;

import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.redhat.ceylon.common.tools.CeylonTool;
import com.redhat.ceylon.tools.copy.CeylonCopyTool;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "copy")
public class CeylonCopyMojo extends AbstractCeylonMojo {

	@Parameter(property = "ceylon.username")
	protected String username;

	@Parameter(property = "ceylon.password")
	protected String password;

	@Parameter(defaultValue = "${project.build.directory}/modules", property = "ceylon.out")
	protected String out;

	@Parameter
	protected String[] userRepos;

	@Parameter(required = true)
	private String[] modules;

	@Parameter
	protected Boolean all;

	@Parameter
	protected Boolean js;

	@Parameter
	protected Boolean jvm;

	@Parameter
	protected Boolean docs;

	@Parameter
	protected Boolean scripts;

	@Parameter
	protected Boolean src;

	@Parameter
	protected Boolean withDependencies;
	
	@Parameter
	protected Boolean includeLanguage;


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		CeylonCopyTool tool = new CeylonCopyTool();
		tool.setOut(out);
		tool.setModules(Arrays.asList(modules));
		tool.setVerbose(verbose);
		if(username != null)
			tool.setUser(username);
		if(password != null)
			tool.setPass(password);
		if(all != null)
			tool.setAll(all);
		if(js != null)
			tool.setJs(js);
		if(jvm != null)
			tool.setJvm(jvm);
		if(src != null)
			tool.setSrc(src);
		if(scripts != null)
			tool.setScripts(scripts);
		if(docs != null)
			tool.setDocs(docs);
		if(withDependencies != null)
			tool.setWithDependencies(withDependencies);
		if(includeLanguage != null)
			tool.setIncludeLanguage(includeLanguage);

		try {
			if(userRepos != null)
				tool.setRepositoryAsStrings(Arrays.asList(userRepos));
			else
		        tool.setRepositoryAsStrings(Arrays.asList(buildDir + "/modules"));

		} catch (Exception e) {
	        throw new MojoExecutionException("Failed to set user repositories " + userRepos, e);
		}
		if(ceylonHome != null)
			tool.setSystemRepository(ceylonHome + "/repo");
	    if(timeout != null)
	    	tool.setTimeout(timeout);
		tool.setCwd(cwd);
		tool.initialize(new CeylonTool());
		try {
			tool.run();
		} catch (Exception e) {
	        throw new MojoExecutionException("Failed to copy modules " + modules, e);
		}
	}
}
