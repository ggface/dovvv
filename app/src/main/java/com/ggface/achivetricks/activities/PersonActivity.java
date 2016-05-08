package com.ggface.achivetricks.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ggface.achivetricks.R;
import com.ggface.achivetricks.Units;
import com.ggface.achivetricks.classes.Person;
import com.ggface.achivetricks.classes.Tools;
import com.ggface.achivetricks.fragments.PersonFragment;
import com.google.gson.Gson;

public class PersonActivity extends AppCompatActivity {

    private Person mPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Toolbar pToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(pToolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PersonFragment())
                    .commit();
        }

        if (Tools.containsString(getIntent().getExtras(), Units.ARG_JSON)) {
            String json = getIntent().getExtras().getString(Units.ARG_JSON);
            this.mPerson = new Gson().fromJson(json, Person.class);
            getSupportActionBar().setTitle(this.mPerson.name);
        } else if (Tools.containsInt(getIntent().getExtras(), Units.ARG_INDEX)) {
            getSupportActionBar().setTitle("Edit lovely note");
        } else
            getSupportActionBar().setTitle("New lovely note");
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_person, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

}
