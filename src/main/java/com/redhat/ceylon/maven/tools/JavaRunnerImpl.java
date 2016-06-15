package com.redhat.ceylon.maven.tools;

import com.redhat.ceylon.cmr.api.RepositoryManager;
import com.redhat.ceylon.cmr.ceylon.CeylonUtils;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunner;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunnerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.RunnerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.impl.CmrLogger;
import com.redhat.ceylon.module.loader.BaseModuleLoaderImpl;
import com.redhat.ceylon.module.loader.FlatpathModuleLoader;
import com.redhat.ceylon.module.loader.ModuleNotFoundException;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JavaRunnerImpl implements JavaRunner {
  private String module;

  private BaseModuleLoaderImpl moduleLoader;
  private ClassLoader moduleClassLoader;
  private String className;

  public JavaRunnerImpl(RunnerOptions options, String module, String version) throws ModuleNotFoundException{
    this.module = module;

    ExtendedRunnerOptions extOptions = (ExtendedRunnerOptions) options;

    RepositoryManager repositoryManager = CeylonUtils.repoManager()
        .cwd(extOptions.getCwd())
        .userRepos(options.getUserRepositories())
        .systemRepo(options.getSystemRepository())
        .offline(options.isOffline())
        .noDefaultRepos(options.isNoDefaultRepositories())
        .overrides(options.getOverrides())
        .logger(new CmrLogger(options.isVerbose("cmr")))
        .buildManager();

    ClassLoader delegateClassLoader = null;
    if(options instanceof JavaRunnerOptions){
      delegateClassLoader = ((JavaRunnerOptions) options).getDelegateClassLoader();
    }

    moduleLoader = new FlatpathModuleLoader(repositoryManager, delegateClassLoader, options.getExtraModules(), options.isVerbose("cmr"));
    moduleClassLoader = moduleLoader.loadModule(module, version);

    if(options.getRun() != null)
      className = options.getRun().replace("::", ".");
    else if(module.equals(com.redhat.ceylon.model.typechecker.model.Module.DEFAULT_MODULE_NAME))
      className = "run_";
    else
      className = module + ".run_";
  }

  public void run(String... arguments){
    if(moduleClassLoader == null)
      throw new ceylon.language.AssertionError("Cannot call run method after cleanup is called");
    // now load and invoke the main class
    invokeMain(module, arguments);
  }

  public ClassLoader getModuleClassLoader() {
    if(moduleClassLoader == null)
      throw new ceylon.language.AssertionError("Cannot get class loader after cleanup is called");
    return moduleClassLoader;
  }

  public void cleanup() {
    moduleLoader.cleanup();
    moduleLoader = null;
    moduleClassLoader = null;
  }

  // for tests
  public URL[] getClassLoaderURLs() {
    return moduleLoader.getClassLoaderURLs(module);
  }

  private void invokeMain(String module, String[] arguments) {
    try {
      Class<?> runClass = moduleClassLoader.loadClass(className);
      Method main = runClass.getMethod("main", String[].class);
      Thread currentThread = Thread.currentThread();
      ClassLoader oldCcl = currentThread.getContextClassLoader();
      try{
        currentThread.setContextClassLoader(moduleClassLoader);
        main.invoke(null, (Object)arguments);
      }finally{
        currentThread.setContextClassLoader(oldCcl);
      }

    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot find main class for module "+module+": "+className, e);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke main method for module "+module+": "+className, e);
    }
  }

}