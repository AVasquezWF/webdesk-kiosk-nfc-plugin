"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useKioskReader = exports.Methods = void 0;
const cordova_1 = require("cordova");
var Methods;
(function (Methods) {
    Methods["setListenerInterval"] = "setListenerInterval";
    Methods["sendReaderCommand"] = "sendReaderCommand";
    Methods["checkIsReady"] = "checkIsReady";
    Methods["addListener"] = "addListener";
    Methods["readCard"] = "readCard";
    Methods["init"] = "init";
})(Methods || (exports.Methods = Methods = {}));
const useKioskReader = (pluginName) => {
    const asPromise = (method, ...args) => new Promise((res, rej) => {
        (0, cordova_1.exec)(res, rej, pluginName, method, [...args]);
    });
    const useBasicExecutor = (method, args = []) => (success, error) => {
        console.log({ pluginName, method });
        (0, cordova_1.exec)(success, error, pluginName, method, [...args]);
    };
    return { asPromise, useBasicExecutor };
};
exports.useKioskReader = useKioskReader;
