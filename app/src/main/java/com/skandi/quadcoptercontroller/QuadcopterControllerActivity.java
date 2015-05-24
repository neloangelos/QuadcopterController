package com.skandi.quadcoptercontroller;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class QuadcopterControllerActivity extends Activity {
    private TextView angleTextView;
    private TextView powerTextView;
    private JoystickView joystick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quadcopter_controller);
        angleTextView = (TextView) findViewById(R.id.angleTextViewLeft);
        powerTextView = (TextView) findViewById(R.id.powerTextViewLeft);
        joystick = (JoystickView) findViewById(R.id.JoystickViewLeft);
        joystick.setYisAutoCenter(false);
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int xPosition, int yPosition) {
                angleTextView.setText("xPosition:" + String.valueOf(xPosition) + "°");
                powerTextView.setText("yPosition:" + String.valueOf(yPosition) + "%");
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_quadcopter_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
