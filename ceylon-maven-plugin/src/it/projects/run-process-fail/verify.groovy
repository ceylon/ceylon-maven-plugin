def log = new File(basedir, 'build.log').text
return log.contains('Caused by: ceylon.language.Exception "the_error"')
