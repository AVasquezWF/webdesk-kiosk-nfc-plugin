package uareu;

import com.digitalpersona.uareu.Fmd;

public class FmdImpl implements Fmd {
    int cbeffId;
    int captureEquipmentCompliance;
    int captureEquipmentId;
    int width, height, resolution, viewCnt;
    Format format;
    Fmv[] views;
    byte[] data;

    public int getCbeffId() {
        return cbeffId;
    }

    public int getCaptureEquipmentCompliance() {
        return captureEquipmentCompliance;
    }

    public int getCaptureEquipmentId() {
        return captureEquipmentId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getResolution() {
        return resolution;
    }

    public int getViewCnt() {
        return viewCnt;
    }

    public Format getFormat() {
        return format;
    }

    public Fmv[] getViews() {
        return views;
    }

    public byte[] getData() {
        return data;
    }

    public static class FmvImpl implements Fmv {
        int fingerPosition, viewNumber, impressionType, quality, minutiaCnt;
        byte[] data, extBlockData;

        public int getFingerPosition() {
            return fingerPosition;
        }

        public int getViewNumber() {
            return viewNumber;
        }

        public int getImpressionType() {
            return impressionType;
        }

        public int getQuality() {
            return quality;
        }

        public int getMinutiaCnt() {
            return minutiaCnt;
        }

        public byte[] getData() {
            return data;
        }

        public byte[] getExtBlockData() {
            return extBlockData;
        }
    }
}
