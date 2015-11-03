package com.redhat.ceylon.maven;

import com.redhat.ceylon.common.tools.CeylonTool;
import com.redhat.ceylon.common.tools.ModuleSpec;
import com.redhat.ceylon.tools.importjar.CeylonImportJarTool;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "import-dependency", defaultPhase = LifecyclePhase.INITIALIZE)
public class CeylonImportDependencyMojo extends AbstractMojo {

  @Parameter(required = true)
  protected ModuleImport[] moduleImports;

  @Parameter(property = "ceylon.cwd", defaultValue = "${project.build.directory}")
  private File cwd;

  @Parameter(defaultValue = "modules")
  private String out;

  @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
  protected RepositorySystemSession repoSession;

  @Parameter(defaultValue = "${project}", readonly = true)
  public MavenProject project;

  @Component
  protected RepositorySystem repoSystem;

  public void execute() throws MojoExecutionException, MojoFailureException {

    DependencyManagement dependencyManagement = project.getDependencyManagement();

    List<CeylonImportJarTool> tools = new ArrayList<CeylonImportJarTool>();
    List<ModuleSpec> moduleSpecs = new ArrayList<ModuleSpec>();

    // Prepare all imports
    for (ModuleImport moduleImport : moduleImports) {
      Dependency dependency = moduleImport.getDependency();
      ArtifactResult result;
      String dependencyVersion = dependency.getVersion();

      // Not provided => get from dependency management
      if (dependencyVersion == null) {
        for (Dependency managed : dependencyManagement.getDependencies()) {
          if (Objects.equals(managed.getGroupId(), dependency.getGroupId()) &&
              Objects.equals(managed.getArtifactId(), dependency.getArtifactId()) &&
              Objects.equals(managed.getClassifier(), dependency.getClassifier()) &&
              Objects.equals(managed.getType(), dependency.getType())) {
            dependencyVersion = managed.getVersion();
          }
        }
      }

      try {
        Artifact artifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), dependencyVersion);
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(Collections.<RemoteRepository>emptyList());
        result = repoSystem.resolveArtifact(repoSession, request);
      } catch (Exception e) {
        MojoExecutionException ex = new MojoExecutionException("Cannot resolve dependency");
        ex.initCause(e);
        throw ex;
      }
      CeylonImportJarTool tool = new CeylonImportJarTool();
      if (moduleImport.getDescriptor() != null) {
        tool.setDescriptor(moduleImport.getDescriptor());
      }
      if (moduleImport.getForce()) {
        tool.setForce(true);
      }
      tool.setCwd(cwd);
      tool.setOut(out);
      tool.setFile(result.getArtifact().getFile());

      //
      String moduleName = moduleImport.getName();
      String moduleVersion = moduleImport.getVersion();
      if (moduleName == null) {
        moduleName = dependency.getGroupId() + "." + dependency.getArtifactId();
      }
      if (moduleVersion == null) {
        moduleVersion = dependencyVersion;
      }
      ModuleSpec moduleSpec = new ModuleSpec(moduleName, moduleVersion);
      moduleSpecs.add(moduleSpec);
      tool.setModuleSpec(moduleSpec);
      tools.add(tool);
    }

    // Now process everything
    for (int i = 0;i < tools.size();i++) {
      CeylonImportJarTool tool = tools.get(i);
      try {
        tool.initialize(new CeylonTool());
        tool.run();
      } catch (Exception e) {
        MojoExecutionException ex = new MojoExecutionException("Cannot import module " + moduleSpecs.get(i));
        ex.initCause(e);
        throw ex;
      }
    }
  }
}
