def root = new File(basedir, 'target/modules/mymodule/1.0.0')
def classes = new File(basedir, 'target/classes/mymodule')
return new File(root, 'mymodule-1.0.0.car').exists() &&
    new File(root, 'mymodule-1.0.0.car.sha1').exists() &&
    new File(root, 'mymodule-1.0.0.src').exists() &&
    new File(root, 'mymodule-1.0.0.src.sha1').exists() &&
    new File(classes, '$module_.class').exists() && 
    new File(classes, '$package_.class').exists()
