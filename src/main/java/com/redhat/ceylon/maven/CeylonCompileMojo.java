package com.redhat.ceylon.maven;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.util.artifact.JavaScopes;

import com.redhat.ceylon.common.FileUtil;
import com.redhat.ceylon.common.ModuleUtil;
import com.redhat.ceylon.common.Versions;
import com.redhat.ceylon.common.config.CeylonConfig;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilationListener;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaCompilerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.impl.JavaCompilerImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class CeylonCompileMojo extends AbstractCeylonMojo {

  @Parameter(defaultValue = "${project.build.directory}/modules")
  private String out;

  @Parameter()
  private List<FileSet> sources;

  @Parameter()
  private List<FileSet> resources;

  @Parameter
  private String[] userRepos;

  @Parameter
  private boolean disablePomChecks;

  @Parameter
  private boolean flatClasspath;

  @Parameter
  private boolean autoExportMavenDependencies;

  @Parameter
  private boolean fullyExportMavenDependencies;

  @Parameter
  private String jdkProvider;

  @Parameter
  private List<String> aptModules;
  
  @Parameter
  private String javacOptions;

  @Parameter
  private File explodeTo;

  @Parameter
  private boolean explode;

  public void execute() throws MojoExecutionException, MojoFailureException {
    ArrayList<File> files = new ArrayList<>();
    ArrayList<File> sourcePaths = new ArrayList<>();
    ArrayList<File> resourcePaths = new ArrayList<>();
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
        collectSources(sourcePath, files);
        sourcePaths.add(sourcePath);
      }
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
        collectSources(resourcePath, files);
        resourcePaths.add(resourcePath);
      }
    }
    if (sourcePaths.size() > 0 && files.size() > 0) {
      compile(sourcePaths, resourcePaths, files);
    }
  }

  protected String getPhase() {
	return "main";
  }

  private void collectSources(File dir, List<File> files) {
    File[] children = dir.listFiles();
    if (children != null) {
      for (File child : children) {
        if (child.isDirectory()) {
          collectSources(child, files);
        } else if (child.isFile() && (child.getName().endsWith(".ceylon") || child.getName().endsWith(".java"))) {
          files.add(child);
        }
      }
    }
  }

  private void compile(List<File> sourcePath, List<File> resourcePath, List<File> files) throws MojoExecutionException, MojoFailureException {
	exportDependencies();
    Compiler compiler = new JavaCompilerImpl() {
        @Override
        protected List<String> translateOptions(CompilerOptions options) {
            List<String> translatedOptions = super.translateOptions(options);
            if (javacOptions != null) {
                Collections.addAll(translatedOptions, javacOptions.split("\\s+"));
            }
            // Temporary until 1.3.2 is released
            Collections.addAll(translatedOptions, "-source", getDefaultTarget().toString());
            return translatedOptions;
        }
    };
    CeylonConfig cfg = CeylonConfig.createFromLocalDir(cwd);
    JavaCompilerOptions options = JavaCompilerOptions.fromConfig(cfg);
    options.setModules(Collections.<String>emptyList());
    options.setJavacTarget(getDefaultTarget());
    options.setSourcePath(sourcePath);
    options.setResourcePath(resourcePath);
    if (cwd != null) {
        options.setWorkingDirectory(cwd.getAbsolutePath());
    }
    options.setOutputRepository(out);
    options.setFlatClasspath(flatClasspath);
    options.setAutoExportMavenDependencies(autoExportMavenDependencies);
    options.setFullyExportMavenDependencies(fullyExportMavenDependencies);
    options.setJdkProvider(jdkProvider);
    if (aptModules != null) {
        options.setAptModules(aptModules);
    }
    if (verbose != null) {
      options.setVerbose(true);
      if (!"true".equals(verbose)) {
        options.setVerboseCategory(verbose);
      }
    }
    if (userRepos != null) {
      for (String userRepo : userRepos) {
        options.addUserRepository(userRepo);
      }
    } else {
      options.addUserRepository(buildDir + "/modules");
    }
    addExportedUserRepository(options);
    if(ceylonHome != null)
    	options.setSystemRepository(ceylonHome + "/repo");
    options.setFiles(files);
    final MojoExecutionException[] x = new MojoExecutionException[1];
    boolean ok = compiler.compile(options, new CompilationListener() {

      public void error(File file, long line, long column, String message) {
        String msg;
        if (file != null) {
          msg = "Compilation error at (" + line + "," + column + ") in " +
              file.getAbsolutePath() + ":" + message;
        } else {
          msg = "Compilation error:" + message;
        }
        getLog().error(msg);
      }

      public void warning(File file, long line, long column, String message) {
        String msg;
        if (file != null) {
          msg = "Compilation warning at (" + line + "," + column + ") in " +
              file.getAbsolutePath() + ":" + message;
        } else {
          msg = "Compilation warning:" + message;
        }
        getLog().warn(msg);
      }

      public void moduleCompiled(String module, String version) {
        getLog().info("Compiled module " + module + "/" + version);
        if(explode){
        	explodeModule(module, version, new File(getClassesOutput()));
        }else if (explodeTo != null) {
            explodeModule(module, version, explodeTo);
        }
        try {
        	if(!disablePomChecks && !isTest())
        		checkDependencies(module, version);
		} catch (MojoExecutionException e) {
			x[0] = e;
		}
      }
    });

    if(x[0] != null)
    	throw x[0];
    if (!ok) {
      throw new MojoExecutionException("Compilation failed");
    }
  }
  
  protected boolean isTest() {
	return false;
  }

  protected String getClassesOutput() {
	return project.getBuild().getOutputDirectory();
  }

  private static Long getDefaultTarget() {
      String dottedVersion = System.getProperty("java.version");
      return Long.parseLong(dottedVersion.split("\\.|_|-")[1]);
  }
  
    protected void explodeModule(String module, String version, File explodeTo) {
        File fOut = new File(out);
        if (fOut.isDirectory()) {
            File path = new File(ModuleUtil.moduleToPath(fOut, module), version);
            File car = new File(path, module + "-" + version + ".car");
            unzip(car, FileUtil.applyCwd(cwd, explodeTo));
        }
    }
    
    private void checkDependencies(String module, String version) throws MojoExecutionException{
      File fOut = new File(out);
      if (fOut.isDirectory()) {
          File path = new File(ModuleUtil.moduleToPath(fOut, module), version);
          File car = new File(path, module + "-" + version + ".car");
      	  MavenXpp3Reader reader = new MavenXpp3Reader();
      	  try(ZipFile zipFile = new ZipFile(car)){
      		  String groupId = project.getGroupId();
      		  String artifactId = project.getArtifactId();
      		  ZipEntry entry = zipFile.getEntry("META-INF/maven/"+groupId+"/"+artifactId+"/pom.xml");
      		  if(entry == null){
      	        throw new MojoExecutionException("Maven descriptor missing in Ceylon module "+car
      	        		+": perhaps you did not set group/artifact to "+groupId+":"+artifactId+"?");
      		  }
      		  try(InputStream is = zipFile.getInputStream(entry)){
      			  Model model = reader.read(is);
      			  compareDependencies(model);
      		  } catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      	  } catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
    }

	private void compareDependencies(Model model) throws MojoExecutionException {
    	Set<Dependency> projectDependencies = new HashSet<>(project.getDependencies());
    	Set<Dependency> augmentedModelDependencies = new HashSet<>(model.getDependencies());
    	Dependency languageDep = new Dependency();
    	languageDep.setGroupId("org.ceylon-lang");
    	languageDep.setArtifactId("ceylon.language");
    	languageDep.setVersion(Versions.CEYLON_VERSION_NUMBER);
    	augmentedModelDependencies.add(languageDep);
    	Set<Dependency> modelDependencies = new HashSet<>(augmentedModelDependencies);
    	OUTER:
    	for (Dependency pomDependency : project.getDependencies()) {
    		// skip test deps
    		if(pomDependency.getScope().equals(JavaScopes.TEST)){
    			projectDependencies.remove(pomDependency);
    			continue;
    		}
    		String pomGroupId = pomDependency.getGroupId();
    		String pomArtifactId = pomDependency.getArtifactId();
    		String pomVersion = pomDependency.getVersion();
			for (Dependency modelDependency : augmentedModelDependencies) {
	    		String modelGroupId = modelDependency.getGroupId();
	    		String modelArtifactId = modelDependency.getArtifactId();
	    		// FIXME: workaround for ceylon bug
	    		int colon = modelArtifactId.indexOf(':');
	    		if(colon != -1){
	    			modelGroupId += "."+modelArtifactId.substring(0, colon);
	    			modelArtifactId = modelArtifactId.substring(colon+1);
	    		}
	    		String modelVersion = modelDependency.getVersion();
	    		if(pomGroupId.equals(modelGroupId)
	    				&& pomArtifactId.equals(modelArtifactId)
	    				&& pomVersion.equals(modelVersion)){
	    			// we found a match, let's remove both and move to the next
	    			projectDependencies.remove(pomDependency);
	    			modelDependencies.remove(modelDependency);
	    			continue OUTER;
	    		}
			}
		}
    	if(projectDependencies.isEmpty()
    			&& modelDependencies.isEmpty())
    		return; // OK
    	
    	StringBuilder sb = new StringBuilder();
    	// At this point, the sets are left with the differences where each are errors
    	if(!projectDependencies.isEmpty()){
    		sb.append("pom.xml dependencies missing from module.ceylon descriptor: ");
    		boolean once = true;
    		for (Dependency projectDependency : projectDependencies) {
    			if(once)
    				once = false;
    			else
    				sb.append(", ");
    			sb.append(projectDependency.getGroupId()+":"+projectDependency.getArtifactId()+"/"+projectDependency.getVersion());
    		}
    		sb.append(".");
        	if(!modelDependencies.isEmpty())
        		sb.append(" ");
    	}
    	if(!modelDependencies.isEmpty()){
    		sb.append("module.ceylon dependencies missing from pom.xml descriptor: ");
    		boolean once = true;
    		for (Dependency modelDependency : modelDependencies) {
    			if(once)
    				once = false;
    			else
    				sb.append(", ");
    			sb.append(modelDependency.getGroupId()+":"+modelDependency.getArtifactId()+"/"+modelDependency.getVersion());
    		}
    	}
        throw new MojoExecutionException("Descriptors mismatch: "+sb.toString());
	}

	private void unzip(File zip, File targetDir) {
      try {
          final ZipFile zipFile = new ZipFile(zip);
          try {
              final Enumeration<? extends ZipEntry> entries = zipFile.entries();
              while (entries.hasMoreElements()) {
                  final ZipEntry ze = entries.nextElement();
                  final File file = new File(targetDir, ze.getName());
                  if (ze.isDirectory()) {
                      if (!file.exists() && file.mkdirs() == false)
                          throw new IllegalArgumentException("Cannot create dir: " + file);
                  } else {
                      final FileOutputStream fos = new FileOutputStream(file);
                      copyStream(zipFile.getInputStream(ze), fos);
                  }
              }
          } finally {
              zipFile.close();
          }
      } catch (IOException e) {
          throw new IllegalArgumentException(e);
      }
  }

  private static void copyStream(final InputStream in, final OutputStream out) throws IOException {
      final byte[] bytes = new byte[8192];
      int cnt;
      try {
          while ((cnt = in.read(bytes)) != -1) {
              out.write(bytes, 0, cnt);
          }
      } finally {
          safeClose(in);
          safeClose(out);
      }
  }

  private static void safeClose(Closeable c) {
      try {
          c.close();
      } catch (Exception ignored) {
      }
  }
}
