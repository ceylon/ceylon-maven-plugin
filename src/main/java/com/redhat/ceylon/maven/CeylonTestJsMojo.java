package com.redhat.ceylon.maven;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.redhat.ceylon.cmr.api.ModuleVersionDetails;
import com.redhat.ceylon.cmr.impl.DefaultRepository;
import com.redhat.ceylon.common.Backend;
import com.redhat.ceylon.common.ModuleSpec;
import com.redhat.ceylon.common.Versions;
import com.redhat.ceylon.common.config.CeylonConfig;
import com.redhat.ceylon.common.tools.ModuleVersionReader;
import com.redhat.ceylon.common.tools.ModuleWildcardsHelper;
import com.redhat.ceylon.compiler.java.runtime.tools.Runner;
import com.redhat.ceylon.compiler.java.runtime.tools.RunnerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.impl.JavaScriptRunnerImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "test-js", defaultPhase = LifecyclePhase.TEST)
public class CeylonTestJsMojo extends AbstractCeylonMojo {

    @Parameter( property = "maven.test.failure.ignore", defaultValue = "false" )
    private boolean testFailureIgnore;

    @Parameter( property = "skipTests", defaultValue = "false" )
    private boolean skipTests;

    @Parameter( property = "maven.test.skip", defaultValue = "false" )
    private boolean mavenTestSkip;

  @Parameter
  private String[] modules;

  @Parameter
  private String[] userRepos;

  @Parameter
  private String[] arguments;

  @Parameter
  private boolean skip;

  public void execute() throws MojoExecutionException, MojoFailureException {
	if (!skip && !mavenTestSkip && !skipTests) {
      exportDependencies();
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
      addExportedUserRepository(runnerOptions);
      if(ceylonHome != null)
      	runnerOptions.setSystemRepository(ceylonHome + "/repo");
      if(timeout != null)
      	runnerOptions.setTimeout(timeout);
      if (cwd != null) {
          runnerOptions.setWorkingDirectory(cwd.getAbsolutePath());
      }
      ModuleSpec[] moduleSpecs;
      if(modules != null && modules.length > 0){
    	  moduleSpecs = new ModuleSpec[modules.length];
    	  int i=0;
    	  for (String module : modules) {
    		  try {
    			  moduleSpecs[i++] = ModuleSpec.parse(module);
    		  } catch (Exception e) {
    			  throw new MojoExecutionException("Invalid module name " + module, e);
    		  }
    	  }
      }else{
    	  List<String> foundModules = new LinkedList<>();
    	  // FIXME: this should be configurable
          File sourcePath = new File(cwd, "src/test/ceylon");
    	  List<File> sourcePaths = Arrays.asList(sourcePath);
    	  ModuleWildcardsHelper.expandWildcard(foundModules, sourcePaths, "*", Backend.JavaScript);
    	  ModuleVersionReader versionReader = new ModuleVersionReader(sourcePaths);
    	  moduleSpecs = new ModuleSpec[foundModules.size()];
    	  int i=0;
    	  for (String module : foundModules) {
    		  ModuleVersionDetails details = versionReader.fromSource(module);
    		  moduleSpecs[i++] = new ModuleSpec(DefaultRepository.NAMESPACE, module, details.getVersion());
    	  }
      }
      Map<String, String> extraModules = new HashMap<>();
      for (ModuleSpec moduleSpec : moduleSpecs) {
    	  extraModules.put(moduleSpec.getName(), moduleSpec.getVersion());
      }
      runnerOptions.setExtraModules(extraModules);
      runnerOptions.setRun("ceylon.test::runTestTool");

      try {
          Runner runner = new JavaScriptRunnerImpl(runnerOptions, "ceylon.test", Versions.CEYLON_VERSION_NUMBER);
          String[] args;
          if (arguments != null) {
          	args = new String[3+(2*moduleSpecs.length)+arguments.length];
          	System.arraycopy(arguments, 0, args, 2*moduleSpecs.length, arguments.length);
          } else {
          	args = new String[3+2*moduleSpecs.length];
          }
          int i = 0;
          args[i++] = "--xml-junit-report";
          args[i++] = "--reports-dir";
          args[i++] = project.getBuild().getDirectory()+"/surefire-reports";
          System.err.println("Reports at "+(project.getBuild().getDirectory()+"/surefire-reports"));
          for (ModuleSpec moduleSpec : moduleSpecs) {
  			args[i++] = "--module";
  			args[i++] = moduleSpec.getName() + "/" + moduleSpec.getVersion();
  		}
        runner.run(args);	
      } catch (Exception e) {
    	  if(e.getCause() != null
    			  && e.getCause().getMessage().contains("Node process exited with non-zero exit code: 100")){
    		  // Surefire sets this one so that "test" targets fail, but "reporting" targets don't
    		  if(testFailureIgnore)
    			  getLog().error("Tests failed: "+e.getMessage());
    		  else
    			  throw new MojoFailureException("Test failure", e);
    	  }else
    		  throw new MojoExecutionException("Execution error", e);
      }
    }
  }
}
