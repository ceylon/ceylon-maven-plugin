package com.redhat.ceylon.maven;

import com.redhat.ceylon.common.tools.ModuleSpec;
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
import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.NONE)
public class CeylonRunMojo extends AbstractMojo {

  @Parameter(property = "ceylon.cwd", defaultValue = "${project.build.directory}")
  private File cwd;

  @Parameter(defaultValue = "false")
  private boolean verbose;

  @Parameter(required = true)
  private String module;

  @Parameter
  private String[] userRepos;

  @Parameter
  private String[] arguments;

  public void execute() throws MojoExecutionException, MojoFailureException {
    ExtendedRunnerOptions runnerOptions = new ExtendedRunnerOptions();
    runnerOptions.setVerbose(verbose);
    if (userRepos != null) {
      for (String userRepo : userRepos) {
        runnerOptions.addUserRepository(userRepo);
      }
    }
    runnerOptions.setCwd(cwd);
    ModuleSpec moduleSpec;
    try {
      moduleSpec = ModuleSpec.parse(module);
    } catch (Exception e) {
      throw new MojoExecutionException("Invalid module name " + module, e);
    }
    JavaRunner runner = new JavaRunnerImpl(runnerOptions, moduleSpec.getName(), moduleSpec.getVersion());
    try {
      if (arguments != null) {
        runner.run(arguments);
      } else {
        runner.run();
      }
    } catch (Exception e) {
      // It shall be a InvocationTargetException so get the cause
      throw new MojoExecutionException("Execution error", e.getCause());
    }
  }
}
