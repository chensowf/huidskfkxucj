package org.caonima.widght;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.caonima.R;
import org.caonima.bean.Node;

import java.util.List;

public class TreeAdapter extends RecyclerView.Adapter<TreeAdapter.TreeViewHolder> {

    private Context context;
    private List<Node> nodeList;
    private OnItemClickListener mOnItemClickListener;

    public TreeAdapter(Context context)
    {
        this.context = context;
    }

    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.node_item,viewGroup,false);
        return new TreeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder treeViewHolder, int i) {
        TreeViewHolder viewHolder = treeViewHolder;
        viewHolder.mCountry.setText(nodeList.get(i).type == Node.FATHER?nodeList.get(i).country:nodeList.get(i).name);
        viewHolder.mItemView.setTag(nodeList.get(i));
        viewHolder.mFlagImage.setVisibility(nodeList.get(i).type == Node.FATHER?View.VISIBLE:View.INVISIBLE);
        viewHolder.mArrowImage.setVisibility(nodeList.get(i).type == Node.FATHER?View.VISIBLE:View.INVISIBLE);
        viewHolder.mItemView.setOnClickListener(v -> {
            Node node = (Node) v.getTag();
            if(node.type == Node.FATHER) {
                if (node.state == Node.RETRACT) {
                    List<Node> list = node.mChileNode;
                    int index = nodeList.indexOf(node);
                    nodeList.addAll(index + 1, list);
                    node.state = Node.UNFOLD;
                    notifyDataSetChanged();
                } else {
                    nodeList.removeAll(node.mChileNode);
                    node.state = Node.RETRACT;
                    notifyDataSetChanged();
                }
            }
            else
            {
                if(mOnItemClickListener!=null)
                    mOnItemClickListener.onItemClick(node);
            }
        });
    }

    @Override
    public int getItemCount() {
        return nodeList == null?0:nodeList.size();
    }

    public void setNodeList(List<Node> nodeList)
    {
        this.nodeList = nodeList;
        Log.e("error",nodeList.size()+"");
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        this.mOnItemClickListener = onItemClickListener;
    }

    class TreeViewHolder extends RecyclerView.ViewHolder
    {

        public TextView mCountry;
        public ImageView mFlagImage;
        public ImageView mArrowImage;
        public View mItemView;
        public TreeViewHolder(@NonNull View itemView) {
            super(itemView);
            mCountry = itemView.findViewById(R.id.node_item_name);
            mFlagImage = itemView.findViewById(R.id.node_item_flag);
            mArrowImage = itemView.findViewById(R.id.node_item_arrow);
            mItemView = itemView;
        }
    }

    public interface OnItemClickListener
    {
        void onItemClick(Node node);
    }
}
