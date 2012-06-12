function UserController($scope, $resource) {

    $scope.user = {
        firstName: '',
        lastName: '',
        pass: '',
        email: ''
    };

    var User = $resource('/memmeerest/user/:userId',
        null,
        {'remove': {method:'DELETE', params: {userId: '@id'}, isArray:false},
         'update': {method:'PUT', params: {userId: '@id'}, isArray:false}
          } );

    $scope.users = {};

    var self = $scope;
    self.loadUsers = function() {

        $scope.users = User.query();

    }

    self.saveUser = function()
    {
        if( $scope.user.id == null )
        {
            $scope.user.id = 1;
            User.save( $scope.user, function(){
                $scope.loadUsers();
                $scope.newUser();
            }, function() {
                console.log('error');
            } );
        }
        else
        {
            User.update({userId: $scope.user.id}, $scope.user, function(){


                $scope.loadUsers();
            }, function() {
                console.log('error');
            } );
        }
    }

    self.editUser = function( user )
    {
        $scope.user = user;
        console.log("user.id=" + user.id);
        console.log("user=" + user);
    }

    self.deleteUser = function( )
    {
        User.delete({userId:$scope.user.id}, function() {
            $scope.loadUsers();
            $scope.user = {
                firstName: '',
                lastName: '',
                email: '',
                pass:''
            }
        })
    }

    self.newUser = function()
    {
        $scope.user = {
            firstName: '',
            lastName: '',
            email: '',
            pass: ''
        }
    }

    self.loadUsers();
}