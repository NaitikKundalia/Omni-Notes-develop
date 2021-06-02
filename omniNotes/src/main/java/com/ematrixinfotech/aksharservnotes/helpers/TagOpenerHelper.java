/*
 * Copyright (C) 2013-2021 Federico Iosue (federico@iosue.it)
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

package com.ematrixinfotech.aksharservnotes.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;

import com.ematrixinfotech.aksharservnotes.exceptions.checked.UnhandledIntentException;
import com.ematrixinfotech.aksharservnotes.utils.IntentChecker;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class TagOpenerHelper {

  @Nullable
  public Intent openOrGetIntent(Context context, String tagText) throws UnhandledIntentException {
    Intent intent;
    switch (tagText.split(":")[0]) {
      case "tel":
        intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(tagText));
        break;
      case "mailto":
        intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse(tagText));
        break;
      default:
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tagText));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    if (StringUtils.isBlank(IntentChecker.resolveActivityPackage(context, intent))) {
      throw new UnhandledIntentException();
    }

    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
    return null;
  }