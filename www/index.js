"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const cordova_1 = require("cordova");
const pluginName = "WebdeskKioskNFCPlugin";
var Methods;
(function (Methods) {
    Methods["sendReaderCommand"] = "sendReaderCommand";
    Methods["checkIsReady"] = "checkIsReady";
    Methods["addListener"] = "addListener";
    Methods["readCard"] = "readCard";
    Methods["init"] = "init";
})(Methods || (Methods = {}));
const asPromise = (method, ...args) => new Promise((res, rej) => {
    (0, cordova_1.exec)(res, rej, pluginName, method, [...args]);
});
const useKioskReader = () => {
    const useBasicExecutor = (method) => (success, error) => (0, cordova_1.exec)(success, error, pluginName, method, []);
    return {
        sendReaderCommand: (command) => asPromise(Methods.sendReaderCommand, command),
        checkIsReady: useBasicExecutor(Methods.checkIsReady),
        addListener: useBasicExecutor(Methods.addListener),
        readCard: useBasicExecutor(Methods.readCard),
        init: useBasicExecutor(Methods.init),
    };
};
window.useKioskReader = useKioskReader();
