package com.example.scarb.firebasetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Layout;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends ArrayAdapter<FriendData>{

    public FriendAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<FriendData> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FriendData friendData = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_row,
                    parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.friendPicture);
        TextView textView = (TextView) convertView.findViewById(R.id.friendUserName);
        /*Gets user name */
        textView.setText(friendData.getUsername());

        /*Decodes the image from dataBase to bitmap*/
        InputStream stream = new ByteArrayInputStream(
                Base64.decode(friendData.getPhotoID().getBytes(), Base64.DEFAULT));
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.
                create(getContext().getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        imageView.setImageDrawable(roundedBitmapDrawable);

        return convertView;
    }
}
