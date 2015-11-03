package com.redhat.ceylon.maven;

import com.redhat.ceylon.maven.tools.ExtendedCompilerOptions;
import com.redhat.ceylon.maven.tools.JavaCompilerImpl;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class CeylonCompileMojo extends AbstractMojo {

  @Parameter(defaultValue = "false")
  private boolean verbose;

  @Parameter(defaultValue = "${project.build.directory}")
  private File cwd;

  @Parameter(defaultValue = "modules")
  private String out;

  @Parameter()
  private List sources;

  @Parameter
  private String[] userRepos;

  @Parameter
  private String javacOptions;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (sources != null) {
      FileSetManager fileSetManager = new FileSetManager();
      ArrayList<File> sources = new ArrayList<File>();
      ArrayList<File> sourcePaths = new ArrayList<File>();
      for (Source source : (List<Source>)this.sources) {
        File sourcePath = new File(source.getFileset().getDirectory());
        Set<File> excluded = new HashSet<File>();
        for (String excludedFile : fileSetManager.getExcludedFiles(source.getFileset())) {
          excluded.add(new File(sourcePath, excludedFile));
        }
        for (String includedFile : fileSetManager.getIncludedFiles(source.getFileset())) {
          File included = new File(sourcePath, includedFile);
          if (!excluded.contains(included)) {
            sources.add(included);
          }
        }
        sourcePaths.add(new File(source.getFileset().getDirectory()));
      }
      compile(sourcePaths, sources);
    } else {
      File sourcePath = new File("src/main/ceylon");
      if (sourcePath.exists() && sourcePath.isDirectory()) {
        List<File> sources = new ArrayList<File>();
        collectSources(sourcePath, sources);
        compile(Collections.singletonList(sourcePath), sources);
      }
    }
  }

  private void collectSources(File dir, List<File> sources) {
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          collectSources(file, sources);
        } else if (file.isFile() && (file.getName().endsWith(".ceylon") || file.getName().endsWith(".java"))) {
          sources.add(file);
        }
      }
    }
  }

  private void compile(List<File> sourcePaths, List<File> sources) throws MojoExecutionException, MojoFailureException {
    Compiler compiler = new JavaCompilerImpl();
    ExtendedCompilerOptions options = new ExtendedCompilerOptions();
    options.setSourcePath(sourcePaths);
    options.setCwd(cwd.getAbsolutePath());
    options.setOutputRepository(out);
    if (javacOptions != null && javacOptions.length() > 0) {
      options.setJavacOptions(javacOptions);
    }
    options.setVerbose(verbose);
    if (userRepos != null) {
      for (String userRepo : userRepos) {
        options.addUserRepository(userRepo);
      }
    }
    options.setFiles(sources);
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
}
