/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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
package com.ematrixinfotech.aksharservnotes.receiver;

import static android.content.Context.MODE_MULTI_PROCESS;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.Spanned;

import com.ematrixinfotech.aksharservnotes.SnoozeActivity;
import com.ematrixinfotech.aksharservnotes.db.DbHelper;
import com.ematrixinfotech.aksharservnotes.helpers.IntentHelper;
import com.ematrixinfotech.aksharservnotes.helpers.LogDelegate;
import com.ematrixinfotech.aksharservnotes.helpers.notifications.NotificationChannels;
import com.ematrixinfotech.aksharservnotes.helpers.notifications.NotificationsHelper;
import com.ematrixinfotech.aksharservnotes.models.Attachment;
import com.ematrixinfotech.aksharservnotes.models.Note;
import com.ematrixinfotech.aksharservnotes.utils.BitmapHelper;
import com.ematrixinfotech.aksharservnotes.utils.Constants;
import com.ematrixinfotech.aksharservnotes.utils.ConstantsBase;
import com.ematrixinfotech.aksharservnotes.utils.ParcelableUtil;
import com.ematrixinfotech.aksharservnotes.utils.TextHelper;

import it.feio.android.omninotes.R;
import com.ematrixinfotech.aksharservnotes.services.NotificationListener;

import java.util.List;


public class AlarmReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context mContext, Intent intent) {
    try {
      if (intent.hasExtra(ConstantsBase.INTENT_NOTE)) {
        Note note = ParcelableUtil.unmarshall(intent.getExtras().getByteArray(ConstantsBase.INTENT_NOTE), Note
            .CREATOR);
        createNotification(mContext, note);
        SnoozeActivity.setNextRecurrentReminder(note);
        updateNote(note);
      }
    } catch (Exception e) {
      LogDelegate.e("Error on receiving reminder", e);
    }
  }

  private void updateNote(Note note) {
    note.setArchived(false);
    if (!NotificationListener.isRunning()) {
      note.setReminderFired(true);
    }
    DbHelper.getInstance().updateNote(note, false);
  }

  private void createNotification(Context mContext, Note note) {
    SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

    PendingIntent piSnooze = IntentHelper
        .getNotePendingIntent(mContext, SnoozeActivity.class, ConstantsBase.ACTION_SNOOZE, note);
    PendingIntent piPostpone = IntentHelper
        .getNotePendingIntent(mContext, SnoozeActivity.class, ConstantsBase.ACTION_POSTPONE, note);
    PendingIntent notifyIntent = IntentHelper
        .getNotePendingIntent(mContext, SnoozeActivity.class, null, note);

    Spanned[] titleAndContent = TextHelper.parseTitleAndContent(mContext, note);
    String title = TextHelper.getAlternativeTitle(mContext, note, titleAndContent[0]);
    String text = titleAndContent[1].toString();

    NotificationsHelper notificationsHelper = new NotificationsHelper(mContext);
    notificationsHelper.createStandardNotification(NotificationChannels.NotificationChannelNames.REMINDERS,
        R.drawable.ic_stat_notification,
        title, notifyIntent).setLedActive().setMessage(text);

    List<Attachment> attachments = note.getAttachmentsList();
    if (!attachments.isEmpty() && !attachments.get(0).getMime_type().equals(ConstantsBase.MIME_TYPE_FILES)) {
      Bitmap notificationIcon = BitmapHelper
          .getBitmapFromAttachment(mContext, note.getAttachmentsList().get(0), 128,
              128);
      notificationsHelper.setLargeIcon(notificationIcon);
    }

    String snoozeDelay = mContext.getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS).getString(
        "settings_notification_snooze_delay", "10");

    notificationsHelper.getBuilder()
        .addAction(R.drawable.ic_material_reminder_time_light,
            TextHelper.capitalize(mContext.getString(R.string.snooze)) + ": " + snoozeDelay,
            piSnooze)
        .addAction(R.drawable.ic_remind_later_light,
            TextHelper.capitalize(mContext.getString(R.string
                .add_reminder)), piPostpone);

    setRingtone(prefs, notificationsHelper);
    setVibrate(prefs, notificationsHelper);

    notificationsHelper.show(note.get_id());
  }


  private void setRingtone(SharedPreferences prefs, NotificationsHelper notificationsHelper) {
    String ringtone = prefs.getString("settings_notification_ringtone", null);
    notificationsHelper.setRingtone(ringtone);
  }


  private void setVibrate(SharedPreferences prefs, NotificationsHelper notificationsHelper) {
    if (prefs.getBoolean("settings_notification_vibration", true)) {
      notificationsHelper.setVibration();
    }
  }

}
