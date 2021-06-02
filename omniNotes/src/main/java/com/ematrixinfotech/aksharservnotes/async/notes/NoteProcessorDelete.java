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

package com.ematrixinfotech.aksharservnotes.async.notes;

import com.ematrixinfotech.aksharservnotes.OmniNotes;
import com.ematrixinfotech.aksharservnotes.async.bus.NotesDeletedEvent;
import com.ematrixinfotech.aksharservnotes.db.DbHelper;

import de.greenrobot.event.EventBus;

import com.ematrixinfotech.aksharservnotes.models.Attachment;
import com.ematrixinfotech.aksharservnotes.models.Note;
import com.ematrixinfotech.aksharservnotes.utils.StorageHelper;
import java.util.List;


public class NoteProcessorDelete extends NoteProcessor {


  private final boolean keepAttachments;


  public NoteProcessorDelete(List<Note> notes) {
    this(notes, false);
  }


  public NoteProcessorDelete(List<Note> notes, boolean keepAttachments) {
    super(notes);
    this.keepAttachments = keepAttachments;
  }


  @Override
  protected void processNote(Note note) {
    DbHelper db = DbHelper.getInstance();
    if (db.deleteNote(note) && !keepAttachments) {
      for (Attachment mAttachment : note.getAttachmentsList()) {
        StorageHelper
            .deleteExternalStoragePrivateFile(OmniNotes.getAppContext(), mAttachment.getUri()
                .getLastPathSegment());
      }
    }
  }


  @Override
  protected void afterProcess(List<Note> notes) {
    EventBus.getDefault().post(new NotesDeletedEvent(notes));
  }

}
