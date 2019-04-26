package org.caonima.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.caonima.R;

public class NodeActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;

    public static void startNodeActivity(Context context)
    {
        context.startActivity(new Intent(context, NodeActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        mRecyclerView = findViewById(R.id.node_node_list);
    }
}
