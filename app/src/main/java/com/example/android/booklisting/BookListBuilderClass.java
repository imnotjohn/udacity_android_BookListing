package com.example.android.booklisting;

import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by fuguBook on 9/7/16.
 */
public class BookListBuilderClass {

    private String mTitle;
    private String mAuthor;

    public BookListBuilderClass(String mParsedTitle, String mParsedAuthor) {
        mTitle = mParsedTitle;
        mAuthor = mParsedAuthor;
    }

    public String toString(String string) {
        if (string == "mParsedTitle") {
            return mTitle;
        } else if (string == "mParsedAuthor") {
            return mAuthor;
        } else if (string == " ") {
            return "No Thang";
        }
        return string;
    }

}
