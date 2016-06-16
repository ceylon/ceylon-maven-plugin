package com.redhat.ceylon.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.redhat.ceylon.common.ModuleUtil;
import com.redhat.ceylon.compiler.java.runtime.tools.*;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import com.redhat.ceylon.compiler.java.runtime.tools.impl.JavaCompilerImpl;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class CeylonCompileMojo extends AbstractMojo {

  @Parameter(readonly = true, property = "project.build.directory")
  private String buildDir;

  @Parameter(defaultValue = "false")
  private boolean verbose;

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

  @Parameter
  private String javacOptions;

  @Parameter
  private File explodeTo;

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
    Compiler compiler = new JavaCompilerImpl() {
        @Override
        protected List<String> translateOptions(CompilerOptions options) {
            List<String> translatedOptions = super.translateOptions(options);
            if (javacOptions != null) {
                Collections.addAll(translatedOptions, javacOptions.split("\\s+"));
            }
            return translatedOptions;
        }
    };
    JavaCompilerOptions options = new JavaCompilerOptions();
    options.setSourcePath(sourcePath);
    options.setResourcePath(resourcePath);
    if (cwd != null) {
        options.setWorkingDirectory(cwd.getAbsolutePath());
    }
    options.setOutputRepository(out);
    options.setVerbose(verbose);
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
        if (explodeTo != null) {
            explodeModule(module, version);
        }
      }
    });

    if (!ok) {
      throw new MojoExecutionException("Compilation failed");
    }
  }
  
    protected void explodeModule(String module, String version) {
        File fOut = new File(out);
        if (fOut.isDirectory()) {
            File path = new File(ModuleUtil.moduleToPath(fOut, module), version);
            File car = new File(path, module + "-" + version + ".car");
            unzip(car, explodeTo);
        }
    }

    private void unzip(File zip, File targetDir) {
      try {
          final ZipFile zipFile = new ZipFile(zip);
          try {
              final Enumeration<? extends ZipEntry> entries = zipFile.entries();
              while (entries.hasMoreElements()) {
                  final ZipEntry ze = entries.nextElement();
                  final File file = new File(targetDir, ze.getName());
                  if (ze.isDirectory()) {
                      if (file.mkdirs() == false)
                          throw new IllegalArgumentException("Cannot create dir: " + file);
                  } else {
                      final FileOutputStream fos = new FileOutputStream(file);
                      copyStream(zipFile.getInputStream(ze), fos);
                  }
              }
          } finally {
              zipFile.close();
          }
      } catch (IOException e) {
          throw new IllegalArgumentException(e);
      }
  }

  private static void copyStream(final InputStream in, final OutputStream out) throws IOException {
      final byte[] bytes = new byte[8192];
      int cnt;
      try {
          while ((cnt = in.read(bytes)) != -1) {
              out.write(bytes, 0, cnt);
          }
      } finally {
          safeClose(in);
          safeClose(out);
      }
  }

  private static void safeClose(Closeable c) {
      try {
          c.close();
      } catch (Exception ignored) {
      }
  }
}
