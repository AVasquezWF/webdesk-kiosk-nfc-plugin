import { exec } from "cordova";

const pluginName = "WebdeskKioskNFCPlugin";
enum Methods {
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
    checkIsReady: UseExecTemplate;
    addListener: UseExecTemplate;
    init: UseExecTemplate;
}

const useKioskReader = (): KioskReaderPlugin => {
    let poolReadTagId: number | NodeJS.Timeout = 0;

    const useBasicExecutor =
        (method: Methods): UseExecTemplate =>
            (success, error) =>
                exec(success, error, pluginName, method, []);


    const addListener: UseExecTemplate = (success, error) => {
        const executor = useBasicExecutor(Methods.readCard);
        clearInterval(poolReadTagId);
        poolReadTagId = setInterval(() => executor(success, error), 1000);
    };

    return {
        checkIsReady: useBasicExecutor(Methods.checkIsReady),
        addListener: useBasicExecutor(Methods.addListener),
        init: useBasicExecutor(Methods.init),
    };
};

(window as any).useKioskReader = useKioskReader();
