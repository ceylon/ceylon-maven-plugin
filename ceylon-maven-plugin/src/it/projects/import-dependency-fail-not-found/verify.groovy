def log = new File(basedir, 'build.log').text
return log.contains('Could not find artifact foobar:notfound:jar:1')
