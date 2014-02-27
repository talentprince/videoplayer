package org.weyoung.player;

import org.weyoung.player.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.play);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this,
                            MovieActivity.class)
                            .setDataAndType(
                                    Uri.parse("http://10.129.156.147/movie.mp4"),
                                    "video/*")
                            .putExtra(Intent.EXTRA_TITLE, "movie")
                            .putExtra(MovieActivity.KEY_TREAT_UP_AS_BACK, true);
                    // startActivityForResult(intent, REQUEST_PLAY_VIDEO);
                    // intent.setClass(MovieActivity.class);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, "error!!!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
