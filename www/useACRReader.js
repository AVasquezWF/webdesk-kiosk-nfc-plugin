"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useACRReader = void 0;
const useACROriginalImpl_1 = require("./useACROriginalImpl");
const useReader_1 = require("./useReader");
const pluginName = "ACRNFCReaderPhoneGapPlugin";
const useACRReader = () => {
    const { asPromise, useBasicExecutor } = (0, useReader_1.useKioskReader)(pluginName);
    const ACR = (0, useACROriginalImpl_1.useACROriginalImpl)(pluginName);
    // We assign it here as it is the original object which 
    // the Java side interacts with.
    window.ACR = ACR;
    return {
        name: pluginName,
        setListenerInterval: (interval) => asPromise(useReader_1.Methods.setListenerInterval, interval),
        sendReaderCommand: (command) => asPromise(useReader_1.Methods.sendReaderCommand, command),
        checkIsReady: ACR.isReady,
        addListener: (success, error) => {
            setTimeout(() => {
                useBasicExecutor(useReader_1.Methods.addListener)(success, error);
            }, 10);
        },
        readCard: ACR.readUID,
        init: (success, error) => { success("Initialized"); },
    };
};
exports.useACRReader = useACRReader;
