package com.redhat.ceylon.maven;

import org.apache.maven.model.Dependency;

import java.io.File;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DependencyImport {

  private Dependency dependency;
  private boolean force;
  private String module;
  private String version;
  private File descriptor;

  public Dependency getDependency() {
    return dependency;
  }

  public void setDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  public boolean getForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public File getDescriptor() {
    return descriptor;
  }

  public void setDescriptor(File descriptor) {
    this.descriptor = descriptor;
  }
}
