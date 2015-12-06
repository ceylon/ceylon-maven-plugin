def log = new File(basedir, 'build.log').text
return log.contains('Compilation failed')
