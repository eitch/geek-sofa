CustomWeb = {
    //
};

(function () {
    var basePath = window.location.pathname.substring(0, window.location.pathname.indexOf('/index.html'));
    basePath = basePath.substring(0, basePath.lastIndexOf('/'));
    CustomWeb.baseRestPath = basePath + '/rest';
    console.log('Base REST Path is ' + CustomWeb.baseRestPath);
    CustomWeb.baseWsPath = basePath + '/websocket';
    console.log('Base WebSocket Path is ' + CustomWeb.baseWsPath);
})();
