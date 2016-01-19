package com.redhat.ceylon.maven;

import org.apache.maven.model.Dependency;

import java.io.File;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ModuleImport {

  private Dependency dependency;
  private boolean force;
  private String name;
  private String version;
  private File descriptor;
  private boolean sources = true;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public boolean getSources() {
    return sources;
  }

  public void setSources(boolean sources) {
    this.sources = sources;
  }
}
