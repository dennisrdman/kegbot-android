/*
 * Copyright 2012 Mike Wakerly <opensource@hoho.com>.
 *
 * This file is part of the Kegtab package from the Kegbot project. For
 * more information on Kegtab or Kegbot, see <http://kegbot.org/>.
 *
 * Kegtab is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, version 2.
 *
 * Kegtab is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Kegtab. If not, see <http://www.gnu.org/licenses/>.
 */
package org.kegbot.app;

import java.util.List;
import java.util.regex.Pattern;

import org.kegbot.app.service.KegbotCoreService;
import org.kegbot.app.settings.ThirdPartyLicensesActivity;
import org.kegbot.app.setup.SetupActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    KegbotCoreService.stopService(this);
  }

  @Override
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.settings_headers, target);
  }

  public static class GeneralFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings_general);

      findPreference("run_setup").setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          final Intent setupIntent = new Intent(getActivity(), SetupActivity.class);
          setupIntent.putExtra(SetupActivity.EXTRA_REASON, SetupActivity.EXTRA_REASON_USER);
          startActivity(setupIntent);
          return true;
        }
      });

      final ActionBar actionBar = getActivity().getActionBar();
      if (actionBar != null) {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
      }
    }
  }

  public static class KegeratorFragment extends PreferenceFragment {
    private static final Pattern KEGBOARD_NAME_PATTERN = Pattern.compile("^\\w+$");

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings_kegerator);

      final EditTextPreference kegboardName =
          (EditTextPreference) findPreference("config:KEGBOARD_NAME");

      // Prevent illegal characters.
      kegboardName.getEditText().addTextChangedListener(new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
          String text = s.toString();
          if (text.isEmpty()) {
            return;
          }

          if (!KEGBOARD_NAME_PATTERN.matcher(text).matches()) {
            int length = text.length();
            s.delete(length - 1, length);
          }
        }
      });

      kegboardName.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          String value = (String) newValue;
          return KEGBOARD_NAME_PATTERN.matcher(value).matches();
        }
      });
    }
  }

  public static class AboutFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings_about);
      final ActionBar actionBar = getActivity().getActionBar();
      if (actionBar != null) {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
      }

      findPreference("third_party").setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          final Intent thirdPartyIntent = new Intent(getActivity(), ThirdPartyLicensesActivity.class);
          startActivity(thirdPartyIntent);
          return true;
        }
      });

      final PackageManager pm = getActivity().getPackageManager();
      final String versionString;
      final int versionCode;
      try {
        final PackageInfo info = pm.getPackageInfo(getActivity().getPackageName(), 0);
        versionString = info.versionName;
        versionCode = info.versionCode;
      } catch (NameNotFoundException e) {
        // Impossible!
        throw new IllegalStateException(e);
      }

      findPreference("version").setSummary(String.format("%s (build #%d)", versionString,
          Integer.valueOf(versionCode)));
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // app icon in Action Bar clicked; go home
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public static void startSettingsActivity(Context context) {
    Intent intent = new Intent(context, SettingsActivity.class);
    PinActivity.startThroughPinActivity(context, intent);
  }

}
