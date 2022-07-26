/*
 * Copyright (c) 2022 chr_56 & Abou Zeid (kabouzeid) (original author)
 */

package lib.phonograph.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

import player.phonograph.R;


/**
 * @author Aidan Follestad (afollestad)
 */
public class EditTextPreferenceX extends EditTextPreference {

    public EditTextPreferenceX(Context context) {
        super(context);
        init(context, null);
    }

    public EditTextPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditTextPreferenceX(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public EditTextPreferenceX(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(R.layout.x_preference);
    }
}
