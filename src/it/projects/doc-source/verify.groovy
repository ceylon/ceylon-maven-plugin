def root = new File(basedir, 'target/modules/foobar/1.0.0/module-doc/api')
return new File(root, 'api-index.html').exists()