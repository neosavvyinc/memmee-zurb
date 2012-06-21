function SecurityController($scope, securityService, $location) {
    $scope.loggedInUser = null;
    $scope.visibleLoggedInStyle = { visibility: 'hidden' };

    $scope.saveLoggedInUser = function( $user ) {
        $scope.loggedInUser = $user;
        $scope.visibleLoggedInStyle = { visibility: 'visible' };
    }

    $scope.logout = function() {
        $scope.visibleLoggedInStyle = { visibility: 'hidden' };
        securityService.logoutUser($scope.loggedInUser);
        $location.path('/home');
    }
}

function NavigationController($scope, securityService) {

    $scope.loggedOutNavigationItems = [
        { displayName: "Home", navigationLink: "#home", selected: "active" }
    ];

    $scope.loggedInNavigationItems = [
            { displayName: "Home", navigationLink: "#home", selected: "active"},
            { displayName: "View", navigationLink: "#view", selected: ""},
            { displayName: "New", navigationLink: "#new", selected: ""}
        ];

    $scope.navigationItems = $scope.loggedOutNavigationItems;

    $scope.profileSelected = "";

    $scope.select = function( $selectedNavigationLink ) {

        $scope.toggleProfile(false)
        for ( navIndex in $scope.navigationItems )
        {
            if( $scope.navigationItems[navIndex].navigationLink == $selectedNavigationLink.navigationLink )
            {
                $scope.navigationItems[navIndex].selected = "active";
            }
            else
            {
                $scope.navigationItems[navIndex].selected = "";
            }
        }
    }

    $scope.toggleProfile = function( $selected ) {

        if( $selected )
        {
            $scope.profileSelected = "active";
            for ( navIndex in $scope.navigationItems )
            {
                $scope.navigationItems[navIndex].selected = "";
            }
        }
        else
        {
            $scope.profileSelected = "";
        }
    }

    $scope.$on('handleLogin', function() {
        $scope.updateNavigation(true);
    });
    $scope.$on('handleLogout', function() {
        $scope.updateNavigation(false);
    });


    $scope.updateNavigation = function( loggedIn ) {

        if( loggedIn )
        {
            $scope.navigationItems = $scope.loggedInNavigationItems;
        }
        else
        {
            $scope.navigationItems = $scope.loggedOutNavigationItems;
        }

    }

}


function RegistrationController($scope, $http, securityService) {

    $scope.user = {
        email: ''
    };

    $scope.register = function()
    {
        $http({method: 'POST', url: '/memmeeuserrest/user', data: $scope.user}).
            success(function(data, status, headers, config) {
                console.log('you were successfully registered');
                $scope.saveLoggedInUser(data);
                securityService.loginUser(data);
            }).
            error(function(data, status, headers, config) {
                console.log('error while saving a new user');
            });
    }


}

function LoginController($scope, $http, securityService) {

    $scope.user = {
        email: '',
        password: ''
    };

    $scope.login = function()
    {
        $http({method: 'POST', url: '/memmeeuserrest/user/login', data: $scope.user}).
            success(function(data, status, headers, config) {
                console.log('you were successfully registered');
                $scope.saveLoggedInUser(data);
                securityService.loginUser(data);
            }).
            error(function(data, status, headers, config) {
                console.log('error while saving a new user');
            });
    }

}

function ProfileController($scope, $http, securityService) {

    $scope.user = securityService.user;
    $scope.confirmedPass = '';

    $scope.update = function()
    {
        if( $scope.confirmedPass != $scope.user.password )
        {
            console.log("your password doesn't match your provided password");
            return;
        }

        console.log("scope.user.id" + $scope.user.id);

        $http({method: 'PUT', url: '/memmeeuserrest/user/' + $scope.user.id, data: $scope.user}).
            success(function(data, status, headers, config) {
                console.log('your user has been updated')
                securityService.loginUser(data);
            }).
            error(function(data, status, headers, config) {
                console.log('error while saving your user');
            });
    }
}

function NewMemmeeController($scope, $http, securityService) {

    $scope.user = securityService.user;
    $scope.memmee = {
        id: '',
        userId: $scope.user.id,
        title: '',
        text: ''
    };
    $scope.attachment = {}


    $scope.createMemmee = function()
    {
        $http({method: 'POST', url: '/memmeerest/insertmemmee/?apiKey=' + $scope.user.apiKey, data: $scope.memmee}).
            success(function(data, status, headers, config) {
                console.log('you have saved a memmee');
            }).
            error(function(data, status, headers, config) {
                console.log('error while saving your user');
                console.log(data);
            });
    }
}

function ViewMemmeesController($scope, $http, securityService) {

    $scope.user = securityService.user;
    $scope.memmees = [
        {
            id: '',
            userId: '',
            title: 'test1',
            text: 'test1'
        },
        {
            id: '',
            userId: '',
            title: 'test2',
            text: 'test2'
        }];

    $http({method: 'GET', url: '/memmeerest/getmemmees/?apiKey=' + $scope.user.apiKey}).
        success(function(data, status, headers, config) {
            console.log('your memmees have been loaded')
            $scope.memmees = data;
        }).
        error(function(data, status, headers, config) {
            console.log('error loading your doggone memmees');
        });

}

ViewMemmeesController.$inject = ['$scope', '$http', 'memmeeSecurityService'];

NewMemmeeController.$inject = ['$scope', '$http', 'memmeeSecurityService'];

LoginController.$inject = ['$scope', '$http', 'memmeeSecurityService'];

ProfileController.$inject = ['$scope', '$http', 'memmeeSecurityService'];

SecurityController.$inject = ['$scope', 'memmeeSecurityService', '$location'];

NavigationController.$inject = ['$scope', 'memmeeSecurityService'];

RegistrationController.$inject = ['$scope', '$http', 'memmeeSecurityService'];