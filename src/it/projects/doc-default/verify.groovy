def root = new File(basedir, 'target/modules/mymodule/1.0.0/module-doc/api')
return new File(root, 'api-index.html').exists()