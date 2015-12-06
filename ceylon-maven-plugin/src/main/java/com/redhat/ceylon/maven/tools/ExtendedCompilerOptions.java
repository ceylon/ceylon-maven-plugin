package com.redhat.ceylon.maven.tools;

import com.redhat.ceylon.compiler.java.runtime.tools.CompilerOptions;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExtendedCompilerOptions extends CompilerOptions {

  private List<File> resourcePath = new LinkedList<>();
  private String javacOptions;
  private String cwd;

  public String getCwd() {
    return cwd;
  }

  public void setCwd(String cwd) {
    this.cwd = cwd;
  }

  public String getJavacOptions() {
    return javacOptions;
  }

  public void setJavacOptions(String javacOptions) {
    this.javacOptions = javacOptions;
  }

  public List<File> getResourcePath() {
    return this.resourcePath;
  }

  public void setResourcePath(List<File> resourcePath) {
    this.resourcePath = resourcePath;
  }

  public void addResourcePath(File resourcePath) {
    this.resourcePath.add(resourcePath);
  }
}
