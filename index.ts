import { exec } from "cordova";

const pluginName = "webdesk-kiosk-nfc-plugin";
enum Methods {
    checkIsReady = "checkIsReady",
    addListener = "addListener",
    init = "init",
}
const useKioskReader = {
    checkIsReady: (success: any, error: any) => {
        exec(success, error, pluginName, Methods.checkIsReady, []);
    },
    addListener: (success: any, error: any) => {
        exec(success, error, pluginName, Methods.addListener, []);
    },
    init: (success: any, error: any) => {
        exec(success, error, pluginName, Methods.init, []);
    },
};

(window as any).useKioskReader = useKioskReader;
