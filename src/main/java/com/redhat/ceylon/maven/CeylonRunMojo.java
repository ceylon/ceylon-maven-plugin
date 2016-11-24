package com.redhat.ceylon.maven;

import com.redhat.ceylon.common.ModuleSpec;
import com.redhat.ceylon.common.config.CeylonConfig;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunner;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunnerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.impl.JavaRunnerImpl;

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

  @Parameter(readonly = true, property = "project.build.directory")
  private String buildDir;

  @Parameter(readonly = true, property = "basedir")
  private File cwd;

  @Parameter(defaultValue = "false")
  private boolean verbose;

  @Parameter(required = true)
  private String module;

  @Parameter
  private String[] userRepos;

  @Parameter
  private String[] arguments;

  @Parameter
  private boolean skip;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!skip) {
      CeylonConfig cfg = CeylonConfig.createFromLocalDir(cwd);
      JavaRunnerOptions runnerOptions = JavaRunnerOptions.fromConfig(cfg);
      runnerOptions.setVerbose(verbose);
      if (userRepos != null) {
        for (String userRepo : userRepos) {
          runnerOptions.addUserRepository(userRepo);
        }
      } else {
        runnerOptions.addUserRepository(buildDir + "/modules");
      }
      if (cwd != null) {
          runnerOptions.setWorkingDirectory(cwd.getAbsolutePath());
      }
      ModuleSpec moduleSpec;
      try {
        moduleSpec = ModuleSpec.parse(module);
      } catch (Exception e) {
        throw new MojoExecutionException("Invalid module name " + module, e);
      }
      try {
        JavaRunner runner = new JavaRunnerImpl(runnerOptions, moduleSpec.getName(), moduleSpec.getVersion());
        if (arguments != null) {
          runner.run(arguments);
        } else {
          runner.run();
        }
      } catch (Exception e) {
        throw new MojoExecutionException("Execution error", e);
      }
    }
  }
}
