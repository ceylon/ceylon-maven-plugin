def root = new File(basedir, 'target/modules/foobar/1.0.0')
return new File(root, 'foobar-1.0.0.car').exists() &&
    new File(root, 'foobar-1.0.0.car.sha1').exists() &&
    new File(root, 'foobar-1.0.0.src').exists() &&
    new File(root, 'foobar-1.0.0.src.sha1').exists()
