package com.redhat.ceylon.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.redhat.ceylon.common.config.CeylonConfig;
import com.redhat.ceylon.compiler.java.runtime.tools.*;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import com.redhat.ceylon.compiler.java.runtime.tools.impl.JavaScriptCompilerImpl;

import org.apache.maven.shared.model.fileset.FileSet;
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
@Mojo(name = "compile-js", defaultPhase = LifecyclePhase.COMPILE)
public class CeylonCompileJsMojo extends AbstractMojo {

  @Parameter(readonly = true, property = "project.build.directory")
  private String buildDir;

  @Parameter
  private String verbose;

  @Parameter(readonly = true, property = "basedir")
  private File cwd;

  @Parameter(defaultValue = "${project.build.directory}/modules")
  private String out;

  @Parameter()
  private List<FileSet> sources;

  @Parameter()
  private List<FileSet> resources;

  @Parameter
  private String[] userRepos;

  public void execute() throws MojoExecutionException, MojoFailureException {
    ArrayList<File> files = new ArrayList<>();
    ArrayList<File> sourcePaths = new ArrayList<>();
    ArrayList<File> resourcePaths = new ArrayList<>();
    if (this.sources != null) {
      FileSetManager fileSetManager = new FileSetManager();
      for (FileSet source : this.sources) {
        File sourcePath = new File(source.getDirectory());
        Set<File> excluded = new HashSet<>();
        for (String excludedFile : fileSetManager.getExcludedFiles(source)) {
          excluded.add(new File(sourcePath, excludedFile));
        }
        for (String includedFile : fileSetManager.getIncludedFiles(source)) {
          File included = new File(sourcePath, includedFile);
          if (!excluded.contains(included)) {
            files.add(included);
          }
        }
        sourcePaths.add(new File(source.getDirectory()));
      }
    } else {
      File sourcePath = new File(cwd, "src/main/ceylon");
      if (sourcePath.exists() && sourcePath.isDirectory()) {
        collectSources(sourcePath, files);
        sourcePaths.add(sourcePath);
      }
    }
    if (this.resources != null) {
      FileSetManager fileSetManager = new FileSetManager();
      for (FileSet resource : this.resources) {
        File resourcePath = new File(resource.getDirectory());
        Set<File> excluded = new HashSet<>();
        for (String excludedFile : fileSetManager.getExcludedFiles(resource)) {
          excluded.add(new File(resourcePath, excludedFile));
        }
        for (String includedFile : fileSetManager.getIncludedFiles(resource)) {
          File included = new File(resourcePath, includedFile);
          if (!excluded.contains(included)) {
            files.add(included);
          }
        }
        resourcePaths.add(new File(resource.getDirectory()));
      }
    } else {
      File resourcePath = new File(cwd, "src/main/resources");
      if (resourcePath.exists() && resourcePath.isDirectory()) {
        collectSources(resourcePath, files);
        resourcePaths.add(resourcePath);
      }
    }
    if (sourcePaths.size() > 0 && files.size() > 0) {
      compile(sourcePaths, resourcePaths, files);
    }
  }

  private void collectSources(File dir, List<File> files) {
    File[] children = dir.listFiles();
    if (children != null) {
      for (File child : children) {
        if (child.isDirectory()) {
          collectSources(child, files);
        } else if (child.isFile() && (child.getName().endsWith(".ceylon") || child.getName().endsWith(".java"))) {
          files.add(child);
        }
      }
    }
  }

  private void compile(List<File> sourcePath, List<File> resourcePath, List<File> files) throws MojoExecutionException, MojoFailureException {
    CeylonConfig cfg = CeylonConfig.createFromLocalDir(cwd);
    Compiler compiler = new JavaScriptCompilerImpl();
    CompilerOptions options = CompilerOptions.fromConfig(cfg);
    options.setModules(Collections.<String>emptyList());
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
}
