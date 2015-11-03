def log = new File(basedir, 'build.log').text
return log.contains('executed_run_default')
