def root = new File(basedir, 'target/modules/javax/inject/javax/inject/1.0.0')
return new File(root, 'javax.inject.javax.inject-1.0.0.jar').exists() &&
    new File(root, 'javax.inject.javax.inject-1.0.0.jar.sha1').exists()
