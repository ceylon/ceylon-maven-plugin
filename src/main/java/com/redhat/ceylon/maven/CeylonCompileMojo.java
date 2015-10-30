package com.redhat.ceylon.maven;

import com.redhat.ceylon.maven.compiler.ExtendedCompilerOptions;
import com.redhat.ceylon.maven.compiler.JavaCompilerImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.redhat.ceylon.compiler.java.runtime.tools.*;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class CeylonCompileMojo extends AbstractMojo {

  @Parameter(defaultValue = "false")
  private boolean verbose;

  @Parameter(defaultValue = "${project.build.directory}/modules")
  private File modulesDirectory;

  @Parameter()
  private List sources;

  @Parameter
  private String[] userRepos;

  @Parameter
  private String target;

  public void execute() throws MojoExecutionException, MojoFailureException {
    FileSetManager fileSetManager = new FileSetManager();
    for (Source source : (List<Source>)sources) {
      File sourcePath = new File(source.getFileset().getDirectory());
      Compiler compiler = new JavaCompilerImpl();
      ExtendedCompilerOptions options = new ExtendedCompilerOptions();
      options.setSourcePath(Collections.singletonList(sourcePath));
      options.setOutputRepository(modulesDirectory.getAbsolutePath());
      if (target != null) {
        options.setTarget(target);
      }
      options.setVerbose(verbose);
      if (userRepos != null) {
        for (String userRepo : userRepos) {
          options.addUserRepository(new File(userRepo).getAbsolutePath());
        }
      }
      ArrayList<File> sources = new ArrayList<File>();
      for (String included : fileSetManager.getIncludedFiles(source.getFileset())) {
        sources.add(new File(sourcePath, included));
      }
      options.setFiles(sources);
      compiler.compile(options, new CompilationListener() {

        public void error(File file, long line, long column, String message) {
          String msg;
          if (file != null) {
            msg = "Compilation error at (" + line + "," + column + ") in " +
                file.getAbsolutePath() + ":" + message;
          } else {
            msg = "Compilation error:" + message;
          }
          System.out.println(msg);
        }

        public void warning(File file, long line, long column, String message) {
          String msg;
          if (file != null) {
            msg = "Compilation warning at (" + line + "," + column + ") in " +
                file.getAbsolutePath() + ":" + message;
          } else {
            msg = "Compilation warning:" + message;
          }
          System.out.println(msg);
        }

        public void moduleCompiled(String module, String version) {
          System.out.println("Compiled module " + module + "/" + version);
        }
      });
    }
  }
}
