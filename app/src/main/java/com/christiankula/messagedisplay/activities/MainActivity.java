package com.christiankula.messagedisplay.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.christiankula.messagedisplay.R;
import com.christiankula.messagedisplay.adapters.ColorButtonAdapter;
import com.christiankula.messagedisplay.models.ColorModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int OFFSET_TEXT_SIZE = 20;
    private final int TEXT_SIZE_SEEKBAR_STEP = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SeekBar sb = (SeekBar) findViewById(R.id.text_size_seekbar);
        final GridView gv = (GridView) findViewById(R.id.grid_colours);
        final TextView tv = (TextView) findViewById(R.id.text_size);
        final CheckBox cb = (CheckBox) findViewById(R.id.all_caps_checkbox);
        final RadioGroup rg = (RadioGroup) findViewById(R.id.text_color_radiogroup);

        int[] colors = this.getResources().getIntArray(R.array.colors);
        String[] colorNames = this.getResources().getStringArray(R.array.color_names);

        List<ColorModel> colorModels = new ArrayList<>();


        for (int i = 0; i < colors.length; i++) {
            String color = Integer.toHexString(colors[i]);
            color = color.substring(2);
            color = "#" + color;

            ColorModel c = new ColorModel(color, colorNames[(int) Math.floor(i / 3)]);
            colorModels.add(c);
        }

        tv.setText(sb.getProgress() + OFFSET_TEXT_SIZE + "sp");

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    progress = (Math.round(progress / TEXT_SIZE_SEEKBAR_STEP)) * TEXT_SIZE_SEEKBAR_STEP;
                    seekBar.setProgress(progress);
                    tv.setText(progress + OFFSET_TEXT_SIZE + "sp");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        gv.setAdapter(new ColorButtonAdapter(getApplicationContext(), colorModels));
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = ((EditText) findViewById(R.id.message_input)).getText().toString();

                String textColor = new String();
                switch (rg.getCheckedRadioButtonId()) {
                    case R.id.text_color_black:
                        textColor = "#000000";
                        break;

                    case R.id.text_color_white:
                        textColor = "#FFFFFF";
                        break;
                }

                ColorModel c = (ColorModel) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), DisplayMessageActivity.class);


                intent.putExtra(DisplayMessageActivity.MESSAGE_EXTRA, message);
                intent.putExtra(DisplayMessageActivity.TEXT_SIZE_EXTRA, sb.getProgress() + OFFSET_TEXT_SIZE);
                intent.putExtra(DisplayMessageActivity.ALL_CAPS_EXTRA, cb.isChecked());
                intent.putExtra(DisplayMessageActivity.TEXT_COLOR_EXTRA, textColor);
                intent.putExtra(DisplayMessageActivity.COLOR_EXTRA, c.getHexColor());


                startActivity(intent);
            }
        });
    }

    public void onResume() {
        super.onResume();
        final SeekBar sb = (SeekBar) findViewById(R.id.text_size_seekbar);
        final TextView tv = (TextView) findViewById(R.id.text_size);

        tv.setText(sb.getProgress() + OFFSET_TEXT_SIZE + "sp");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void hideKeyboard(View view) {
        // Check if no view has focus:
        view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
