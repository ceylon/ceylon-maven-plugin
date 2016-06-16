def log = new File(basedir, 'build.log').text
return log.contains('ceylon.language::Exception "the_error"')
