package org.smssecure.smssecure.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;

import org.smssecure.smssecure.ConversationActivity;
import org.smssecure.smssecure.recipients.Recipient;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.util.Util;

public class NotificationItem {

  private final Recipients   recipients;
  private final Recipient    individualRecipient;
  private final Recipients   threadRecipients;
  private final long         threadId;
  private final CharSequence text;
  private final Uri          image;

  public NotificationItem(Recipient individualRecipient, Recipients recipients,
                          Recipients threadRecipients, long threadId,
                          CharSequence text, Uri image)
  {
    this.individualRecipient = individualRecipient;
    this.recipients          = recipients;
    this.threadRecipients    = threadRecipients;
    this.text                = text;
    this.image               = image;
    this.threadId            = threadId;
  }

  public Recipient getIndividualRecipient() {
    return individualRecipient;
  }

  public String getIndividualRecipientName() {
    return individualRecipient.toShortString();
  }

  public CharSequence getText() {
    return text;
  }

  public Uri getImage() {
    return image;
  }

  public boolean hasImage() {
    return image != null;
  }

  public long getThreadId() {
    return threadId;
  }

  public CharSequence getBigStyleSummary() {
    return (text == null) ? "" : text;
  }

  public CharSequence getTickerText() {
    SpannableStringBuilder builder = new SpannableStringBuilder();
    builder.append(Util.getBoldedString(getIndividualRecipientName()));
    builder.append(": ");
    builder.append(getText());

    return builder;
  }

  public PendingIntent getPendingIntent(Context context) {
    Intent intent = new Intent(context, ConversationActivity.class);

    Recipients notifyRecipients = threadRecipients != null ? threadRecipients : recipients;
    if (notifyRecipients != null) intent.putExtra("recipients", notifyRecipients.getIds());

    intent.putExtra("thread_id", threadId);
    intent.setData((Uri.parse("custom://"+System.currentTimeMillis())));

    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

}
