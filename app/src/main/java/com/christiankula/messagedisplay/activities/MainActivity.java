package com.christiankula.messagedisplay.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.christiankula.messagedisplay.R;
import com.christiankula.messagedisplay.adapters.ColourButtonAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private int OFFSET_TEXT_SIZE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SeekBar sb = (SeekBar) findViewById(R.id.text_size_seekbar);
        final GridView gv = (GridView) findViewById(R.id.grid_colours);
        final TextView tv = (TextView) findViewById(R.id.text_size);
        final CheckBox cb = (CheckBox) findViewById(R.id.all_caps_checkbox);

        ArrayList<String> s_colors = new ArrayList<>();
        int[] colours = this.getResources().getIntArray(R.array.colors);

        for (int colour : colours) {
            s_colors.add(Integer.toHexString(colour));
        }

        tv.setText(sb.getProgress() + OFFSET_TEXT_SIZE + "sp");

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv.setText(progress + OFFSET_TEXT_SIZE + "sp");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        gv.setAdapter(new ColourButtonAdapter(getApplicationContext(), s_colors));

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = ((EditText) findViewById(R.id.message_input)).getText().toString();


                Intent intent = new Intent(getApplicationContext(), DisplayMessageActivity.class);
                intent.putExtra(DisplayMessageActivity.COLOR_EXTRA, ((Button) view.findViewById(R.id.colour_button))
                        .getText());

                intent.putExtra(DisplayMessageActivity.TEXT_SIZE_EXTRA, sb.getProgress() + OFFSET_TEXT_SIZE);
                intent.putExtra(DisplayMessageActivity.ALL_CAPS_EXTRA, cb.isChecked());
                intent.putExtra(DisplayMessageActivity.MESSAGE_EXTRA, message);
                startActivity(intent);
            }
        });
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
}
