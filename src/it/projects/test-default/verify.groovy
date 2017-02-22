def log = new File(basedir, 'target/surefire-reports/TEST-test.mymodule.xml').text
return log.contains('tests="1" errors="0"')
