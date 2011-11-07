/*
 * Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greendroid.widget.itemview;

import greendroid.widget.item.Item;
import greendroid.widget.item.SubtitleItem2;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

//import com.cyrilmottier.android.greendroid.R;
import com.cldellow.ballero.R;

/**
 * View representation of the {@link SubtitleItem}.
 * 
 * @author Cyril Mottier
 */
public class SubtitleItemView2 extends LinearLayout implements ItemView {

    private TextView mTextView;
    private TextView mSubtitleView;
    private TextView mSubtitleView2;

    public SubtitleItemView2(Context context) {
        this(context, null);
    }

    public SubtitleItemView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void prepareItemView() {
        mTextView = (TextView) findViewById(R.id.gd_text);
        mSubtitleView = (TextView) findViewById(R.id.gd_subtitle);
        mSubtitleView2 = (TextView) findViewById(R.id.gd_subtitle2);
    }

    public void setObject(Item object) {
        final SubtitleItem2 item = (SubtitleItem2) object;
        mTextView.setText(item.text);
        mSubtitleView.setText(item.subtitle);
        mSubtitleView2.setText(item.subtitle2);
        if(item.subtitle2 == null || item.subtitle2.equals("")) {
          mSubtitleView2.setVisibility(View.GONE);
        } else {
          mSubtitleView2.setVisibility(View.VISIBLE);
        }
    }
}
