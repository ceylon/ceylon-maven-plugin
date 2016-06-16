def root = new File(basedir, 'target/modules/foobar/1.0.0')
return new File(root, 'foobar-1.0.0.js').exists() &&
    new File(root, 'foobar-1.0.0-model.js').exists() &&
    new File(root, 'foobar-1.0.0-model.js.sha1').exists() &&
    new File(root, 'foobar-1.0.0.js.sha1').exists() &&
    new File(root, 'package.json').exists() &&
    new File(root, 'foobar-1.0.0.src').exists() &&
    new File(root, 'foobar-1.0.0.src.sha1').exists()
