package com.redhat.ceylon.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;

import com.redhat.ceylon.common.Versions;
import com.redhat.ceylon.compiler.java.runtime.tools.Options;

public abstract class AbstractCeylonMojo extends AbstractMojo {
	
	@Parameter(readonly = true, property = "ceylon.home")
	protected String ceylonHome;

	@Parameter(readonly = true, property = "project.build.directory")
	protected String buildDir;

	@Parameter(readonly = true, property = "basedir")
	protected File cwd;

	@Parameter
	protected String verbose;

	@Parameter( defaultValue = "${project}", readonly = true, required = true )
	protected MavenProject project;

	@Component
	protected RepositorySystem repoSystem;

	@Parameter( defaultValue = "${repositorySystemSession}", readonly = true, required = true )
	protected RepositorySystemSession repoSession;

	@Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true )
	protected List<RemoteRepository> repositories;

	protected void exportDependencies() throws MojoExecutionException{
		System.err.println("Exporting deps ");
		addLanguageDeps();
		for (Artifact pomDependency : project.getDependencyArtifacts()) {
			System.err.println("Exporting "+pomDependency);
			// skip test deps
			if(pomDependency.getScope().equals(JavaScopes.TEST)){
				continue;
			}
			System.err.println("Exporting type "+pomDependency.getType());
			// skip non-jars
			if(!pomDependency.getType().equals("jar"))
				continue;
			exportDependency(pomDependency.getVersion(), pomDependency.getFile());
		}    	
	}

	private void exportDependency(String version, File file) {
		try(ZipFile zip = new ZipFile(file)){
			// find a Ceylon module descriptor
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if(name.endsWith("/$module_.class")){
					exportDependency(name.substring(0, name.length()-15).replace('/', '.'),
							version,
							file);
					return;
				}
				if(name.startsWith("META-INF/jbossmodules/")
						&& name.endsWith("/module.xml")){
					String moduleAndVersion = name.substring(22, name.length()-11);
					int sep = moduleAndVersion.lastIndexOf('/');
					String module = moduleAndVersion.substring(0, sep);
					exportDependency(module.replace('/', '.'),
							version,
							file);
					return;
				}
			}
			System.err.println("Not a Ceylon module: "+file);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addLanguageDeps() throws MojoExecutionException {
		addDep("org.ceylon-lang", "ceylon.language", Versions.CEYLON_VERSION_NUMBER);
	}
	private void addDep(String groupId, String artifactId, String version) throws MojoExecutionException {
		System.err.println("addDep "+groupId+":"+artifactId);
		org.eclipse.aether.artifact.Artifact aetherArtifact = new DefaultArtifact(
				groupId,
				artifactId,
				null,
				"jar",
				version);

		final org.eclipse.aether.graph.Dependency dependency = new org.eclipse.aether.graph.Dependency( aetherArtifact, JavaScopes.COMPILE );
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRepositories(this.repositories);
		collectRequest.setRoot( dependency );

		DependencyRequest dependencyRequest = new DependencyRequest();
		dependencyRequest.setCollectRequest(collectRequest);
		dependencyRequest.setFilter(new DependencyFilter(){
			@Override
			public boolean accept(DependencyNode dep, List<DependencyNode> parents) {
				return true;
			}
		});

		DependencyResult dependencyResult;
		try {
			dependencyResult = this.repoSystem.resolveDependencies(this.repoSession, dependencyRequest);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException( "Artifact could not be resolved.", e );
		}

		System.err.println("Got results: "+dependencyResult.getArtifactResults());
		for(ArtifactResult result : dependencyResult.getArtifactResults()){
			System.err.println("Got result: "+result);
			File file = result.getArtifact().getFile();
			if( file == null || ! file.exists()) {
				getLog().warn( "Artifact  has no attached file. Its content will not be copied in the target model directory." );
			}else{
				exportDependency(result.getArtifact().getVersion(), file);
			}
		}
	}

	private void exportDependency(String name, String version, File file) throws IOException {
		System.err.println("Exporting Ceylon module file: "+file);
		System.err.println("Exporting Ceylon module name: "+name);
		System.err.println("Exporting Ceylon module name: "+version);
		File outputRepo = new File(buildDir, "ceylon-exported");
		outputRepo.mkdirs();
		File outputFolder = new File(outputRepo, name.replace('.', '/') + "/" + version);
		outputFolder.mkdirs();
		File outputJar = new File(outputFolder, name+"-"+version+".car");
		Files.copy(file.toPath(), outputJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
		// FIXME: sha?
	}

	protected void addExportedUserRepository(Options options){
		File outputRepo = new File(buildDir, "ceylon-exported");
		if(outputRepo.isDirectory())
			options.addUserRepository(outputRepo.getPath());
	}
}
