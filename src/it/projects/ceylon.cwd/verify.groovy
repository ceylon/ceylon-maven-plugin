def log = new File(basedir, 'build.log').text
def root = new File(basedir, 'target/other/modules/mymodule/1.0.0/module-doc/api')
return log.contains('executed_ceylon.cwd') && new File(root, 'api-index.html').exists()
