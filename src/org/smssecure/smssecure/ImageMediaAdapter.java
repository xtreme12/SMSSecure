/**
 * Copyright (C) 2015 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.smssecure.smssecure;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.smssecure.smssecure.ImageMediaAdapter.ViewHolder;
import org.smssecure.smssecure.components.ForegroundImageView;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.database.CursorRecyclerViewAdapter;
import org.smssecure.smssecure.database.PartDatabase.ImageRecord;
import org.smssecure.smssecure.mms.Slide;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.util.FutureTaskListener;
import org.smssecure.smssecure.util.MediaUtil;

import ws.com.google.android.mms.pdu.PduPart;

public class ImageMediaAdapter extends CursorRecyclerViewAdapter<ViewHolder> {
  private static final String TAG = ImageMediaAdapter.class.getSimpleName();

  private final MasterSecret masterSecret;
  private final int          gridSize;

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public ForegroundImageView imageView;

    public ViewHolder(View v) {
      super(v);
      imageView = (ForegroundImageView) v.findViewById(R.id.image);
    }
  }

  public ImageMediaAdapter(Context context, MasterSecret masterSecret, Cursor c) {
    super(context, c);
    this.masterSecret = masterSecret;
    this.gridSize     = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_max_size);
  }

  @Override
  public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
    final View view = LayoutInflater.from(getContext()).inflate(R.layout.media_overview_item, viewGroup, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
    final ForegroundImageView imageView   = viewHolder.imageView;
    final ImageRecord         imageRecord = ImageRecord.from(cursor);

    PduPart part = new PduPart();

    part.setDataUri(imageRecord.getUri());
    part.setContentType(imageRecord.getContentType().getBytes());
    part.setId(imageRecord.getPartId());

    imageView.setVisibility(View.INVISIBLE);
    Slide slide = MediaUtil.getSlideForPart(getContext(), masterSecret, part, imageRecord.getContentType());
    if (slide != null) {
      slide.getThumbnail(getContext()).addListener(new FutureTaskListener<Pair<Drawable, Boolean>>() {
        @Override
        public void onSuccess(final Pair<Drawable, Boolean> result) {
          imageView.post(new Runnable() {
            @Override
            public void run() {
              imageView.show(result.first, false);
            }
          });
        }

        @Override
        public void onFailure(Throwable error) {
          Log.w(TAG, error);
        }
      });
    }

    imageView.setOnClickListener(new OnMediaClickListener(imageRecord));
  }

  private class OnMediaClickListener implements OnClickListener {
    private ImageRecord record;

    private OnMediaClickListener(ImageRecord record) {
      this.record = record;
    }

    @Override
    public void onClick(View v) {
      Intent intent = new Intent(getContext(), MediaPreviewActivity.class);
      intent.putExtra(MediaPreviewActivity.DATE_EXTRA, record.getDate());

      if (!TextUtils.isEmpty(record.getAddress())) {
        Recipients recipients = RecipientFactory.getRecipientsFromString(getContext(),
                                                                         record.getAddress(),
                                                                         true);
        if (recipients != null && recipients.getPrimaryRecipient() != null) {
          intent.putExtra(MediaPreviewActivity.RECIPIENT_EXTRA, recipients.getPrimaryRecipient().getRecipientId());
        }
      }
      intent.setDataAndType(record.getUri(), record.getContentType());
      getContext().startActivity(intent);

    }
  }
}
