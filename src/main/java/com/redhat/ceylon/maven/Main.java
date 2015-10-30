package com.redhat.ceylon.maven;

import com.redhat.ceylon.common.tools.CeylonTool;
import com.redhat.ceylon.common.tools.ModuleSpec;
import com.redhat.ceylon.tools.importjar.CeylonImportJarTool;

import java.io.File;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Main {

  public static void main(String[] args) throws Exception {
    CeylonImportJarTool tool = new CeylonImportJarTool();

    tool.setDescriptor(new File("/Users/julien/java/vertx-lang-ceylon/descriptors/org.hamcrest.hamcrest-core.properties"));
    tool.setFile(new File("/Users/julien/java/vertx-lang-ceylon/target/jars/hamcrest-core-1.3.jar"));
    tool.setModuleSpec(new ModuleSpec("org.hamcrest.hamcrest-core", "1.3"));

    tool.initialize(new CeylonTool());

    tool.run();
  }
}
