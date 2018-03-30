package heyhub.vikalppatel.in.task;

import android.os.Parcel;
import android.os.Parcelable;

public class Geopoint implements Parcelable {
    private Double Latitude;
    private Double Longitude;

    public Geopoint(Double latitude, Double longitude) {
        Latitude = latitude;
        Longitude = longitude;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }

    protected Geopoint(Parcel in) {
        Latitude = in.readByte() == 0x00 ? null : in.readDouble();
        Longitude = in.readByte() == 0x00 ? null : in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (Latitude == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(Latitude);
        }
        if (Longitude == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(Longitude);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Geopoint> CREATOR = new Parcelable.Creator<Geopoint>() {
        @Override
        public Geopoint createFromParcel(Parcel in) {
            return new Geopoint(in);
        }

        @Override
        public Geopoint[] newArray(int size) {
            return new Geopoint[size];
        }
    };
}
