package com.redhat.ceylon.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "testCompile-js", defaultPhase = LifecyclePhase.TEST_COMPILE)
public class CeylonTestCompileJsMojo extends CeylonCompileJsMojo {

	@Override
	protected String getPhase() {
		return "test";
	}

	@Override
	protected boolean isTest() {
		return true;
	}
}
