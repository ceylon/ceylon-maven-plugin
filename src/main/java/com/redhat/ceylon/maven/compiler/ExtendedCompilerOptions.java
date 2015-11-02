package com.redhat.ceylon.maven.compiler;

import com.redhat.ceylon.compiler.java.runtime.tools.CompilerOptions;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExtendedCompilerOptions extends CompilerOptions {

  private String javacOptions;

  public String getJavacOptions() {
    return javacOptions;
  }

  public void setJavacOptions(String javacOptions) {
    this.javacOptions = javacOptions;
  }
}
