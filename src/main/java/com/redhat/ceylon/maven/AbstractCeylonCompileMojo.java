package com.redhat.ceylon.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.redhat.ceylon.common.Backend;
import com.redhat.ceylon.common.tools.ModuleWildcardsHelper;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class AbstractCeylonCompileMojo extends AbstractCeylonMojo {

  @Parameter(defaultValue = "${project.build.directory}/modules")
  protected String out;

  @Parameter()
  protected List<FileSet> sources;

  @Parameter()
  protected List<FileSet> resources;

  @Parameter
  protected String[] userRepos;

  public void execute() throws MojoExecutionException, MojoFailureException {
	  ArrayList<File> files = new ArrayList<>();
	  ArrayList<File> sourcePaths = new ArrayList<>();
	  ArrayList<File> resourcePaths = new ArrayList<>();
	  List<String> modules = new ArrayList<>();
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
		  File sourcePath = new File(cwd, "src/"+getPhase()+"/ceylon");
		  if (sourcePath.exists() && sourcePath.isDirectory()) {
			  sourcePaths.add(sourcePath);
		  }
		  ModuleWildcardsHelper.expandWildcard(modules, sourcePaths, "*", getBackend());
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
		  File resourcePath = new File(cwd, "src/"+getPhase()+"/resources");
		  if (resourcePath.exists() && resourcePath.isDirectory()) {
			  resourcePaths.add(resourcePath);
		  }
	  }
	  if (sourcePaths.size() > 0 && (files.size() > 0 || modules.size() > 0)) {
		  compile(sourcePaths, resourcePaths, files, modules);
	  }
  }

  protected abstract Backend getBackend();

  protected String getPhase() {
	return "main";
  }

  protected abstract void compile(List<File> sourcePath, List<File> resourcePath, List<File> files, List<String> modules) 
		  throws MojoExecutionException, MojoFailureException;
}
