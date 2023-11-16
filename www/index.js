"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const cordova_1 = require("cordova");
const pluginName = "WebdeskKioskNFCPlugin";
var Methods;
(function (Methods) {
    Methods["checkIsReady"] = "checkIsReady";
    Methods["addListener"] = "addListener";
    Methods["init"] = "init";
})(Methods || (Methods = {}));
const useKioskReader = {
    checkIsReady: (success, error) => {
        (0, cordova_1.exec)(success, error, pluginName, Methods.checkIsReady, []);
    },
    addListener: (success, error) => {
        (0, cordova_1.exec)(success, error, pluginName, Methods.addListener, []);
    },
    init: (success, error) => {
        (0, cordova_1.exec)(success, error, pluginName, Methods.init, []);
    },
};
window.useKioskReader = useKioskReader;
