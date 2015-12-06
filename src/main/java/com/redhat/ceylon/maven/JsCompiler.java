package com.redhat.ceylon.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.redhat.ceylon.cmr.api.RepositoryManager;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilationListener;
import com.redhat.ceylon.compiler.js.CeylonCompileJsTool;
import com.redhat.ceylon.maven.tools.ExtendedCompilerOptions;

public class JsCompiler {
	private RepositoryManager rm;

	public boolean compile(final ExtendedCompilerOptions options, CompilationListener listener) {
		CeylonCompileJsTool jsCompileTool = new CeylonCompileJsTool();
		
		
		jsCompileTool.setSource(options.getSourcePath());
		
		
		jsCompileTool.setModule(findJsModulesInDir(options.getSourcePath()));
		jsCompileTool.setSource(options.getSourcePath());
		jsCompileTool.setResource(options.getResourcePath());
		jsCompileTool.setCwd(new File(options.getCwd()));
		jsCompileTool.setOut(options.getOutputRepository());
		jsCompileTool.setVerbose(options.getVerboseCategory());
		
		return false;
	}
	

	
	private List<String> findJsModulesInDir(List<File> dirs) {
		ArrayList<String> modules = new ArrayList<>();
		
		for(File dir : dirs) {
			for(File file : dir.listFiles()) {
				if(file.getName().equals("module.xml"));
				
			}
			File[] moduleDescriptors = dir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.equals("module.ceylon");
				}
			});
			for(File moduleDescriptor : moduleDescriptors) {
				BufferedReader bufferedReader;
				try {
					bufferedReader = new BufferedReader(new FileReader(moduleDescriptor));
		
					String line;
					StringBuilder builder = new StringBuilder();
					while((line = bufferedReader.readLine()) != null) {
						builder.append(line);
					}
					String moduleContent = builder.toString();
					String nat = moduleContent.substring(0, moduleContent.indexOf("module")).trim();
					if(!nat.contains("native") || nat.contains("\"js\"")) {
						modules.add(moduleContent.split("module | \"")[1].trim());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return modules;
	}
	
}