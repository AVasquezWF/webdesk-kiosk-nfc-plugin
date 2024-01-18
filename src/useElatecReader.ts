import { KioskReaderPlugin, Methods, useKioskReader } from "./useReader";

const pluginName = "WebdeskKioskNFCPlugin";

export const useElatecReader = (): KioskReaderPlugin => {

    let initInterval: any = 0;
    const { useBasicExecutor, asPromise } = useKioskReader(pluginName);

    return {
        name: pluginName,
        setListenerInterval: (interval: number) => asPromise(Methods.setListenerInterval, interval),
        sendReaderCommand: (command: string) => asPromise(Methods.sendReaderCommand, command),
        checkIsReady: useBasicExecutor(Methods.checkIsReady),
        addListener: useBasicExecutor(Methods.addListener),
        readCard: useBasicExecutor(Methods.readCard),
        init: (success, error) => {
            clearInterval(initInterval);
            initInterval = setInterval(() => useBasicExecutor(Methods.init)(success, error), 10000)
        },
    };
};
