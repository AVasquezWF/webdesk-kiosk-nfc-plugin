"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const cordova_1 = require("cordova");
const pluginName = "WebdeskKioskNFCPlugin";
var Methods;
(function (Methods) {
    Methods["checkIsReady"] = "checkIsReady";
    Methods["readCard"] = "readCard";
    Methods["init"] = "init";
})(Methods || (Methods = {}));
const useKioskReader = () => {
    let poolReadTagId = 0;
    const useBasicExecutor = (method) => (success, error) => (0, cordova_1.exec)(success, error, pluginName, method, []);
    const addListener = (success, error) => {
        const executor = useBasicExecutor(Methods.readCard);
        clearInterval(poolReadTagId);
        poolReadTagId = setInterval(() => executor(success, error), 1000);
    };
    return {
        checkIsReady: useBasicExecutor(Methods.checkIsReady),
        addListener,
        init: useBasicExecutor(Methods.init),
    };
};
window.useKioskReader = useKioskReader();
