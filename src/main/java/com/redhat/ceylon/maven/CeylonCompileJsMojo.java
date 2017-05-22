package com.redhat.ceylon.maven;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.redhat.ceylon.common.Backend;
import com.redhat.ceylon.common.config.CeylonConfig;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilationListener;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.impl.JavaScriptCompilerImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "compile-js", defaultPhase = LifecyclePhase.COMPILE)
public class CeylonCompileJsMojo extends AbstractCeylonCompileMojo {

  @Override
  protected void compile(List<File> sourcePath, List<File> resourcePath, List<File> files, List<String> modules) 
		  throws MojoExecutionException, MojoFailureException {
    exportDependencies();
    CeylonConfig cfg = CeylonConfig.createFromLocalDir(cwd);
    Compiler compiler = new JavaScriptCompilerImpl();
    CompilerOptions options = CompilerOptions.fromConfig(cfg);
    options.setModules(modules);
    options.setSourcePath(sourcePath);
    options.setResourcePath(resourcePath);
    if (cwd != null) {
        options.setWorkingDirectory(cwd.getAbsolutePath());
    }
    options.setOutputRepository(out);
    if (verbose != null) {
      options.setVerbose(true);
      if (!"true".equals(verbose)) {
        options.setVerboseCategory(verbose);
      }
    }
    if (userRepos != null) {
      for (String userRepo : userRepos) {
        options.addUserRepository(userRepo);
      }
    } else {
      options.addUserRepository(buildDir + "/modules");
    }
    addExportedUserRepository(options);
    if(ceylonHome != null)
    	options.setSystemRepository(ceylonHome + "/repo");
    if(timeout != null)
    	options.setTimeout(timeout);
    options.setFiles(files);
    boolean ok = compiler.compile(options, new CompilationListener() {

      public void error(File file, long line, long column, String message) {
        String msg;
        if (file != null) {
          msg = "Compilation error at (" + line + "," + column + ") in " +
              file.getAbsolutePath() + ":" + message;
        } else {
          msg = "Compilation error:" + message;
        }
        System.out.println("ERROR : " + msg);
        getLog().error(msg);
      }

      public void warning(File file, long line, long column, String message) {
        String msg;
        if (file != null) {
          msg = "Compilation warning at (" + line + "," + column + ") in " +
              file.getAbsolutePath() + ":" + message;
        } else {
          msg = "Compilation warning:" + message;
        }
        getLog().warn(msg);
        System.out.println(msg);
      }

      public void moduleCompiled(String module, String version) {
        getLog().info("Compiled module " + module + "/" + version);
      }
    });

    if (!ok) {
      throw new MojoExecutionException("Compilation failed");
    }
  }

  @Override
  protected Backend getBackend() {
	return Backend.JavaScript;
  }
}
