package adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.hunliji.livelistvideoplayer.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import listvideo.ListVideoPlayer;
import listvideo.ListVideoPlayerManager;
import utils.CommonUtil;

/**
 * Created by jing_tian on 2018/7/19.
 */
public class LiveListAdapter extends RecyclerView.Adapter<LiveListAdapter.ViewHolder>  {

    private List<String> mUrls;
    private Context mContext;
    private OnModeChange onModeChange;

    public LiveListAdapter(List<String> mUrls, Context context) {
        this.mUrls = mUrls;
        this.mContext = context;
    }


    public void setOnModeChange(OnModeChange onModeChange) {
        this.onModeChange = onModeChange;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.live_list_item, parent, false);
        return new ViewHolder(view);
    }

    public interface OnModeChange {
        void modeChange(int mode);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        if (TextUtils.isEmpty(mUrls.get(position))) {
            return;
        }
        holder.listVideoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ListVideoPlayerManager.getCurrentMode() == ListVideoPlayer.Mode.FULL_SCREEN) {
                    Toast.makeText(mContext, "hhhh", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                holder.listVideoPlayer.startVideo();
            }
        });


        if (!TextUtils.isEmpty(mUrls.get(position))) {
            holder.listVideoPlayer.setVisibility(View.VISIBLE);
            holder.listVideoPlayer.setSource(Uri.parse(mUrls.get(position)));
            holder.listVideoPlayer.setOnModeChangeListener(new ListVideoPlayer
                    .OnModeChangeListener() {
                @Override
                public void OnModeChange(int mode) {
                    if (onModeChange != null) {
                        onModeChange.modeChange(mode);
                    }
                }
            });
            holder.listVideoPlayer.setOnStateChangeListener(new ListVideoPlayer
                    .OnStateChangeListener() {
                @Override
                public void OnStateChange(int state) {
                    Log.d("gaofenghlj", state + "");
                    switch (state) {
                        case ListVideoPlayer.STATE.ERROR:
                        case ListVideoPlayer.STATE.NORMAL:
                        case ListVideoPlayer.STATE.COMPLETE:
                            holder.mBgIv.setVisibility(View.VISIBLE);
                            if (ListVideoPlayerManager.getCurrentMode() == ListVideoPlayer.Mode
                                    .TINY_SCREEN) {
                                ListVideoPlayerManager.hideController();
                            }

                            Log.d("fff", "播放结束了");
                            break;
                        case ListVideoPlayer.STATE.PREPARING:
                        case ListVideoPlayer.STATE.PLAYING:
                        case ListVideoPlayer.STATE.PAUSE:
                            holder.mBgIv.setVisibility(View.GONE);
                            ListVideoPlayerManager.showController();

                            break;
                    }
                }
            });
        } else {
            holder.mBgIv.setVisibility(View.VISIBLE);
            holder.listVideoPlayer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return CommonUtil.getCollectionSize(mUrls);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.player)
        ListVideoPlayer listVideoPlayer;
        @BindView(R.id.iv)
        ImageView mBgIv;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

    }
}

