def root = new File(basedir, 'target/modules/foobar/1.0.0/module-resources')
return new File(root, 'foobar/foo.txt').exists() &&
    new File(root, 'root.txt').exists()
