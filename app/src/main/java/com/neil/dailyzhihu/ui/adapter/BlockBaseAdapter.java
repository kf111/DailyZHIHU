package com.neil.dailyzhihu.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neil.dailyzhihu.R;
import com.neil.dailyzhihu.model.bean.IBlockBean;
import com.neil.dailyzhihu.utils.load.LoaderFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：Neil on 2017/2/16 22:29.
 * 邮箱：cn.neillee@gmail.com
 */

public class BlockBaseAdapter<T extends IBlockBean> extends RecyclerView.Adapter<BlockBaseAdapter.ViewHolder> {
    private Context mContext;
    private List<T> mData;

    private OnItemClickListener mOnItemClickListener;

    public BlockBaseAdapter(Context context, List<T> blockList) {
        this.mData = blockList;
        this.mContext = context;
    }

    @Override
    public BlockBaseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_gv_block, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final BlockBaseAdapter.ViewHolder holder, final int position) {
        holder.ivTitle.setText(mData.get(position).getTitle());
        holder.ivDescription.setText(mData.get(position).getDescription());
        LoaderFactory.getImageLoader().displayImage(holder.ivImg, mData.get(position).getImages().get(0), null);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.OnItemClick(position, holder);
            }
        });
//        if (position == getItemCount() - 1) {
//            holder.itemView.setPadding(holder.itemView.getLeft(), holder.itemView.getTop(),
//                    holder.itemView.getRight(), holder.itemView.getBottom() + DisplayUtil.dip2px(mContext, 8));
//        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_img)
        ImageView ivImg;
        @BindView(R.id.iv_title)
        TextView ivTitle;
        @BindView(R.id.tv_description)
        TextView ivDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClick(int position, ViewHolder shareView);
    }
}
