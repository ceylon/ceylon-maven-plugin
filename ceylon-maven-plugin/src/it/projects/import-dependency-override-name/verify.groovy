def root = new File(basedir, 'target/modules/foobar/1')
return new File(root, 'foobar-1.jar').exists() &&
    new File(root, 'foobar-1.jar.sha1').exists()
