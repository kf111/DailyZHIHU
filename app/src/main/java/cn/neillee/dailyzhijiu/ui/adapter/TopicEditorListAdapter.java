package cn.neillee.dailyzhijiu.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.neillee.dailyzhijiu.R;
import cn.neillee.dailyzhijiu.model.bean.orignal.TopicStoryListBean;
import cn.neillee.dailyzhijiu.utils.load.LoaderFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：Neil on 2016/3/23 14:16.
 * 邮箱：cn.neillee@gmail.com
 */
public class TopicEditorListAdapter extends BaseAdapter {
    private Context mContext;
    private List<TopicStoryListBean.EditorsBean> mDatas;

    public TopicEditorListAdapter(Context mContext, List<TopicStoryListBean.EditorsBean> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        if (mDatas == null) return -1;
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).
                    inflate(R.layout.item_lv_editor_theme, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.tvName.setText(mDatas.get(position).getName());
        if (TextUtils.isEmpty(mDatas.get(position).getBio())) {
            vh.tvBio.setVisibility(View.GONE);
        } else {
            vh.tvBio.setVisibility(View.VISIBLE);
            vh.tvBio.setText(mDatas.get(position).getBio());
        }
        LoaderFactory.getImageLoader().displayImage(vh.ivAvatar, mDatas.get(position).getAvatar(), null);
        return convertView;
    }

    class ViewHolder {
        @BindView(R.id.iv_avatar)
        ImageView ivAvatar;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_bio)
        TextView tvBio;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
