def log = new File(basedir, 'build.log').text
return log.contains('Could not find module: mymodule/1.0.0')
