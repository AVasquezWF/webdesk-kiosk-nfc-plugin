"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useElatecReader = void 0;
const useReader_1 = require("./useReader");
const pluginName = "WebdeskKioskNFCPlugin";
const useElatecReader = () => {
    let initInterval = 0;
    const { useBasicExecutor, asPromise } = (0, useReader_1.useKioskReader)(pluginName);
    return {
        name: pluginName,
        setListenerInterval: (interval) => asPromise(useReader_1.Methods.setListenerInterval, interval),
        sendReaderCommand: (command) => asPromise(useReader_1.Methods.sendReaderCommand, command),
        checkIsReady: useBasicExecutor(useReader_1.Methods.checkIsReady),
        addListener: useBasicExecutor(useReader_1.Methods.addListener),
        readCard: useBasicExecutor(useReader_1.Methods.readCard),
        init: (success, error) => {
            useBasicExecutor(useReader_1.Methods.init)(success, error);
            clearInterval(initInterval);
            initInterval = setInterval(() => useBasicExecutor(useReader_1.Methods.reconnectReader)(() => { }, () => { }), 10000);
        },
    };
};
exports.useElatecReader = useElatecReader;
