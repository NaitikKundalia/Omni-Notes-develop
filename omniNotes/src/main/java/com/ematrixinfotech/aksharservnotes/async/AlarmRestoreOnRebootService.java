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

package it.ematrixinfotech.aksharservnotes.async;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import it.ematrixinfotech.aksharservnotes.BaseActivity;
import it.ematrixinfotech.aksharservnotes.OmniNotes;
import it.ematrixinfotech.aksharservnotes.db.DbHelper;
import it.ematrixinfotech.aksharservnotes.helpers.LogDelegate;
import it.ematrixinfotech.aksharservnotes.models.Note;
import it.ematrixinfotech.aksharservnotes.utils.ReminderHelper;
import java.util.List;

/**
 * Verify version code and add wake lock in manifest is important to avoid crash
 */
public class AlarmRestoreOnRebootService extends JobIntentService {

  public static final int JOB_ID = 0x01;

  public static void enqueueWork(Context context, Intent work) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      enqueueWork(context, AlarmRestoreOnRebootService.class, JOB_ID, work);
    } else {
      Intent jobIntent = new Intent(context, AlarmRestoreOnRebootService.class);
      context.startService(jobIntent);
    }
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    LogDelegate.i("System rebooted: service refreshing reminders");
    Context mContext = getApplicationContext();

    BaseActivity.notifyAppWidgets(mContext);

    List<Note> notes = DbHelper.getInstance().getNotesWithReminderNotFired();
    LogDelegate.d("Found " + notes.size() + " reminders");
    for (Note note : notes) {
      ReminderHelper.addReminder(OmniNotes.getAppContext(), note);
    }
  }

}
