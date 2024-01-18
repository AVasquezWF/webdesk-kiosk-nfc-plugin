import { useACROriginalImpl } from "./useACROriginalImpl";
import {
    KioskReaderPlugin,
    Methods,
    useKioskReader,
} from "./useReader";

const pluginName = "ACRNFCReaderPhoneGapPlugin";

export const useACRReader = (): KioskReaderPlugin => {
    const { asPromise, useBasicExecutor } = useKioskReader(pluginName);

    const ACR = useACROriginalImpl(pluginName);

    // We assign it here as it is the original object which 
    // the Java side interacts with.
    (window as any).ACR = ACR

    return {
        name: pluginName,
        setListenerInterval: (interval: number) =>
            asPromise(Methods.setListenerInterval, interval),
        sendReaderCommand: (command: string) =>
            asPromise(Methods.sendReaderCommand, command),
        checkIsReady: ACR.isReady,
        addListener: (success, error) => {
            setTimeout(() => {
                useBasicExecutor(Methods.addListener)(success, error);
            }, 10);
        },
        readCard: ACR.readUID,
        init: (success, error) => { success("Initialized") },
    };
};
