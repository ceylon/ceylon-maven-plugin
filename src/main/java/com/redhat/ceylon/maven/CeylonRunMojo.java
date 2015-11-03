package com.redhat.ceylon.maven;

import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunner;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunnerOptions;
import com.redhat.ceylon.maven.tools.ExtendedRunnerOptions;
import com.redhat.ceylon.maven.tools.JavaRunnerImpl;
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

  @Parameter(property = "ceylon.cwd", defaultValue = "${project.build.directory}")
  private File cwd;

  @Parameter(defaultValue = "false")
  private boolean verbose;

  @Parameter
  private String module;

  @Parameter
  private String version;

  @Parameter
  private String[] userRepos;

  public void execute() throws MojoExecutionException, MojoFailureException {
    ExtendedRunnerOptions runnerOptions = new ExtendedRunnerOptions();
    runnerOptions.setVerbose(verbose);
    if (userRepos != null) {
      for (String userRepo : userRepos) {
        runnerOptions.addUserRepository(userRepo);
      }
    }
    runnerOptions.setCwd(cwd);
    JavaRunner runner = new JavaRunnerImpl(runnerOptions, module, version);
    runner.run();
  }
}
