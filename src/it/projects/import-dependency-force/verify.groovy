def root = new File(basedir, 'target/modules/org/jboss/modules/jboss-modules/1.4.4.Final')
return new File(root, 'org.jboss.modules.jboss-modules-1.4.4.Final.jar').exists() &&
    new File(root, 'org.jboss.modules.jboss-modules-1.4.4.Final.jar.sha1').exists()
