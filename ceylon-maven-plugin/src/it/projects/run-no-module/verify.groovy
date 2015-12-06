def log = new File(basedir, 'build.log').text
return log.contains('Missing module: mymodule/1.0.0')
