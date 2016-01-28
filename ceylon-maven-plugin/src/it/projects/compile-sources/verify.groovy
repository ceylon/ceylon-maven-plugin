def foo = new File(basedir, 'target/modules/foo/1.0.0')
def bar = new File(basedir, 'target/modules/bar/1.0.0')
return new File(foo, 'foo-1.0.0.car').exists() &&
    new File(foo, 'foo-1.0.0.car.sha1').exists() &&
    new File(foo, 'foo-1.0.0.src').exists() &&
    new File(foo, 'foo-1.0.0.src.sha1').exists() &&
    new File(bar, 'bar-1.0.0.car').exists() &&
    new File(bar, 'bar-1.0.0.car.sha1').exists() &&
    new File(bar, 'bar-1.0.0.src').exists() &&
    new File(bar, 'bar-1.0.0.src.sha1').exists()
