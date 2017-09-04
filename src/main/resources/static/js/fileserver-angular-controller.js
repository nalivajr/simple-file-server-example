'use strict';

fileserverApp.controller('fileListController', function ($scope, $http, $compile) {
    $scope.files = [];

    this.loadFiles = function () {
        $http
            .get(serverUrl + '/api/get-files', {
                headers: {
                    'AuthToken': getAuthCookie(document.cookie)
                }})
            .then(function(response) {
                    var list = '<div>' +
                        '<ul ng-repeat="file in files">' +
                        '<li><a href="/api/get-file?filename={{file.filename}}&AuthToken=' + getAuthCookie(document.cookie) +
                        '">{{file.filename}} - {{file.size}} B</a></li>' +
                        '</ul>' +
                        '</div>';
                    var data = $compile(list)($scope);
                    $('.content')
                        .empty()
                        .append(data)
                        .removeClass('hidden');
                    $scope.files = response.data;
                    console.log('success on get files');
                },
                function() {
                    console.log('error on get files');
                });
    };

    this.uploadFile = function () {
        var file = $('#fileUploadForm .file-for-upload').val();
        if (file === null || file === undefined || file == "") {
            alert('Select file');
            return;
        }
        var form = $('#fileUploadForm')[0];

        var data = new FormData(form);
        $('#btnSubmit').prop('disabled', true);

        return $http({
            url: serverUrl + '/api/upload',
            method: 'POST',
            data: data,
            //assign content-type as undefined, the browser
            //will assign the correct boundary for us
            headers: {
                'Content-Type': undefined,
                'AuthToken': getAuthCookie(document.cookie)
            },
            //prevents serializing payload.  don't do it.
            transformRequest: angular.identity
        }).then(function (response) {
            $('#fileUploadForm .file-for-upload').val(null);
            console.log(response);
            $('#btnSubmit').prop('disabled', false);
            alert('Success!');
        }).catch(function (error) {
            alert('error');
            console.log(error);
            $('#btnSubmit').prop('disabled', false);
        })
        // alert('Upload');
    };

    this.showUploadForm = function () {
        var form = '<form method="post" enctype="multipart/form-data" id="fileUploadForm">' +
            '<input type="file" name="file" class="file-for-upload"/><br/>' +
            '<input type="submit" value="Upload" class="submit-upload-btn" id="btnSubmit" ng-click="fControl.uploadFile()"/>' +
            '</form>';
        var data = $compile(form)($scope);
        $('.content')
            .empty()
            .append(data)
            .removeClass('hidden');

    }
});

function getAuthCookie(cookieStr) {
    var parts = cookieStr.split(';');
    for (var cookie in parts) {
        var pair = parts[cookie];
        if (pair.match('^AuthToken') === null) {
            continue;
        }
        return pair.split('=')[1]
    }
}