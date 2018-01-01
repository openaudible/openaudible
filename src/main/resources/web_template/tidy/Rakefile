desc "Check code quality"
task :jshint do
  test_file = File.expand_path('tidy-table.js')
  system("jshint file://#{test_file}")
end

desc "Run test suite"
task :qunit do
  test_file = File.expand_path('test.html')
  system("phantomjs test/run-qunit.js file://#{test_file}")
end

task :default => [:jshint, :qunit]
