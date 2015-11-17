package com.redhat.ceylon.maven;

import com.redhat.ceylon.ceylondoc.CeylonDocTool;
import com.redhat.ceylon.common.tools.CeylonTool;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
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
public class CeylonDocMojo extends AbstractMojo {

  @Parameter
  private String verbose;

  @Parameter(property = "ceylon.cwd", defaultValue = "${project.build.directory}")
  private File cwd;

  @Parameter(defaultValue = "modules")
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
      tool.setSourceFolders(sources);
      tool.setCwd(cwd);
      tool.setRepositoryAsStrings(userRepos != null ? Arrays.asList(userRepos) : null);
      tool.setOut(out);
      tool.setVerbose(verbose);
      tool.setModuleSpecs(modules != null ? Arrays.asList(modules) : Collections.<String>emptyList());
      tool.initialize(new CeylonTool());
      tool.run();
    } catch (Exception e) {
      throw new MojoExecutionException("Doc tool error", e);
    }

  }
}
