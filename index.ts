import { exec } from "cordova";

const pluginName = "WebdeskKioskNFCPlugin";
enum Methods {
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
    ) => Promise<boolean>;
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
        sendReaderCommand: (command: string) => asPromise<boolean>(Methods.sendReaderCommand, command),
        checkIsReady: useBasicExecutor(Methods.checkIsReady),
        addListener: useBasicExecutor(Methods.addListener),
        readCard: useBasicExecutor(Methods.readCard),
        init: useBasicExecutor(Methods.init),
    };
};

(window as any).useKioskReader = useKioskReader();
