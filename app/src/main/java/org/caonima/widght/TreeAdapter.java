package org.caonima.widght;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class TreeAdapter extends RecyclerView.Adapter<TreeAdapter.TreeViewHolder> {


    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder treeViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class TreeViewHolder extends RecyclerView.ViewHolder
    {

        public TreeViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
