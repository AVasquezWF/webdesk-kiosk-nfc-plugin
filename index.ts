import { exec } from "cordova";

const pluginName = "WebdeskKioskNFCPlugin";
enum Methods {
    setListenerInterval = "setListenerInterval",
    sendReaderCommand = "sendReaderCommand",
    checkIsReady = "checkIsReady",
    addListener = "addListener",
    readCard = "readCard",
    init = "init",
}

type CordovaCallback = (data: any) => any;
type UseExecTemplate = (
    success: CordovaCallback,
    error: CordovaCallback
) => void;
interface KioskReaderPlugin {
    sendReaderCommand: (
        command: string
    ) => Promise<void>;
    setListenerInterval: (
        interval: number
    ) => Promise<void>;
    checkIsReady: UseExecTemplate;
    addListener: UseExecTemplate;
    readCard: UseExecTemplate;
    init: UseExecTemplate;
}

const asPromise = <T>(method: Methods, ...args: any) =>
    new Promise<T>((res, rej) => {
        exec(res, rej, pluginName, method, [...args])
    });

const useKioskReader = (): KioskReaderPlugin => {

    const useBasicExecutor =
        (method: Methods): UseExecTemplate =>
            (success, error) =>
                exec(success, error, pluginName, method, []);

    return {
        setListenerInterval: (interval: number) => asPromise(Methods.setListenerInterval, interval),
        sendReaderCommand: (command: string) => asPromise(Methods.sendReaderCommand, command),
        checkIsReady: useBasicExecutor(Methods.checkIsReady),
        addListener: useBasicExecutor(Methods.addListener),
        readCard: useBasicExecutor(Methods.readCard),
        init: useBasicExecutor(Methods.init),
    };
};

(window as any).useKioskReader = useKioskReader();
