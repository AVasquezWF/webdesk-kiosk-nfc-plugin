"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const cordova_1 = require("cordova");
const pluginName = "WebdeskKioskNFCPlugin";
var Methods;
(function (Methods) {
    Methods["checkIsReady"] = "checkIsReady";
    Methods["poolReadTag"] = "poolReadTag";
    Methods["addListener"] = "addListener";
    Methods["init"] = "init";
})(Methods || (Methods = {}));
const useKioskReader = () => {
    let poolReadTagId = 0;
    const useExec = (method) => (success, error) => {
        (0, cordova_1.exec)(success, error, pluginName, method, []);
    };
    const checkIsReady = useExec(Methods.checkIsReady);
    const poolReadTag = (success, error) => {
        const executor = useExec(Methods.poolReadTag);
        clearInterval(poolReadTagId);
        poolReadTagId = setInterval(() => executor(success, error), 2000);
    };
    const addListener = useExec(Methods.addListener);
    const init = useExec(Methods.init);
    return {
        checkIsReady,
        poolReadTag,
        addListener,
        init,
    };
};
window.useKioskReader = useKioskReader();
