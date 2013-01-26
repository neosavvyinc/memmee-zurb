var memmeeServices = angular.module('admin.services', ['ngResource']);

memmeeServices.factory('adminService', ["$http", "$rootScope", function ($http, $scope) {
    'use strict';
    return {
        showAllInspirations: function (resultHandler, faultHandler) {
            $http({method: 'GET', url: '/memmeeinspirationrest/inspirations/all' }).then(
                function (response) {
                    if (resultHandler) {
                        resultHandler(response.data);
                    }
                },
                function (response) {
                    if (faultHandler) {
                        faultHandler(response.data);
                    }
                }
            );
        },

    };

}]);
