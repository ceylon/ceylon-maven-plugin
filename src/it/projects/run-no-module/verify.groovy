def log = new File(basedir, 'build.log').text
return log.contains('org.apache.maven.plugin.MojoExecutionException: Execution error')
