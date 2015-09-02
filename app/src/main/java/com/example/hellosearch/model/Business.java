package com.example.hellosearch.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by madhav on 8/30/15.
 */
public class Business implements Parcelable {
    public String businessName;
    public String business_id;
    public String image_url;
    public String url;
    public String display_phone;
    public String rating_img_url;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.businessName);
        dest.writeString(this.business_id);
        dest.writeString(this.image_url);
        dest.writeString(this.url);
        dest.writeString(this.display_phone);
        dest.writeString(this.rating_img_url);
    }

    public Business() { }

    private Business(Parcel in) {
        this.businessName = in.readString();
        this.business_id = in.readString();
        this.image_url = in.readString();
        this.url = in.readString();
        this.display_phone = in.readString();
        this.rating_img_url = in.readString();

    }

    public static final Parcelable.Creator<Business> CREATOR = new Parcelable.Creator<Business>() {
        public Business createFromParcel(Parcel source) {
            return new Business(source);
        }

        public Business[] newArray(int size) {
            return new Business[size];
        }
    };
}
