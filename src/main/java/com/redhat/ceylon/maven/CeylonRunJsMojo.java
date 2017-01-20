package com.redhat.ceylon.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.redhat.ceylon.common.ModuleSpec;
import com.redhat.ceylon.common.config.CeylonConfig;
import com.redhat.ceylon.compiler.java.runtime.tools.RunnerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.impl.JavaScriptRunnerImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "run-js", defaultPhase = LifecyclePhase.NONE)
public class CeylonRunJsMojo extends AbstractCeylonMojo {

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
      RunnerOptions runnerOptions = RunnerOptions.fromConfig(cfg);
      if (verbose != null) {
        runnerOptions.setVerbose(true);
        if (!"true".equals(verbose)) {
          runnerOptions.setVerboseCategory(verbose);
        }
      }
      if (userRepos != null) {
        for (String userRepo : userRepos) {
          runnerOptions.addUserRepository(userRepo);
        }
      } else {
        runnerOptions.addUserRepository(buildDir + "/modules");
      }
      if(ceylonHome != null)
      	runnerOptions.setSystemRepository(ceylonHome + "/repo");
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
        JavaScriptRunnerImpl runner = new JavaScriptRunnerImpl(runnerOptions, moduleSpec.getName(), moduleSpec.getVersion());
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
