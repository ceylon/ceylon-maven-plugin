package com.redhat.ceylon.maven;

import com.redhat.ceylon.ceylondoc.CeylonDocTool;
import com.redhat.ceylon.common.tools.CeylonTool;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "doc")
public class CeylonDocMojo extends AbstractCeylonMojo {

  @Parameter(defaultValue = "${project.build.directory}/modules")
  private String out;

  @Parameter()
  private List<File> sources;

  @Parameter
  private String[] userRepos;

  @Parameter(required = true)
  private String[] modules;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      CeylonDocTool tool = new CeylonDocTool();
      if (sources == null) {
        sources = Collections.singletonList(new File("src/main/ceylon"));
      }
      if (userRepos != null) {
        tool.setRepositoryAsStrings(Arrays.asList(userRepos));
      } else {
        tool.setRepositoryAsStrings(Arrays.asList(new String[] {buildDir + "/modules"}));
      }
      if(ceylonHome != null)
      	tool.setSystemRepository(ceylonHome + "/repo");
      if(timeout != null)
      	tool.setTimeout(timeout);
      tool.setSourceFolders(sources);
      tool.setCwd(cwd);
      tool.setOut(out);
      if (verbose != null) {
    	  tool.setVerbose(verbose);
      }
      tool.setModuleSpecs(modules != null ? Arrays.asList(modules) : Collections.<String>emptyList());
      tool.initialize(new CeylonTool());
      tool.run();
    } catch (Exception e) {
      throw new MojoExecutionException("Doc tool error", e);
    }

  }
}
