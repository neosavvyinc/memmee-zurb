// Sample Testacular configuration file, that contain pretty much all the available options
// It's used for running client tests on Travis (http://travis-ci.org/#!/vojtajina/testacular)
// Most of the options can be overriden by cli arguments (see testacular --help)



// base path, that will be used to resolve files and exclude
basePath = '../../'

// list of files / patterns to load in the browser
files = [

    ANGULAR_SCENARIO,
    ANGULAR_SCENARIO_ADAPTER,

    'test/resources/lib/angular-mocks.js',
    'test/resources/e2e/*.js'
];

// list of files to exclude
exclude = [];

// use dots reporter, as travis terminal does not support escaping sequences
// possible values: 'dots' || 'progress'
reporter = 'progress';

// web server port
port = 9876;

// cli runner port
runnerPort = 9100;

// enable / disable colors in the output (reporters and logs)
colors = true;

// level of logging
// possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
logLevel = LOG_WARN;

// enable / disable watching file and executing tests whenever any file changes
autoWatch = true;

// polling interval in ms (ignored on OS that support inotify)
//autoWatchInterval = 100;

// Start these browsers, currently available:
// - Chrome
// - ChromeCanary
// - Firefox
// - Opera
// - Safari
browsers = ['ChromeCanary'];
//browsers = ['Chrome', 'Firefox', 'Safari', 'PhantomJS'];

// Auto run tests on start (when browsers are captured) and exit
singleRun = false;

urlRoot = '/__testacular/';

proxies = {
    '/': 'http://local.memmee.com/'
};