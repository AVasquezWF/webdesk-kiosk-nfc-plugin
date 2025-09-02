import { KioskReaderPlugin, Methods, useKioskReader } from "./useReader";

const pluginName = "WebdeskKioskNFCPlugin";

export const useElatecReader = (): KioskReaderPlugin => {

    const { useBasicExecutor, asPromise } = useKioskReader(pluginName);

    return {
        name: pluginName,
        call: (methodName, ...args) => asPromise(methodName as Methods, ...args),
        setListenerInterval: (interval: number) => asPromise(Methods.setListenerInterval, interval),
        sendReaderCommand: (command: string) => asPromise(Methods.sendReaderCommand, command),
        checkIsReady: useBasicExecutor(Methods.checkIsReady),
        addListener: useBasicExecutor(Methods.addListener),
        readCard: useBasicExecutor(Methods.readCard),
        reconnectReader: useBasicExecutor(Methods.reconnectReader),
        init: useBasicExecutor(Methods.init)
    };
};
