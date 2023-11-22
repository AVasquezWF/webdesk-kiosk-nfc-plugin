import { exec } from "cordova";

const pluginName = "WebdeskKioskNFCPlugin";
enum Methods {
    checkIsReady = "checkIsReady",
    poolReadTag = "poolReadTag",
    addListener = "addListener",
    init = "init",
}

type UseExecTemplate = (success: () => void, error: () => void) => void;
interface KioskReaderPlugin {
    checkIsReady: UseExecTemplate;
    poolReadTag: UseExecTemplate;
    addListener: UseExecTemplate;
    init: UseExecTemplate;
}

const useKioskReader = (): KioskReaderPlugin => {
    let poolReadTagId: number | NodeJS.Timeout = 0;
    const useExec =
        (method: Methods): UseExecTemplate =>
        (success: any, error: any) => {
            exec(success, error, pluginName, method, []);
        };

    const checkIsReady = useExec(Methods.checkIsReady);
    const poolReadTag: UseExecTemplate = (success, error) => {
        const executor = useExec(Methods.poolReadTag);
        clearInterval(poolReadTagId);
        poolReadTagId = setInterval(() => executor(success, error), 1000);
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

(window as any).useKioskReader = useKioskReader();
