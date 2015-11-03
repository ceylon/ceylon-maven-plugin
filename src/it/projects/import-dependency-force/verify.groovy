def root = new File(basedir, 'target/modules/io/netty/netty-codec/4.0.31.Final')
return new File(root, 'io.netty.netty-codec-4.0.31.Final.jar').exists() &&
    new File(root, 'io.netty.netty-codec-4.0.31.Final.jar.sha1').exists()