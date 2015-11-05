def root = new File(basedir, 'target/modules/foobar/1.0.0')
def car = new File(root, 'foobar-1.0.0.car');
if (!car.exists()) {
    return false;
}
def entries = [] as Set
new java.util.zip.ZipFile(car).entries().each { entry ->
    entries << entry.name
}
return entries.contains("foobar/foo.txt") && entries.contains("root.txt")