package com.redhat.ceylon.maven;

import com.redhat.ceylon.compiler.java.runtime.tools.Backend;
import com.redhat.ceylon.compiler.java.runtime.tools.CeylonToolProvider;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunner;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunnerOptions;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.NONE)
public class CeylonRunMojo extends AbstractMojo {

  @Parameter(defaultValue = "false")
  private boolean verbose;

  @Parameter
  private String module;

  @Parameter
  private String version;

  @Parameter
  private String[] userRepos;

  public void execute() throws MojoExecutionException, MojoFailureException {
    JavaRunnerOptions runnerOptions = new JavaRunnerOptions();
    runnerOptions.setVerbose(verbose);
    for (String userRepo : userRepos) {
      runnerOptions.addUserRepository(new File(userRepo).getAbsolutePath());
    }
    JavaRunner runner = (JavaRunner) CeylonToolProvider.getRunner(Backend.Java, runnerOptions, module, version);
    runner.run();
  }
}
