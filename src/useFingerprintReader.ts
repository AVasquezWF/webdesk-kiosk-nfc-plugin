import { KioskReaderPlugin, Methods, useKioskReader } from "./useReader";

const pluginName = "WebdeskKioskFingerprintPlugin";

export const useFingerprintReader = (): KioskReaderPlugin => {

    const { useBasicExecutor, asPromise } = useKioskReader(pluginName);

    return {
        name: pluginName,
        setListenerInterval: (interval: number) => asPromise(Methods.setListenerInterval, interval),
        sendReaderCommand: (command: string) => asPromise(Methods.sendReaderCommand, command),
        checkIsReady: useBasicExecutor(Methods.checkIsReady),
        addListener: useBasicExecutor(Methods.addListener),
        readCard: useBasicExecutor(Methods.readCard),
        reconnectReader: useBasicExecutor(Methods.reconnectReader),
        init: useBasicExecutor(Methods.init)
    };
};
