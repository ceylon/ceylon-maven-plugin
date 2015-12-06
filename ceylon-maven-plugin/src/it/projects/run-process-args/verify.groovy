def log = new File(basedir, 'build.log').text
return log.contains('executed_run_args:[arg_0, arg_1, arg_2, arg_3]')
