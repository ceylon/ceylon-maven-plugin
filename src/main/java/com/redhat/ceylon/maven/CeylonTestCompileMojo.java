package com.redhat.ceylon.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "testCompile", defaultPhase = LifecyclePhase.TEST_COMPILE)
public class CeylonTestCompileMojo extends CeylonCompileMojo {

	@Override
	protected String getPhase() {
		return "test";
	}

	@Override
	protected String getClassesOutput() {
		return project.getBuild().getTestOutputDirectory();
	}
	
	@Override
	protected boolean isTest() {
		return true;
	}
}
