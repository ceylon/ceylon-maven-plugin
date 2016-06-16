def log = new File(basedir, 'build.log').text
return log.contains('Module mymodule/1.0.0 not found')
