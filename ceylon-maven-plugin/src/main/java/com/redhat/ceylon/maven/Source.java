package com.redhat.ceylon.maven;

import org.apache.maven.shared.model.fileset.FileSet;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Source {

  private FileSet fileset;

  public FileSet getFileset() {
    return fileset;
  }

  public void setFileset(FileSet fileset) {
    this.fileset = fileset;
  }
}
