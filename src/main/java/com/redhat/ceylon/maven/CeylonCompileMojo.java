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

  @Parameter(property = "ceylon.cwd", defaultValue = "${project.build.directory}")
  private File cwd;

  @Parameter(defaultValue = "modules")
  private String out;

  @Parameter()
  private List sources;

  @Parameter()
  private List resources;

  @Parameter
  private String[] userRepos;

  @Parameter
  private String javacOptions;

  public void execute() throws MojoExecutionException, MojoFailureException {
    ArrayList<File> files = new ArrayList<>();
    ArrayList<File> sourcePaths = new ArrayList<>();
    ArrayList<File> resourcePaths = new ArrayList<>();
    if (this.sources != null) {
      FileSetManager fileSetManager = new FileSetManager();
      for (Source source : (List<Source>)this.sources) {
        File sourcePath = new File(source.getFileset().getDirectory());
        Set<File> excluded = new HashSet<>();
        for (String excludedFile : fileSetManager.getExcludedFiles(source.getFileset())) {
          excluded.add(new File(sourcePath, excludedFile));
        }
        for (String includedFile : fileSetManager.getIncludedFiles(source.getFileset())) {
          File included = new File(sourcePath, includedFile);
          if (!excluded.contains(included)) {
            files.add(included);
          }
        }
        sourcePaths.add(new File(source.getFileset().getDirectory()));
      }
    } else {
      File sourcePath = new File("src/main/ceylon");
      if (sourcePath.exists() && sourcePath.isDirectory()) {
        collectSources(sourcePath, files);
        sourcePaths.add(sourcePath);
      }
    }
    if (this.resources != null) {
      FileSetManager fileSetManager = new FileSetManager();
      for (Resource resource : (List<Resource>)this.resources) {
        File resourcePath = new File(resource.getFileset().getDirectory());
        Set<File> excluded = new HashSet<>();
        for (String excludedFile : fileSetManager.getExcludedFiles(resource.getFileset())) {
          excluded.add(new File(resourcePath, excludedFile));
        }
        for (String includedFile : fileSetManager.getIncludedFiles(resource.getFileset())) {
          File included = new File(resourcePath, includedFile);
          if (!excluded.contains(included)) {
            files.add(included);
          }
        }
        resourcePaths.add(new File(resource.getFileset().getDirectory()));
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
    Compiler compiler = new JavaCompilerImpl();
    ExtendedCompilerOptions options = new ExtendedCompilerOptions();
    options.setSourcePath(sourcePath);
    options.setResourcePath(resourcePath);
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
