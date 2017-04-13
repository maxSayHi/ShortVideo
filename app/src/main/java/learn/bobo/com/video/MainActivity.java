package learn.bobo.com.video;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.iv_result)
    ImageView iv_result;
    @BindView(R.id.tv_result)
    TextView tv_result;

    private String result;
    public static byte[] pic;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            tv_result.setText(result);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        tv_result.setMovementMethod(new ScrollingMovementMethod());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @OnClick(R.id.btn_start)
    public void startRecord() {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }


    @OnClick(R.id.btn_preview)
    public void startPreview() {
        Intent intent = new Intent(this, PreviewActivity.class);
        startActivityForResult(intent, 0);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        byte[] pics = data.getByteArrayExtra("pic");
//        Bitmap bitmap = BitmapFactory.decodeByteArray(pics, 0, pics.length);
//        iv_result.setImageBitmap(bitmap);
//    }

    @OnClick(R.id.btn_view_pic)
    public void viewPic() {
//        byte[] pics = data.getByteArrayExtra("pic");
        Log.e("bobo", pic.length / 1024 + "  照片大小");
        Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);
        iv_result.setImageBitmap(bitmap);
    }

    @OnClick(R.id.btn_get_result)
    public void getResult() {
        new Thread(){
            @Override
            public void run() {
                try {
                    getData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void getData() throws IOException {
        String url = "http://192.168.1.6:8080/hi.do";
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        result = response.body().string();
        mHandler.sendEmptyMessage(0);
    }
}
