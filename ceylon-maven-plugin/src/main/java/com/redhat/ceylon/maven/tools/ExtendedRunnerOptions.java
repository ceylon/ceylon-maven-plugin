package com.redhat.ceylon.maven.tools;

import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunnerOptions;

import java.io.File;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExtendedRunnerOptions extends JavaRunnerOptions {

  private File cwd;

  public File getCwd() {
    return cwd;
  }

  public void setCwd(File cwd) {
    this.cwd = cwd;
  }

}
