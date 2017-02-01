package com.example.android.booklisting;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by fuguBook on 9/7/16.
 */
public class BookListBuilderAdapterClass extends ArrayAdapter<BookListBuilderClass> {

    private ArrayList<BookListBuilderClass> mBookListBuilderClassArrayList = new ArrayList<BookListBuilderClass>(); //added for updateEntries Below

    public BookListBuilderAdapterClass(Activity context, int position, ArrayList<BookListBuilderClass> bookListBuilderClassArrayList) {
        super(context,0,bookListBuilderClassArrayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        BookListBuilderClass currentBookItem = getItem(position);

        TextView titleTextView = (TextView) listItemView.findViewById(R.id.list_title);
        titleTextView.setText(currentBookItem.toString("mParsedTitle"));

        TextView authorTextView = (TextView) listItemView.findViewById(R.id.list_author);
        authorTextView.setText(currentBookItem.toString("mParsedAuthor"));

        return listItemView;
    }

    //added
    public void updateEntries(ArrayList<BookListBuilderClass> bookListBuilderClassArrayList) {
        mBookListBuilderClassArrayList = bookListBuilderClassArrayList;
        notifyDataSetChanged();
    }
}
