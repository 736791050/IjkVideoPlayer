package com.study.ijkvideoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.study.ijkplayer.widget.media.AndroidMediaController;
import com.study.ijkplayer.widget.media.IjkVideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "MainActivity";
    private LinearLayout ll_video_select;
    private EditText et_video;
    private Button bt_confirm;
    private IjkVideoView mVideoView;
    private Button bt_full_screen;
    private boolean mBackPressed;
    private RecyclerView video_list;
    private LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutInflater = LayoutInflater.from(this);

        video_list = (RecyclerView) findViewById(R.id.video_list);
        video_list.setLayoutManager(new LinearLayoutManager(this));

        setVideoList(video_list);

        ll_video_select = (LinearLayout) findViewById(R.id.ll_video_select);
        et_video = (EditText) findViewById(R.id.et_video);
        bt_confirm = (Button) findViewById(R.id.bt_confirm);
        bt_full_screen = (Button) findViewById(R.id.bt_full_screen);
        mVideoView = findViewById(R.id.video_view);

        bt_full_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int direction = getRequestedOrientation();
                if (direction == -1) {
                    direction = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                }
                switch (direction) {
                    case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        bt_full_screen.setText("全屏显示");
                        break;
                    case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        bt_full_screen.setText("退出全屏");
                        break;
                }
            }
        });


        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoView.isPlaying()) {
                    int ratio = mVideoView.toggleAspectRatio();
                    Log.i(TAG, "onClick: " + ratio);
                } else {
                    initPlayer();
                    String path = et_video.getText().toString();
                    if (TextUtils.isEmpty(path)) {
                        path = "test.mov";
                    }
                    String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + path;
//        String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "liechang.mp4";
                    Log.i(TAG, "onCreate: " + videoPath);
                    play(videoPath);
                }
            }
        });

    }

    private void play(String videoPath) {

        if(mVideoView.isPlaying() || mVideoView.mCurrentState == -1){
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
            mVideoView.enterBackground();
            IjkMediaPlayer.native_profileEnd();
        }

        TableLayout mHudView = findViewById(R.id.hud_view);
        AndroidMediaController mMediaController = new AndroidMediaController(this, false);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setHudView(mHudView);
        // prefer mVideoPath
        mVideoView.setVideoPath(videoPath);
        mVideoView.start();
    }

    private void initPlayer() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBackPressed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBackPressed || !mVideoView.isBackgroundPlayEnabled()){
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        }else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.i(TAG, "onConfigurationChanged: ｜");
        }else {
            Log.i(TAG, "onConfigurationChanged: ——");

        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public List<String> path;
    public int prePos = -1;
    public void setVideoList(final RecyclerView videoList) {
        path = new ArrayList<>();
        final String videoPath1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test.mov";
        final String videoPath2 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "liechang.mp4";
        final String videoPath3 = "rtmp://live.hkstv.hk.lxdns.com/live/hks";

        for (int i = 0; i < 100; i++) {
            if(i % 3 == 0) {
                path.add(videoPath1);
            }else if(i % 3 == 1){
                path.add(videoPath2);
            }else {
                path.add(videoPath3);
            }
        }
        videoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.i(TAG, "onScrollStateChanged: " + newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    int pos = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if(pos >= 0){
                        View view = ((LinearLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(pos);
                        if(view != null) {
                            int top = view.getTop();
                            int height = view.getHeight();
                            Log.i(TAG, "onScrollStateChanged: " + top + "  pos:" + pos + "  height:" + height);
                            if(top < 0 && height / 4f < -top){
                                pos = pos + 1;
                            }
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForLayoutPosition(pos);
                            if (prePos != pos && viewHolder != null && viewHolder instanceof VideoHolder) {
                                ((VideoHolder) viewHolder).playVideo(pos);
                            }
                            prePos = pos;
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        RecyclerView.Adapter adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>(){

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                FrameLayout fr = new FrameLayout(parent.getContext());
                fr.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
                TextView tv = new TextView(parent.getContext());
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                tv.setGravity(Gravity.CENTER);
                tv.setId(R.id.tv_id);
                fr.addView(tv);
                return new VideoHolder(fr);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((VideoHolder)holder).fillContent(position);
            }

            @Override
            public int getItemCount() {
                return path.size();
            }
        };
        videoList.setAdapter(adapter);
    }

    public class VideoHolder extends RecyclerView.ViewHolder{

        public VideoHolder(View itemView) {
            super(itemView);
        }

        public void fillContent(final int pos){
            if(((ViewGroup)itemView).getChildCount() == 2){
                ((ViewGroup) itemView).removeViewAt(1);
            }
            ((TextView)itemView.findViewById(R.id.tv_id)).setTextColor(Color.BLACK);
            itemView.setBackgroundColor(Color.WHITE);

            ((TextView)itemView.findViewById(R.id.tv_id)).setText("位置：" + pos);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String url = path.get(pos);
//                    play(url);
//                    video_list.setVisibility(View.GONE);
//                }
//            });
        }

        public void playVideo(int pos){
            String url = path.get(pos);
            addVideoView();
            play(url);
//            video_list.setVisibility(View.GONE);
            ((TextView)itemView.findViewById(R.id.tv_id)).setText("play：" + pos);

        }

        private void addVideoView() {
            ViewGroup parent = (ViewGroup) mVideoView.getParent();
            if(parent != null){
                parent.removeView(mVideoView);
                TextView tv = ((TextView)parent.findViewById(R.id.tv_id));
                if(tv != null) {
                    tv.setTextColor(Color.BLACK);
                    parent.setBackgroundColor(Color.WHITE);
                }
            }
            ((ViewGroup)itemView).addView(mVideoView);
            ((TextView)itemView.findViewById(R.id.tv_id)).setTextColor(Color.WHITE);
            itemView.setBackgroundColor(Color.BLACK);
        }
    }
}
