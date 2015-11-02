package com.redhat.ceylon.maven.tools;

import com.redhat.ceylon.compiler.java.runtime.tools.CompilerOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExtendedCompilerOptions extends CompilerOptions {

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
}
