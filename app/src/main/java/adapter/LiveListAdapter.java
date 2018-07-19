package adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.example.hunliji.livelistvideoplayer.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import listvideo.ListVideoPlayer;
import utils.CommonUtil;

/**
 * Created by jing_tian on 2018/7/19.
 */
public class LiveListAdapter extends RecyclerView.Adapter<LiveListAdapter.ViewHolder>  {

    private List<String> mUrls;
    private Context mContext;

    public LiveListAdapter(List<String> mUrls, Context context) {
        this.mUrls = mUrls;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.live_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (TextUtils.isEmpty(mUrls.get(position))) {
            return;
        }
        holder.listVideoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.listVideoPlayer.startVideo();
            }
        });

        if (!TextUtils.isEmpty(mUrls.get(position))) {
            holder.listVideoPlayer.setVisibility(View.VISIBLE);
            holder.listVideoPlayer.setSource(Uri.parse(mUrls.get(position)));
            holder.listVideoPlayer.setOnStateChangeListener(new ListVideoPlayer.OnStateChangeListener() {
                @Override
                public void OnStateChange(int state) {
                    switch (state) {
                        case ListVideoPlayer.STATE.ERROR:
                        case ListVideoPlayer.STATE.NORMAL:
                        case ListVideoPlayer.STATE.COMPLETE:
                            holder.mBgIv.setVisibility(View.VISIBLE);
                            break;
                        case ListVideoPlayer.STATE.PREPARING:
                        case ListVideoPlayer.STATE.PLAYING:
                        case ListVideoPlayer.STATE.PAUSE:
                            holder.mBgIv.setVisibility(View.GONE);
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

