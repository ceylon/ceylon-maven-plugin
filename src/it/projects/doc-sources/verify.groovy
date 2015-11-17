def root = new File(basedir, 'target/modules')
return new File(root, 'foo/1.0.0/module-doc/api/api-index.html').exists() &&
       new File(root, 'bar/1.0.0/module-doc/api/api-index.html').exists()