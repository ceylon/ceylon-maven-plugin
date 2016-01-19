def root = new File(basedir, 'target/modules/javax/portlet/portlet-api/1.0')
return new File(root, 'javax.portlet.portlet-api-1.0.jar').exists() &&
    new File(root, 'javax.portlet.portlet-api-1.0.jar.sha1').exists() &&
    !new File(root, 'javax.portlet.portlet-api-1.0-sources.jar').exists() &&
    !new File(root, 'javax.portlet.portlet-api-1.0-sources.jar.sha1').exists()
