package com.redhat.ceylon.maven;

import com.redhat.ceylon.common.tools.CeylonTool;
import com.redhat.ceylon.common.tools.ModuleSpec;
import com.redhat.ceylon.tools.importjar.CeylonImportJarTool;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "import-dependency", defaultPhase = LifecyclePhase.NONE)
public class CeylonImportDependencyMojo extends AbstractMojo {

  @Parameter(required = true)
  protected DependencyImport[] imports;

  @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
  protected RepositorySystemSession repoSession;

  @Component
  protected RepositorySystem repoSystem;

  public void execute() throws MojoExecutionException, MojoFailureException {

    List<CeylonImportJarTool> tools = new ArrayList<CeylonImportJarTool>();

    // Prepare all imports
    for (DependencyImport dependencyImport : imports) {
      Dependency dependency = dependencyImport.getDependency();
      ArtifactResult result;
      try {
        Artifact artifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), dependency.getVersion());
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
      if (dependencyImport.getDescriptor() != null) {
        tool.setDescriptor(dependencyImport.getDescriptor());
      }
      if (dependencyImport.getForce()) {
        tool.setForce(true);
      }
      tool.setFile(result.getArtifact().getFile());
      tool.setModuleSpec(new ModuleSpec(dependencyImport.getModule(), dependencyImport.getVersion()));
      tools.add(tool);
    }

    // Now process everything
    for (int i = 0;i < tools.size();i++) {
      CeylonImportJarTool tool = tools.get(i);
      try {
        tool.initialize(new CeylonTool());
        tool.run();
      } catch (Exception e) {
        MojoExecutionException ex = new MojoExecutionException("Cannot import dependency " + imports[i].getModule() + "/" + imports[i].getVersion());
        ex.initCause(e);
        throw ex;
      }
    }
  }
}
