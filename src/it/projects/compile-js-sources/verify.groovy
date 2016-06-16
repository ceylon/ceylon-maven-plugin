def foo = new File(basedir, 'target/modules/foo/1.0.0')
def bar = new File(basedir, 'target/modules/bar/1.0.0')
return new File(foo, 'foo-1.0.0.js').exists() &&
    new File(foo, 'foo-1.0.0.js.sha1').exists() &&
    new File(foo, 'foo-1.0.0-model.js').exists() &&
    new File(foo, 'foo-1.0.0-model.js.sha1').exists() &&
    new File(foo, 'package.json').exists() &&
    new File(foo, 'foo-1.0.0.src').exists() &&
    new File(foo, 'foo-1.0.0.src.sha1').exists() &&
    new File(bar, 'bar-1.0.0.js').exists() &&
    new File(bar, 'bar-1.0.0.js.sha1').exists() &&
    new File(bar, 'bar-1.0.0-model.js').exists() &&
    new File(bar, 'bar-1.0.0-model.js.sha1').exists() &&
    new File(bar, 'package.json').exists() &&
    new File(bar, 'bar-1.0.0.src').exists() &&
    new File(bar, 'bar-1.0.0.src.sha1').exists()
