import { useACRReader } from "./useACRReader"
import { useElatecReader } from "./useElatecReader";
import { useFingerprintReader } from "./useFingerprintReader";

const _window: any = window;
const init = () => {
    _window.ACRPlugin = useACRReader();
    _window.ElatecPlugin = useElatecReader();
    _window.FingerprintPlugin = useFingerprintReader();
}

init();